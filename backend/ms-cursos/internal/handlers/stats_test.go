package handlers_test

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"github.com/seed-educa/ms-cursos/internal/models"
)

func TestStats_Aluno(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	alunoID := uuid.New()
	curso1 := criarCurso(t, db, "Stats Curso 1")
	curso2 := criarCurso(t, db, "Stats Curso 2")

	// Inscrição ativa
	require.NoError(t, db.Create(&models.InscricaoCurso{
		ID: uuid.New(), AlunoID: alunoID, CursoID: curso1.ID,
		DataInscricao: time.Now(), Concluido: false,
	}).Error)
	// Inscrição concluída
	require.NoError(t, db.Create(&models.InscricaoCurso{
		ID: uuid.New(), AlunoID: alunoID, CursoID: curso2.ID,
		DataInscricao: time.Now(), Concluido: true,
	}).Error)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, "/stats", nil)
	authHeader(req, alunoID.String(), "ALUNO_EM")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var body map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &body))
	assert.Equal(t, float64(1), body["cursosAtivos"])
	assert.Equal(t, float64(1), body["cursosConcluidos"])
}

func TestStats_Admin(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	criarCurso(t, db, "Admin Stats 1")
	criarCurso(t, db, "Admin Stats 2")

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, "/stats", nil)
	authHeader(req, uuid.New().String(), "ADMIN_SEED")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var body map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &body))
	assert.Contains(t, body, "totalCursos")
	assert.Contains(t, body, "totalInscricoes")
	assert.Contains(t, body, "totalAlunos")
}

func TestStats_Professor(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, "/stats", nil)
	authHeader(req, uuid.New().String(), "PROFESSOR")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var body map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &body))
	assert.Contains(t, body, "totalCursos")
	assert.Contains(t, body, "alunosAtivos")
}

func TestStats_SemToken(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, "/stats", nil)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusUnauthorized, w.Code)
}
