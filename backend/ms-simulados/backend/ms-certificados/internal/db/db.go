package db

import (
	"fmt"
	"os"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// Open abre a conexão com o Postgres compartilhado.
// Schema é controlado pelo Flyway (ms-autenticacao).
func Open() (*gorm.DB, error) {
	host := envDefault("DB_HOST", "localhost")
	port := envDefault("DB_PORT", "5432")
	user := envDefault("DB_USER", "seed_user")
	pass := envDefault("DB_PASSWORD", "seed_pass")
	name := envDefault("DB_NAME", "seed_educa")
	sslmode := envDefault("DB_SSLMODE", "disable")

	dsn := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=%s TimeZone=America/Maceio",
		host, port, user, pass, name, sslmode,
	)

	conn, err := gorm.Open(postgres.Open(dsn), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Warn),
		DisableForeignKeyConstraintWhenMigrating: true,
	})
	if err != nil {
		return nil, fmt.Errorf("postgres: %w", err)
	}
	return conn, nil
}

func envDefault(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
