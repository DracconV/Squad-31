// @title           SEED Educa — ms-certificados
// @version         1.0
// @description     API de emissão e verificação de certificados do SEED Educa. Autentique-se com o token JWT obtido em /auth/login.
// @host            localhost:8080
// @BasePath        /
// @securityDefinitions.apikey BearerAuth
// @in              header
// @name            Authorization
// @description     Formato: Bearer {token}
package main

import (
	"context"
	"errors"
	"log"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"

	_ "github.com/seed-educa/ms-certificados/docs"
	"github.com/seed-educa/ms-certificados/internal/db"
	"github.com/seed-educa/ms-certificados/internal/handlers"
	kafkapkg "github.com/seed-educa/ms-certificados/internal/kafka"
	"github.com/seed-educa/ms-certificados/internal/middleware"
	"github.com/seed-educa/ms-certificados/internal/services"
	"github.com/seed-educa/ms-certificados/internal/storage"
)

const workerCount = 5

func main() {
	_ = godotenv.Load()

	conn, err := db.Open()
	if err != nil {
		log.Fatalf("falha ao conectar no Postgres: %v", err)
	}

	minioStorage, err := storage.NewMinioStorage()
	if err != nil {
		log.Fatalf("falha ao conectar no MinIO: %v", err)
	}

	ctx, cancel := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer cancel()

	jobs := make(chan kafkapkg.EventoInscricaoConcluida, 100)

	brokers := strings.Split(envDefault("KAFKA_BROKERS", "localhost:9092"), ",")
	topic := envDefault("KAFKA_TOPIC", "inscricao-concluida")
	groupID := envDefault("KAFKA_GROUP_ID", "ms-certificados")

	consumer := kafkapkg.NewConsumer(brokers, topic, groupID, jobs)
	go consumer.Run(ctx)

	emissaoSvc := services.NewEmissaoService(conn, minioStorage)
	for i := 0; i < workerCount; i++ {
		go emissaoSvc.Worker(ctx, jobs)
	}

	r := gin.Default()
	r.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"http://localhost", "http://localhost:80", "http://localhost:5173", "http://localhost:8080"},
		AllowMethods:     []string{"GET", "POST", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type"},
		AllowCredentials: true,
	}))

	// ── Swagger UI ──────────────────────────────────────────────
	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	r.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "UP", "service": "ms-certificados"})
	})
	r.GET("/metrics", gin.WrapH(promhttp.Handler()))

	cert := handlers.NewCertificadoHandler(conn, minioStorage)
	r.GET("/verificar-certificado/:qr", cert.VerificarPublico)

	autenticado := r.Group("/", middleware.JWT())
	autenticado.GET("/certificados/:aluno/:curso", cert.BuscarPorAlunoCurso)
	autenticado.GET("/certificados/:aluno/:curso/pdf", cert.DownloadPDF)

	port := envDefault("PORT", "8086")
	srv := &http.Server{Addr: ":" + port, Handler: r}

	go func() {
		<-ctx.Done()
		log.Println("ms-certificados: desligando...")
		_ = srv.Shutdown(context.Background())
	}()

	log.Printf("ms-certificados ouvindo em :%s", port)
	if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		log.Fatalf("falha ao subir o servidor: %v", err)
	}
}

func envDefault(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
