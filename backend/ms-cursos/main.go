// @title           SEED Educa — ms-cursos
// @version         1.0
// @description     API de cursos, inscrições e certificados do SEED Educa. Autentique-se com o token JWT obtido em /auth/login.
// @host            localhost:8080
// @BasePath        /
// @securityDefinitions.apikey BearerAuth
// @in              header
// @name            Authorization
// @description     Formato: Bearer {token}
package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"

	_ "github.com/seed-educa/ms-cursos/docs"
	"github.com/seed-educa/ms-cursos/internal/db"
	"github.com/seed-educa/ms-cursos/internal/handlers"
	kafkapkg "github.com/seed-educa/ms-cursos/internal/kafka"
	"github.com/seed-educa/ms-cursos/internal/middleware"
)

func main() {
	_ = godotenv.Load()

	// ── Banco de dados ──────────────────────────────────────
	conn, err := db.Open()
	if err != nil {
		log.Fatalf("falha ao conectar no Postgres: %v", err)
	}

	// ── Kafka producer ──────────────────────────────────────
	brokersEnv := os.Getenv("KAFKA_BROKERS")
	if brokersEnv == "" {
		brokersEnv = "localhost:9092"
	}
	topic := os.Getenv("KAFKA_TOPIC")
	if topic == "" {
		topic = "inscricao-concluida"
	}
	brokers := strings.Split(brokersEnv, ",")
	producer := kafkapkg.NewProducer(brokers, topic)
	defer producer.Close()

	// ── Handlers ────────────────────────────────────────────
	cursoHandler := handlers.NewCursoHandler(conn)
	inscricaoHandler := handlers.NewInscricaoHandler(conn, producer)
	statsHandler := handlers.NewStatsHandler(conn)
	moduloHandler := handlers.NewModuloHandler(conn)

	// ── Router ──────────────────────────────────────────────
	r := gin.Default()
	r.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"http://localhost", "http://localhost:80", "http://localhost:5173", "http://localhost:8080"},
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type"},
		AllowCredentials: true,
	}))

	// ── Swagger UI ──────────────────────────────────────────
	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	r.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "UP", "service": "ms-cursos"})
	})

	// Rotas públicas
	r.GET("/cursos", cursoHandler.Listar)
	r.GET("/cursos/:id", cursoHandler.Buscar)

	// Rotas protegidas por JWT
	auth := r.Group("/", middleware.JWT())
	{
		auth.POST("/cursos", cursoHandler.Criar)
		auth.PUT("/cursos/:id", cursoHandler.Atualizar)
		auth.DELETE("/cursos/:id", cursoHandler.Desativar)
		auth.POST("/inscricoes", inscricaoHandler.Inscrever)
		auth.GET("/inscricoes/minhas", inscricaoHandler.ListarMinhas)
		auth.PUT("/inscricoes/:id/concluir", inscricaoHandler.Concluir)
		auth.GET("/inscricoes/:id/progresso", moduloHandler.Progresso)
		auth.GET("/stats", statsHandler.Get)

		// Módulos
		auth.GET("/cursos/:id/modulos", moduloHandler.Listar)
		auth.POST("/cursos/:id/modulos", moduloHandler.Criar)
		auth.PUT("/modulos/:id", moduloHandler.Atualizar)
		auth.DELETE("/modulos/:id", moduloHandler.Remover)
		auth.POST("/modulos/:id/concluir", moduloHandler.Concluir)
	}

	// ── Servidor com graceful shutdown ──────────────────────
	port := os.Getenv("PORT")
	if port == "" {
		port = "8085"
	}

	srv := &http.Server{Addr: ":" + port, Handler: r}

	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	go func() {
		log.Printf("ms-cursos ouvindo em :%s", port)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("falha ao subir servidor: %v", err)
		}
	}()

	<-ctx.Done()
	log.Println("ms-cursos: encerrando...")
	shutCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	_ = srv.Shutdown(shutCtx)
	log.Println("ms-cursos: encerrado.")
}
