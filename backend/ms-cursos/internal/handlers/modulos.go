package handlers

import (
	"errors"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"

	"github.com/seed-educa/ms-cursos/internal/models"
)

type ModuloHandler struct {
	db *gorm.DB
}

func NewModuloHandler(db *gorm.DB) *ModuloHandler {
	return &ModuloHandler{db: db}
}

// Listar godoc
// @Summary      Lista módulos de um curso em ordem
// @Tags         Módulos
// @Produce      json
// @Security     BearerAuth
// @Param        id  path      string  true  "UUID do curso"
// @Success      200  {array}   models.Modulo
// @Failure      400  {object}  map[string]string
// @Router       /cursos/{id}/modulos [get]
func (h *ModuloHandler) Listar(c *gin.Context) {
	cursoID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "id do curso inválido"})
		return
	}

	var modulos []models.Modulo
	if err := h.db.Where("curso_id = ?", cursoID).Order("ordem asc").Find(&modulos).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao listar módulos"})
		return
	}
	if modulos == nil {
		modulos = []models.Modulo{}
	}
	c.JSON(http.StatusOK, modulos)
}

// Criar godoc
// @Summary      Cria um módulo em um curso
// @Tags         Módulos
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id     path      string        true  "UUID do curso"
// @Param        body   body      models.Modulo true  "Dados do módulo"
// @Success      201    {object}  models.Modulo
// @Failure      400    {object}  map[string]string
// @Failure      403    {object}  map[string]string
// @Failure      404    {object}  map[string]string
// @Router       /cursos/{id}/modulos [post]
func (h *ModuloHandler) Criar(c *gin.Context) {
	perfil := c.GetString("perfil")
	if perfil != "PROFESSOR" && perfil != "ADMIN_ESCOLA" && perfil != "ADMIN_SEED" {
		c.JSON(http.StatusForbidden, gin.H{"erro": "apenas professores e administradores podem criar módulos"})
		return
	}

	cursoID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "id do curso inválido"})
		return
	}

	// Verifica se o curso existe
	var count int64
	h.db.Model(&models.Curso{}).Where("id = ? AND ativo = true", cursoID).Count(&count)
	if count == 0 {
		c.JSON(http.StatusNotFound, gin.H{"erro": "curso não encontrado"})
		return
	}

	var input struct {
		Nome                 string     `json:"nome" binding:"required"`
		Ordem                int        `json:"ordem" binding:"required"`
		PrerequisitoModuloID *uuid.UUID `json:"prerequisitoModuloId"`
	}
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": err.Error()})
		return
	}

	modulo := models.Modulo{
		ID:                   uuid.New(),
		Nome:                 input.Nome,
		Ordem:                input.Ordem,
		CursoID:              cursoID,
		PrerequisitoModuloID: input.PrerequisitoModuloID,
	}
	if err := h.db.Create(&modulo).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao criar módulo"})
		return
	}
	c.JSON(http.StatusCreated, modulo)
}

// Atualizar godoc
// @Summary      Atualiza um módulo
// @Tags         Módulos
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id  path      string  true  "UUID do módulo"
// @Success      200  {object}  models.Modulo
// @Failure      400  {object}  map[string]string
// @Failure      403  {object}  map[string]string
// @Failure      404  {object}  map[string]string
// @Router       /modulos/{id} [put]
func (h *ModuloHandler) Atualizar(c *gin.Context) {
	perfil := c.GetString("perfil")
	if perfil != "PROFESSOR" && perfil != "ADMIN_ESCOLA" && perfil != "ADMIN_SEED" {
		c.JSON(http.StatusForbidden, gin.H{"erro": "apenas professores e administradores podem editar módulos"})
		return
	}

	moduloID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "id do módulo inválido"})
		return
	}

	var modulo models.Modulo
	if err := h.db.First(&modulo, "id = ?", moduloID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			c.JSON(http.StatusNotFound, gin.H{"erro": "módulo não encontrado"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao buscar módulo"})
		return
	}

	var input struct {
		Nome                 string     `json:"nome"`
		Ordem                int        `json:"ordem"`
		PrerequisitoModuloID *uuid.UUID `json:"prerequisitoModuloId"`
	}
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": err.Error()})
		return
	}
	if input.Nome != "" {
		modulo.Nome = input.Nome
	}
	if input.Ordem > 0 {
		modulo.Ordem = input.Ordem
	}
	modulo.PrerequisitoModuloID = input.PrerequisitoModuloID

	if err := h.db.Save(&modulo).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao atualizar módulo"})
		return
	}
	c.JSON(http.StatusOK, modulo)
}

