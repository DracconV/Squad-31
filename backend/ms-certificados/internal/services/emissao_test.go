package services_test

import (
	"context"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/testcontainers/testcontainers-go"
	tcpostgres "github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/testcontainers/testcontainers-go/wait"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"

	kafkapkg "github.com/seed-educa/ms-certificados/internal/kafka"
	"github.com/seed-educa/ms-certificados/internal/models"
	"github.com/seed-educa/ms-certificados/internal/services"
)

type mockUploader struct {
	uploadedFiles []string
	returnErr     error
}

func (m *mockUploader) UploadPDF(_ context.Context, nome string, _ []byte) (string, error) {
	if m.returnErr != nil {
		return "", m.returnErr
	}
	m.uploadedFiles = append(m.uploadedFiles, nome)
	return "/certificados/" + nome, nil
}

func setupDB(t *testing.T) *gorm.DB {
	t.Helper()
	ctx := context.Background()

	pg, err := tcpostgres.Run(ctx, "postgres:16-alpine",
		tcpostgres.WithDatabase("testdb"),
		tcpostgres.WithUsername("test"),
		tcpostgres.WithPassword("test"),
		testcontainers.WithWaitStrategy(
			wait.ForLog("database system is ready to accept connections").
				WithOccurrence(2).
				WithStartupTimeout(60*time.Second),
		),
	)
	if err != nil {
		t.Skipf("Docker não acessível, pulando teste de integração: %v", err)
	}
	t.Cleanup(func() { _ = pg.Terminate(ctx) })

	dsn, err := pg.ConnectionString(ctx, "sslmode=disable")
	require.NoError(t, err)

	conn, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	require.NoError(t, err)

	require.NoError(t, conn.AutoMigrate(&models.Certificado{}))
	return conn
}

func evento(alunoID, cursoID string) kafkapkg.EventoInscricaoConcluida {
	return kafkapkg.EventoInscricaoConcluida{
		AlunoID:     alunoID,
		CursoID:     cursoID,
		NomeAluno:   "Maria Silva",
		NomeCurso:   "Programação Web",
		ConcluidoEm: time.Now(),
	}
}

func TestEmitir_Sucesso(t *testing.T) {
	db := setupDB(t)
	up := &mockUploader{}
	svc := services.NewEmissaoService(db, up)

	alunoID := uuid.New().String()
	cursoID := uuid.New().String()

	err := svc.Emitir(context.Background(), evento(alunoID, cursoID))
	require.NoError(t, err)

	assert.Len(t, up.uploadedFiles, 1)
	
	var cert models.Certificado
	err = db.First(&cert, "aluno_id = ? AND curso_id = ?", alunoID, cursoID).Error
	require.NoError(t, err)

	assert.True(t, cert.Valido)
	assert.NotEmpty(t, cert.QRCode)
	assert.NotEmpty(t, cert.URLPDF)
}

func TestEmitir_Idempotencia(t *testing.T) {
	db := setupDB(t)
	up := &mockUploader{}
	svc := services.NewEmissaoService(db, up)

	alunoID := uuid.New().String()
	cursoID := uuid.New().String()
	ev := evento(alunoID, cursoID)

	require.NoError(t, svc.Emitir(context.Background(), ev))
	require.NoError(t, svc.Emitir(context.Background(), ev)) 
	assert.Len(t, up.uploadedFiles, 1)

	var count int64
	db.Model(&models.Certificado{}).Where("aluno_id = ? AND curso_id = ?", alunoID, cursoID).Count(&count)
	assert.Equal(t, int64(1), count)
}

func TestEmitir_AlunoIDInvalido(t *testing.T) {
	db := setupDB(t)
	svc := services.NewEmissaoService(db, &mockUploader{})

	ev := evento("nao-e-uuid", uuid.New().String())
	err := svc.Emitir(context.Background(), ev)
	assert.ErrorContains(t, err, "alunoId inválido")
}

func TestEmitir_FalhaUpload(t *testing.T) {
	db := setupDB(t)
	up := &mockUploader{returnErr: assert.AnError}
	svc := services.NewEmissaoService(db, up)

	ev := evento(uuid.New().String(), uuid.New().String())
	err := svc.Emitir(context.Background(), ev)
	assert.ErrorContains(t, err, "upload")

	var count int64
	db.Model(&models.Certificado{}).Count(&count)
	assert.Equal(t, int64(0), count)
}
