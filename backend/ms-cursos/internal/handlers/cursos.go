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

// Listar godoc
// @Summary      Lista cursos disponíveis
// @Description  Retorna todos os cursos ativos. Passe incluirInativos=true para ver todos.
// @Tags         Cursos
// @Produce      json
// @Param        incluirInativos  query   bool  false  "Incluir cursos inativos"
// @Success      200  {array}   models.Curso
// @Failure      500  {object}  map[string]string
// @Router       /cursos [get]
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

// Buscar godoc
// @Summary      Busca um curso por ID
// @Tags         Cursos
// @Produce      json
// @Param        id   path      string  true  "UUID do curso"
// @Success      200  {object}  models.Curso
// @Failure      400  {object}  map[string]string
// @Failure      404  {object}  map[string]string
// @Router       /cursos/{id} [get]
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

// Criar godoc
// @Summary      Cria um novo curso
// @Description  Apenas PROFESSOR e ADMIN podem criar cursos.
// @Tags         Cursos
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        curso  body      models.Curso  true  "Dados do curso"
// @Success      201    {object}  models.Curso
// @Failure      400    {object}  map[string]string
// @Failure      403    {object}  map[string]string
// @Router       /cursos [post]
func (h *CursoHandler) Criar(c *gin.Context) {
	perfil := c.GetString("perfil")
	if perfil != "PROFESSOR" && perfil != "ADMIN_ESCOLA" && perfil != "ADMIN_SEED" {
		c.JSON(http.StatusForbidden, gin.H{"erro": "apenas professores e administradores podem criar cursos"})
		return
	}

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

// Atualizar godoc
// @Summary      Atualiza nome e descrição de um curso
// @Description  Apenas PROFESSOR e ADMIN podem editar cursos.
// @Tags         Cursos
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id     path      string        true  "UUID do curso"
// @Param        curso  body      models.Curso  true  "Campos a atualizar"
// @Success      200    {object}  models.Curso
// @Failure      400    {object}  map[string]string
// @Failure      403    {object}  map[string]string
// @Failure      404    {object}  map[string]string
// @Router       /cursos/{id} [put]
func (h *CursoHandler) Atualizar(c *gin.Context) {
	perfil := c.GetString("perfil")
	if perfil != "PROFESSOR" && perfil != "ADMIN_ESCOLA" && perfil != "ADMIN_SEED" {
		c.JSON(http.StatusForbidden, gin.H{"erro": "apenas professores e administradores podem editar cursos"})
		return
	}

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

	var input struct {
		Nome      string `json:"nome"`
		Descricao string `json:"descricao"`
	}
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": err.Error()})
		return
	}
	if input.Nome != "" {
		curso.Nome = input.Nome
	}
	if input.Descricao != "" {
		curso.Descricao = input.Descricao
	}
	if err := h.db.Save(&curso).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao atualizar curso"})
		return
	}
	c.JSON(http.StatusOK, curso)
}

// Desativar godoc
// @Summary      Desativa um curso (soft delete)
// @Description  Apenas ADMIN pode desativar cursos. O curso não é removido do banco.
// @Tags         Cursos
// @Produce      json
// @Security     BearerAuth
// @Param        id  path      string  true  "UUID do curso"
// @Success      200  {object}  map[string]string
// @Failure      403  {object}  map[string]string
// @Failure      404  {object}  map[string]string
// @Router       /cursos/{id} [delete]
func (h *CursoHandler) Desativar(c *gin.Context) {
	perfil := c.GetString("perfil")
	if perfil != "ADMIN_ESCOLA" && perfil != "ADMIN_SEED" {
		c.JSON(http.StatusForbidden, gin.H{"erro": "apenas administradores podem desativar cursos"})
		return
	}

	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "id inválido"})
		return
	}

	result := h.db.Model(&models.Curso{}).Where("id = ?", id).Update("ativo", false)
	if result.Error != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao desativar curso"})
		return
	}
	if result.RowsAffected == 0 {
		c.JSON(http.StatusNotFound, gin.H{"erro": "curso não encontrado"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"mensagem": "curso desativado com sucesso"})
}