// Remover godoc
// @Summary      Remove um módulo
// @Tags         Módulos
// @Produce      json
// @Security     BearerAuth
// @Param        id  path      string  true  "UUID do módulo"
// @Success      204  "Sem conteúdo"
// @Failure      403  {object}  map[string]string
// @Failure      404  {object}  map[string]string
// @Router       /modulos/{id} [delete]
func (h *ModuloHandler) Remover(c *gin.Context) {
	perfil := c.GetString("perfil")
	if perfil != "PROFESSOR" && perfil != "ADMIN_ESCOLA" && perfil != "ADMIN_SEED" {
		c.JSON(http.StatusForbidden, gin.H{"erro": "apenas professores e administradores podem remover módulos"})
		return
	}

	moduloID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "id do módulo inválido"})
		return
	}

	result := h.db.Delete(&models.Modulo{}, "id = ?", moduloID)
	if result.Error != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao remover módulo"})
		return
	}
	if result.RowsAffected == 0 {
		c.JSON(http.StatusNotFound, gin.H{"erro": "módulo não encontrado"})
		return
	}
	c.Status(http.StatusNoContent)
}

// Concluir godoc
// @Summary      Aluno marca módulo como concluído
// @Tags         Módulos
// @Produce      json
// @Security     BearerAuth
// @Param        id  path      string  true  "UUID do módulo"
// @Success      200  {object}  map[string]interface{}
// @Failure      400  {object}  map[string]string
// @Failure      404  {object}  map[string]string
// @Failure      409  {object}  map[string]string
// @Router       /modulos/{id}/concluir [post]
func (h *ModuloHandler) Concluir(c *gin.Context) {
	alunoID, err := uuid.Parse(c.GetString("userID"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "userID inválido no token"})
		return
	}

	moduloID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "id do módulo inválido"})
		return
	}

	// Verifica se o módulo existe
	var count int64
	h.db.Table("modulo").Where("id = ?", moduloID).Count(&count)
	if count == 0 {
		c.JSON(http.StatusNotFound, gin.H{"erro": "módulo não encontrado"})
		return
	}

	// Idempotência — ignora se já concluído
	type ProgressoModulo struct {
		AlunoID     uuid.UUID `gorm:"column:aluno_id"`
		ModuloID    uuid.UUID `gorm:"column:modulo_id"`
		ConcluidoEm time.Time `gorm:"column:concluido_em"`
	}

	var existing ProgressoModulo
	err = h.db.Table("progresso_modulo").
		Where("aluno_id = ? AND modulo_id = ?", alunoID, moduloID).
		First(&existing).Error
	if err == nil {
		c.JSON(http.StatusConflict, gin.H{"erro": "módulo já concluído", "concluidoEm": existing.ConcluidoEm})
		return
	}

	progresso := ProgressoModulo{
		AlunoID:     alunoID,
		ModuloID:    moduloID,
		ConcluidoEm: time.Now(),
	}
	if err := h.db.Table("progresso_modulo").Create(&progresso).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao registrar conclusão"})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"mensagem":    "módulo concluído com sucesso",
		"moduloId":    moduloID,
		"concluidoEm": progresso.ConcluidoEm,
	})
}

// Progresso godoc
// @Summary      Retorna progresso do aluno em uma inscrição
// @Description  Calcula % de módulos concluídos do curso vinculado à inscrição.
// @Tags         Inscrições
// @Produce      json
// @Security     BearerAuth
// @Param        id  path      string  true  "UUID da inscrição"
// @Success      200  {object}  map[string]interface{}
// @Failure      400  {object}  map[string]string
// @Failure      403  {object}  map[string]string
// @Failure      404  {object}  map[string]string
// @Router       /inscricoes/{id}/progresso [get]
func (h *ModuloHandler) Progresso(c *gin.Context) {
	inscricaoID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "id da inscrição inválido"})
		return
	}

	var inscricao models.InscricaoCurso
	if err := h.db.First(&inscricao, "id = ?", inscricaoID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			c.JSON(http.StatusNotFound, gin.H{"erro": "inscrição não encontrada"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao buscar inscrição"})
		return
	}

	// Controle de acesso — aluno só vê o próprio progresso
	perfil := c.GetString("perfil")
	callerID := c.GetString("userID")
	isAdmin := perfil == "ADMIN_SEED" || perfil == "ADMIN_ESCOLA" || perfil == "PROFESSOR"
	if !isAdmin && inscricao.AlunoID.String() != callerID {
		c.JSON(http.StatusForbidden, gin.H{"erro": "acesso negado"})
		return
	}

	// Total de módulos do curso
	var totalModulos int64
	h.db.Table("modulo").Where("curso_id = ?", inscricao.CursoID).Count(&totalModulos)

	// Módulos concluídos pelo aluno neste curso
	var concluidos int64
	h.db.Table("progresso_modulo pm").
		Joins("JOIN modulo m ON m.id = pm.modulo_id").
		Where("pm.aluno_id = ? AND m.curso_id = ?", inscricao.AlunoID, inscricao.CursoID).
		Count(&concluidos)

	percentual := 0.0
	if totalModulos > 0 {
		percentual = float64(concluidos) / float64(totalModulos) * 100
	}

	c.JSON(http.StatusOK, gin.H{
		"inscricaoId":    inscricaoID,
		"cursoId":        inscricao.CursoID,
		"alunoId":        inscricao.AlunoID,
		"totalModulos":   totalModulos,
		"concluidos":     concluidos,
		"percentual":     percentual,
		"cursoConcluido": inscricao.Concluido,
	})
}
