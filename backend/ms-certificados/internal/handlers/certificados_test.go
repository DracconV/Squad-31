package handlers_test

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/testcontainers/testcontainers-go"
	tcpostgres "github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/testcontainers/testcontainers-go/wait"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"

	"github.com/seed-educa/ms-certificados/internal/handlers"
	"github.com/seed-educa/ms-certificados/internal/middleware"
	"github.com/seed-educa/ms-certificados/internal/models"
)

const testSecret = "test-secret-key"

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

func setupRouter(db *gorm.DB) *gin.Engine {
	return setupRouterWithStore(db, nil) // nil: storage não usado nesses testes
}

// setupRouterWithStore permite injetar um PDFStore mock para testes de DownloadPDF.
func setupRouterWithStore(db *gorm.DB, store handlers.PDFStore) *gin.Engine {
	gin.SetMode(gin.TestMode)
	r := gin.New()

	cert := handlers.NewCertificadoHandler(db, store)
	r.GET("/verificar-certificado/:qr", cert.VerificarPublico)

	auth := r.Group("/", middleware.JWTWithSecret(testSecret))
	auth.GET("/certificados/:aluno/:curso", cert.BuscarPorAlunoCurso)
	auth.GET("/certificados/:aluno/:curso/pdf", cert.DownloadPDF)

	return r
}

// mockPDFStore implementa handlers.PDFStore para testes.
type mockPDFStore struct {
	data []byte
	err  error
}

func (m *mockPDFStore) GetPDF(_ context.Context, _ string) ([]byte, error) {
	return m.data, m.err
}

func makeToken(userID, perfil string) string {
	claims := jwt.MapClaims{
		"sub":    userID,
		"perfil": perfil,
		"nome":   "Teste",
		"exp":    time.Now().Add(time.Hour).Unix(),
	}
	token, _ := jwt.NewWithClaims(jwt.SigningMethodHS256, claims).SignedString([]byte(testSecret))
	return token
}


func TestVerificarPublico(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	alunoID := uuid.New()
	cursoID := uuid.New()

	validoCert := models.Certificado{
		ID: uuid.New(), AlunoID: alunoID, CursoID: cursoID,
		QRCode: "qr-valido", EmitidoEm: time.Now(), Valido: true,
	}
	revogadoCert := models.Certificado{
		ID: uuid.New(), AlunoID: alunoID, CursoID: uuid.New(),
		QRCode: "qr-revogado", EmitidoEm: time.Now(), Valido: false,
	}
	require.NoError(t, db.Create(&validoCert).Error)
	revogadoCert.Valido = true
	require.NoError(t, db.Create(&revogadoCert).Error)
	require.NoError(t, db.Model(&revogadoCert).Update("valido", false).Error)

	cases := []struct {
		name       string
		qr         string
		wantStatus int
		wantValido *bool
	}{
		{"certificado válido", "qr-valido", http.StatusOK, boolPtr(true)},
		{"certificado revogado", "qr-revogado", http.StatusOK, boolPtr(false)},
		{"não encontrado", "qr-inexistente", http.StatusNotFound, nil},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			w := httptest.NewRecorder()
			req, _ := http.NewRequest(http.MethodGet, "/verificar-certificado/"+tc.qr, nil)
			r.ServeHTTP(w, req)

			assert.Equal(t, tc.wantStatus, w.Code)

			var body map[string]any
			require.NoError(t, json.Unmarshal(w.Body.Bytes(), &body))
			if tc.wantValido != nil {
				assert.Equal(t, *tc.wantValido, body["valido"])
			}
		})
	}
}


func TestBuscarPorAlunoCurso(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	alunoID := uuid.New()
	outroAlunoID := uuid.New()
	cursoID := uuid.New()

	cert := models.Certificado{
		ID: uuid.New(), AlunoID: alunoID, CursoID: cursoID,
		QRCode: "qr-busca", EmitidoEm: time.Now(), Valido: true,
	}
	require.NoError(t, db.Create(&cert).Error)

	cases := []struct {
		name       string
		userID     string
		perfil     string
		alunoID    string
		cursoID    string
		token      string
		wantStatus int
	}{
		{
			name: "aluno acessa próprio certificado",
			userID: alunoID.String(), perfil: "ALUNO_EM",
			alunoID: alunoID.String(), cursoID: cursoID.String(),
			wantStatus: http.StatusOK,
		},
		{
			name: "admin acessa certificado de outro aluno",
			userID: uuid.New().String(), perfil: "ADMIN_SEED",
			alunoID: alunoID.String(), cursoID: cursoID.String(),
			wantStatus: http.StatusOK,
		},
		{
			name: "aluno tenta acessar certificado alheio",
			userID: outroAlunoID.String(), perfil: "ALUNO_EM",
			alunoID: alunoID.String(), cursoID: cursoID.String(),
			wantStatus: http.StatusForbidden,
		},
		{
			name: "sem token",
			userID: "", perfil: "",
			alunoID: alunoID.String(), cursoID: cursoID.String(),
			token:      "sem-token",
			wantStatus: http.StatusUnauthorized,
		},
		{
			name: "certificado não encontrado",
			userID: alunoID.String(), perfil: "ALUNO_EM",
			alunoID: alunoID.String(), cursoID: uuid.New().String(),
			wantStatus: http.StatusNotFound,
		},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			w := httptest.NewRecorder()
			url := fmt.Sprintf("/certificados/%s/%s", tc.alunoID, tc.cursoID)
			req, _ := http.NewRequest(http.MethodGet, url, nil)

			if tc.token == "sem-token" {
				// sem header
			} else {
				req.Header.Set("Authorization", "Bearer "+makeToken(tc.userID, tc.perfil))
			}

			r.ServeHTTP(w, req)
			assert.Equal(t, tc.wantStatus, w.Code, "body: %s", w.Body.String())
		})
	}
}

