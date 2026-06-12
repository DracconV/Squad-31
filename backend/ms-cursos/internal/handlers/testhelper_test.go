package handlers_test

import (
	"context"
	"net/http"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/stretchr/testify/require"
	"github.com/testcontainers/testcontainers-go"
	tcpostgres "github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/testcontainers/testcontainers-go/wait"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"

	"github.com/seed-educa/ms-cursos/internal/handlers"
	"github.com/seed-educa/ms-cursos/internal/middleware"
	"github.com/seed-educa/ms-cursos/internal/models"
)

const testSecret = "test-secret-key"

// setupDB sobe um Postgres via Testcontainers e retorna uma conexão GORM.
// Pula o teste se Docker não estiver disponível.
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

	// AutoMigrate cria as tabelas no banco de testes
	require.NoError(t, conn.AutoMigrate(&models.Curso{}, &models.InscricaoCurso{}))

	// Tabela usuario mínima necessária para os handlers de stats e inscricoes
	// gen_random_uuid() é nativo no PostgreSQL 13+ (sem pgcrypto)
	conn.Exec(`CREATE TABLE IF NOT EXISTS usuario (
		id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
		nome TEXT NOT NULL DEFAULT 'Teste'
	)`)

	return conn
}

// setupRouter monta o router Gin no modo teste com todos os handlers.
func setupRouter(db *gorm.DB) *gin.Engine {
	gin.SetMode(gin.TestMode)
	r := gin.New()

	cursoH := handlers.NewCursoHandler(db)
	inscricaoH := handlers.NewInscricaoHandler(db, nil) // producer nil: Kafka não usado em testes
	statsH := handlers.NewStatsHandler(db)

	r.GET("/cursos", cursoH.Listar)
	r.GET("/cursos/:id", cursoH.Buscar)

	auth := r.Group("/", middleware.JWTWithSecret(testSecret))
	auth.POST("/cursos", cursoH.Criar)
	auth.PUT("/cursos/:id", cursoH.Atualizar)
	auth.DELETE("/cursos/:id", cursoH.Desativar)
	auth.POST("/inscricoes", inscricaoH.Inscrever)
	auth.GET("/inscricoes/minhas", inscricaoH.ListarMinhas)
	auth.PUT("/inscricoes/:id/concluir", inscricaoH.Concluir)
	auth.GET("/stats", statsH.Get)

	return r
}

// makeToken gera um JWT assinado com o secret de teste.
func makeToken(userID, perfil string) string {
	claims := jwt.MapClaims{
		"sub":    userID,
		"perfil": perfil,
		"nome":   "Usuário Teste",
		"exp":    time.Now().Add(time.Hour).Unix(),
	}
	tok, _ := jwt.NewWithClaims(jwt.SigningMethodHS256, claims).
		SignedString([]byte(testSecret))
	return "Bearer " + tok
}

// authHeader retorna o header Authorization pronto para usar na requisição.
func authHeader(req *http.Request, userID, perfil string) *http.Request {
	req.Header.Set("Authorization", makeToken(userID, perfil))
	return req
}

// criarCurso insere um curso diretamente no banco (helper de fixture).
func criarCurso(t *testing.T, db *gorm.DB, nome string) models.Curso {
	t.Helper()
	c := models.Curso{
		ID:        uuid.New(),
		Nome:      nome,
		Descricao: "Descrição de " + nome,
		Ativo:     true,
		CriadoEm:  time.Now(),
	}
	require.NoError(t, db.Create(&c).Error)
	return c
}
