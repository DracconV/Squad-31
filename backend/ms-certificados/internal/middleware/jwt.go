package middleware

import (
	"fmt"
	"net/http"
	"os"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

type Claims struct {
	Perfil        string `json:"perfil"`
	Nome          string `json:"nome"`
	InstituicaoID string `json:"instituicaoId,omitempty"`
	jwt.RegisteredClaims
}

func JWT() gin.HandlerFunc {
	return JWTWithSecret(envDefault("JWT_SECRET", "seed-educa-jwt-secret-key-2024-muito-segura-para-desenvolvimento"))
}

func JWTWithSecret(secret string) gin.HandlerFunc {
	key := []byte(secret)
	return func(c *gin.Context) {
		header := c.GetHeader("Authorization")
		if !strings.HasPrefix(header, "Bearer ") {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"erro": "token ausente"})
			return
		}
		tokenStr := strings.TrimPrefix(header, "Bearer ")

		claims := &Claims{}
		token, err := jwt.ParseWithClaims(tokenStr, claims, func(t *jwt.Token) (interface{}, error) {
			if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, fmt.Errorf("algoritmo inesperado: %v", t.Header["alg"])
			}
			return key, nil
		})
		if err != nil || !token.Valid {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"erro": "token inválido"})
			return
		}

		c.Set("userID", claims.Subject)
		c.Set("perfil", claims.Perfil)
		c.Set("nome", claims.Nome)
		c.Next()
	}
}

func envDefault(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
