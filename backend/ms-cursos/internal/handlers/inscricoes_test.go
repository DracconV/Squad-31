package handlers_test

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"github.com/seed-educa/ms-cursos/internal/models"
)

func TestInscrever_Sucesso(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	alunoID := uuid.New()
	curso := criarCurso(t, db, "Curso para Inscrição")

	body := fmt.Sprintf(`{"cursoId":"%s"}`, curso.ID)
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPost, "/inscricoes", bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	authHeader(req, alunoID.String(), "ALUNO_EM")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusCreated, w.Code)
	var resp map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &resp))
	assert.Equal(t, alunoID.String(), resp["alunoId"])
}

func TestInscrever_Duplicado(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	alunoID := uuid.New()
	curso := criarCurso(t, db, "Curso Duplicado")

	// Insere inscrição diretamente
	ins := models.InscricaoCurso{
		ID: uuid.New(), AlunoID: alunoID, CursoID: curso.ID,
		DataInscricao: time.Now(), Concluido: false,
	}
	require.NoError(t, db.Create(&ins).Error)

	body := fmt.Sprintf(`{"cursoId":"%s"}`, curso.ID)
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPost, "/inscricoes", bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	authHeader(req, alunoID.String(), "ALUNO_EM")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusConflict, w.Code)
}

func TestInscrever_CursoInexistente(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	body := fmt.Sprintf(`{"cursoId":"%s"}`, uuid.New())
	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPost, "/inscricoes", bytes.NewBufferString(body))
	req.Header.Set("Content-Type", "application/json")
	authHeader(req, uuid.New().String(), "ALUNO_EM")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNotFound, w.Code)
}

func TestListarMinhas(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	alunoID := uuid.New()
	outroAlunoID := uuid.New()
	curso := criarCurso(t, db, "Curso Listagem")

	// Inscrição do aluno A
	ins := models.InscricaoCurso{
		ID: uuid.New(), AlunoID: alunoID, CursoID: curso.ID,
		DataInscricao: time.Now(), Concluido: false,
	}
	require.NoError(t, db.Create(&ins).Error)

	// Inscrição de outro aluno (não deve aparecer)
	ins2 := models.InscricaoCurso{
		ID: uuid.New(), AlunoID: outroAlunoID, CursoID: curso.ID,
		DataInscricao: time.Now(), Concluido: false,
	}
	require.NoError(t, db.Create(&ins2).Error)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodGet, "/inscricoes/minhas", nil)
	authHeader(req, alunoID.String(), "ALUNO_EM")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var resp []map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &resp))
	assert.Len(t, resp, 1)
}

func TestConcluir_Sucesso(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	alunoID := uuid.New()
	curso := criarCurso(t, db, "Curso Conclusão")

	ins := models.InscricaoCurso{
		ID: uuid.New(), AlunoID: alunoID, CursoID: curso.ID,
		DataInscricao: time.Now(), Concluido: false,
	}
	require.NoError(t, db.Create(&ins).Error)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPut, fmt.Sprintf("/inscricoes/%s/concluir", ins.ID), nil)
	authHeader(req, alunoID.String(), "ALUNO_EM")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var resp map[string]any
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &resp))
	assert.True(t, resp["concluido"].(bool))
}

func TestConcluir_JaConcluido(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	alunoID := uuid.New()
	curso := criarCurso(t, db, "Curso Já Concluído")

	ins := models.InscricaoCurso{
		ID: uuid.New(), AlunoID: alunoID, CursoID: curso.ID,
		DataInscricao: time.Now(), Concluido: true,
	}
	require.NoError(t, db.Create(&ins).Error)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPut, fmt.Sprintf("/inscricoes/%s/concluir", ins.ID), nil)
	authHeader(req, alunoID.String(), "ALUNO_EM")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusConflict, w.Code)
}

func TestConcluir_OutroAluno_Forbidden(t *testing.T) {
	db := setupDB(t)
	r := setupRouter(db)

	proprietarioID := uuid.New()
	invasorID := uuid.New()
	curso := criarCurso(t, db, "Curso Alheio")

	ins := models.InscricaoCurso{
		ID: uuid.New(), AlunoID: proprietarioID, CursoID: curso.ID,
		DataInscricao: time.Now(), Concluido: false,
	}
	require.NoError(t, db.Create(&ins).Error)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest(http.MethodPut, fmt.Sprintf("/inscricoes/%s/concluir", ins.ID), nil)
	authHeader(req, invasorID.String(), "ALUNO_EM")
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusForbidden, w.Code)
}
