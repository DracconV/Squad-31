package db

import (
	"fmt"
	"os"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// Open abre a conexão com o Postgres usando as variáveis de
// ambiente padronizadas no projeto. As migrations são geridas
// pelo Flyway no ms-autenticacao — aqui só LEMOS e ESCREVEMOS
// nas tabelas já existentes (curso, modulo, inscricao_curso).
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

	gormCfg := &gorm.Config{
		Logger: logger.Default.LogMode(logger.Warn),
		// Não deixamos GORM criar/alterar tabelas — schema é do Flyway.
		DisableForeignKeyConstraintWhenMigrating: true,
	}

	conn, err := gorm.Open(postgres.Open(dsn), gormCfg)
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
