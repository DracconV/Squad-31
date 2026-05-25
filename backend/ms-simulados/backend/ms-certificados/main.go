package main

import (
	"log"
	"os"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"

	"github.com/seed-educa/ms-certificados/internal/db"
	"github.com/seed-educa/ms-certificados/internal/handlers"
)

func main() {
	_ = godotenv.Load()

	conn, err := db.Open()
	if err != nil {
		log.Fatalf("falha ao conectar no Postgres: %v", err)
	}

	r := gin.Default()
	r.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"http://localhost:5173", "http://localhost:8080"},
		AllowMethods:     []string{"GET", "POST", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type"},
		AllowCredentials: true,
	}))

	r.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "UP",
			"service": "ms-certificados",
		})
	})

	cert := handlers.NewCertificadoHandler(conn)

	// Pública: validação de QR Code
	r.GET("/verificar-certificado/:qr", cert.VerificarPublico)

	// Autenticadas (validação de JWT virá do api-gateway)
	r.GET("/certificados/:aluno/:curso", cert.BuscarPorAlunoCurso)

	port := os.Getenv("PORT")
	if port == "" {
		port = "8086"
	}
	log.Printf("ms-certificados ouvindo em :%s", port)
	if err := r.Run(":" + port); err != nil {
		log.Fatalf("falha ao subir o servidor: %v", err)
	}
}
