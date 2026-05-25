package main

import (
	"log"
	"os"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"

	"github.com/seed-educa/ms-cursos/internal/db"
	"github.com/seed-educa/ms-cursos/internal/handlers"
)

func main() {
	// Carrega variáveis de .env se existir (em prod usamos as do
	// container; em dev local quase sempre tem um .env).
	_ = godotenv.Load()

	conn, err := db.Open()
	if err != nil {
		log.Fatalf("falha ao conectar no Postgres: %v", err)
	}

	r := gin.Default()
	r.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"http://localhost:5173", "http://localhost:8080"},
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type"},
		AllowCredentials: true,
	}))

	r.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "UP",
			"service": "ms-cursos",
		})
	})

	cursoHandler := handlers.NewCursoHandler(conn)
	r.GET("/cursos", cursoHandler.Listar)
	r.GET("/cursos/:id", cursoHandler.Buscar)
	r.POST("/cursos", cursoHandler.Criar)

	port := os.Getenv("PORT")
	if port == "" {
		port = "8085"
	}
	log.Printf("ms-cursos ouvindo em :%s", port)
	if err := r.Run(":" + port); err != nil {
		log.Fatalf("falha ao subir o servidor: %v", err)
	}
}
