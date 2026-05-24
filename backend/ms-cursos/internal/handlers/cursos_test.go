package handlers_test

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestListarCursos(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	criarCurso(t, db, "Matemática")
	criarCurso(t, db, "Português")

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, "/cursos", nil)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var body []map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &body))
	assert.GreaterOrEqual(t, len(body), 2)
}

func TestBuscarCurso_Encontrado(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	c := criarCurso(t, db, "Física")

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, "/cursos/"+c.ID.String(), nil)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var body map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &body))
	assert.Equal(t, "Física", body["nome"])
}

func TestBuscarCurso_NaoEncontrado(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, "/cursos/"+uuid.New().String(), nil)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNotFound, w.Code)
}

func TestBuscarCurso_IDInvalido(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, "/cursos/nao-e-uuid", nil)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusBadRequest, w.Code)
}

func TestCriarCurso_Professor(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	body := `{"nome":"Química","descricao":"Curso de química avançada"}`
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPost, "/cursos", bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	authHeader(req, uuid.New().String(), "PROFESSOR")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusCreated, w.Code)
	var resp map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &resp))
	assert.Equal(t, "Química", resp["nome"])
}

func TestCriarCurso_Aluno_Forbidden(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	body := `{"nome":"Tentativa","descricao":"não deve criar"}`
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPost, "/cursos", bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	authHeader(req, uuid.New().String(), "ALUNO_EM")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusForbidden, w.Code)
}

func TestCriarCurso_SemToken(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	body := `{"nome":"Tentativa"}`
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPost, "/cursos", bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusUnauthorized, w.Code)
}

func TestAtualizarCurso_Professor(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	c := criarCurso(t, db, "Original")

	body := `{"nome":"Atualizado","descricao":"nova descrição"}`
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPut, fmt.Sprintf("/cursos/%s", c.ID), bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	authHeader(req, uuid.New().String(), "PROFESSOR")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var resp map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &resp))
	assert.Equal(t, "Atualizado", resp["nome"])
}

func TestAtualizarCurso_NaoEncontrado(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	body := `{"nome":"Novo"}`
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPut, "/cursos/"+uuid.New().String(), bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	authHeader(req, uuid.New().String(), "PROFESSOR")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNotFound, w.Code)
}

func TestDesativarCurso_Admin(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	c := criarCurso(t, db, "Para Desativar")

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodDelete, fmt.Sprintf("/cursos/%s", c.ID), nil)
	authHeader(req, uuid.New().String(), "ADMIN_SEED")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
}

func TestDesativarCurso_Professor_Forbidden(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	c := criarCurso(t, db, "Não Pode Desativar")

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodDelete, fmt.Sprintf("/cursos/%s", c.ID), nil)
	authHeader(req, uuid.New().String(), "PROFESSOR")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusForbidden, w.Code)
}