func TestDownloadPDF(t *testing.T) {
	db := setupDB(t)

	alunoID := uuid.New()
	cursoID := uuid.New()

	// Certificado com URL válida (será usado nos testes de sucesso e erro de storage)
	certValido := models.Certificado{
		ID:        uuid.New(),
		AlunoID:   alunoID,
		CursoID:   cursoID,
		QRCode:    "qr-download-ok",
		URLPDF:    "/certificados/cert-valido.pdf",
		EmitidoEm: time.Now(),
		Valido:    true,
	}
	require.NoError(t, db.Create(&certValido).Error)

	// Certificado com URL inválida (não começa com /certificados/)
	certURLInvalida := models.Certificado{
		ID:        uuid.New(),
		AlunoID:   alunoID,
		CursoID:   uuid.New(),
		QRCode:    "qr-url-invalida",
		URLPDF:    "",
		EmitidoEm: time.Now(),
		Valido:    true,
	}
	require.NoError(t, db.Create(&certURLInvalida).Error)

	cases := []struct {
		name       string
		store      handlers.PDFStore
		alunoParam string
		cursoParam string
		userID     string
		perfil     string
		useToken   bool
		wantStatus int
	}{
		{
			name:       "400 aluno UUID inválido",
			store:      &mockPDFStore{data: []byte("pdf")},
			alunoParam: "nao-e-uuid",
			cursoParam: cursoID.String(),
			userID:     alunoID.String(),
			perfil:     "ALUNO_EM",
			useToken:   true,
			wantStatus: http.StatusBadRequest,
		},
		{
			name:       "400 curso UUID inválido",
			store:      &mockPDFStore{data: []byte("pdf")},
			alunoParam: alunoID.String(),
			cursoParam: "nao-e-uuid",
			userID:     alunoID.String(),
			perfil:     "ALUNO_EM",
			useToken:   true,
			wantStatus: http.StatusBadRequest,
		},
		{
			name:       "403 aluno tentando baixar PDF alheio",
			store:      &mockPDFStore{data: []byte("pdf")},
			alunoParam: alunoID.String(),
			cursoParam: cursoID.String(),
			userID:     uuid.New().String(),
			perfil:     "ALUNO_EM",
			useToken:   true,
			wantStatus: http.StatusForbidden,
		},
		{
			name:       "404 certificado não encontrado",
			store:      &mockPDFStore{data: []byte("pdf")},
			alunoParam: uuid.New().String(),
			cursoParam: uuid.New().String(),
			userID:     alunoID.String(),
			perfil:     "ADMIN_SEED",
			useToken:   true,
			wantStatus: http.StatusNotFound,
		},
		{
			name:       "500 URL do PDF inválida",
			store:      &mockPDFStore{data: []byte("pdf")},
			alunoParam: alunoID.String(),
			cursoParam: certURLInvalida.CursoID.String(),
			userID:     alunoID.String(),
			perfil:     "ALUNO_EM",
			useToken:   true,
			wantStatus: http.StatusInternalServerError,
		},
		{
			name:       "500 erro no storage ao baixar PDF",
			store:      &mockPDFStore{err: fmt.Errorf("minio get: connection refused")},
			alunoParam: alunoID.String(),
			cursoParam: cursoID.String(),
			userID:     alunoID.String(),
			perfil:     "ALUNO_EM",
			useToken:   true,
			wantStatus: http.StatusInternalServerError,
		},
		{
			name:       "200 aluno baixa próprio PDF com sucesso",
			store:      &mockPDFStore{data: []byte("%PDF-1.4 fake content")},
			alunoParam: alunoID.String(),
			cursoParam: cursoID.String(),
			userID:     alunoID.String(),
			perfil:     "ALUNO_EM",
			useToken:   true,
			wantStatus: http.StatusOK,
		},
		{
			name:       "200 admin baixa PDF de qualquer aluno",
			store:      &mockPDFStore{data: []byte("%PDF-1.4 fake content")},
			alunoParam: alunoID.String(),
			cursoParam: cursoID.String(),
			userID:     uuid.New().String(),
			perfil:     "ADMIN_SEED",
			useToken:   true,
			wantStatus: http.StatusOK,
		},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			r := setupRouterWithStore(db, tc.store)
			url := fmt.Sprintf("/certificados/%s/%s/pdf", tc.alunoParam, tc.cursoParam)
			w := httptest.NewRecorder()
			req, _ := http.NewRequest(http.MethodGet, url, nil)
			if tc.useToken {
				req.Header.Set("Authorization", "Bearer "+makeToken(tc.userID, tc.perfil))
			}
			r.ServeHTTP(w, req)
			assert.Equal(t, tc.wantStatus, w.Code, "body: %s", w.Body.String())
		})
	}
}

func boolPtr(b bool) *bool { return &b }
