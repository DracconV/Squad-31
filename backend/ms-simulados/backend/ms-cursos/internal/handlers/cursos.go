package handlers

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"

	"github.com/seed-educa/ms-cursos/internal/models"
)

type CursoHandler struct {
	db *gorm.DB
}

func NewCursoHandler(db *gorm.DB) *CursoHandler {
	return &CursoHandler{db: db}
}

// GET /cursos — lista cursos ativos.
func (h *CursoHandler) Listar(c *gin.Context) {
	var cursos []models.Curso
	q := h.db.Order("nome asc")
	if c.Query("incluirInativos") != "true" {
		q = q.Where("ativo = ?", true)
	}
	if err := q.Find(&cursos).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao listar cursos"})
		return
	}
	c.JSON(http.StatusOK, cursos)
}

// GET /cursos/:id — detalhe de um curso.
func (h *CursoHandler) Buscar(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "id inválido"})
		return
	}
	var curso models.Curso
	if err := h.db.First(&curso, "id = ?", id).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			c.JSON(http.StatusNotFound, gin.H{"erro": "curso não encontrado"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao buscar curso"})
		return
	}
	c.JSON(http.StatusOK, curso)
}

// POST /cursos — cria um curso.
// TODO: proteger por JWT e perfil PROFESSOR/ADMIN_ESCOLA quando o
// api-gateway começar a injetar o usuário no header.
func (h *CursoHandler) Criar(c *gin.Context) {
	var input models.Curso
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": err.Error()})
		return
	}
	input.ID = uuid.New()
	input.Ativo = true
	if err := h.db.Create(&input).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao criar curso"})
		return
	}
	c.JSON(http.StatusCreated, input)
}
