package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

type StatsHandler struct {
	db *gorm.DB
}

func NewStatsHandler(db *gorm.DB) *StatsHandler {
	return &StatsHandler{db: db}
}

// GET /stats — retorna contadores para o dashboard do usuário autenticado.
func (h *StatsHandler) Get(c *gin.Context) {
	perfil := c.GetString("perfil")
	userID := c.GetString("userID")

	switch perfil {
	case "ALUNO_EM", "ALUNO_EJA", "ALUNO_PROF":
		h.statsAluno(c, userID)
	case "PROFESSOR":
		h.statsProfessor(c)
	case "ADMIN_ESCOLA", "ADMIN_SEED":
		h.statsAdmin(c)
	default:
		c.JSON(http.StatusOK, gin.H{})
	}
}

func (h *StatsHandler) statsAluno(c *gin.Context, userID string) {
	alunoID, err := uuid.Parse(userID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "userID inválido"})
		return
	}

	var totalInscricoes, totalConcluidos int64
	h.db.Table("inscricao_curso").Where("aluno_id = ?", alunoID).Count(&totalInscricoes)
	h.db.Table("inscricao_curso").Where("aluno_id = ? AND concluido = true", alunoID).Count(&totalConcluidos)

	var totalCursos int64
	h.db.Table("curso").Where("ativo = true").Count(&totalCursos)

	c.JSON(http.StatusOK, gin.H{
		"cursosAtivos":    totalInscricoes - totalConcluidos,
		"cursosConcluidos": totalConcluidos,
		"certificados":    totalConcluidos,
		"totalCursos":     totalCursos,
	})
}

func (h *StatsHandler) statsProfessor(c *gin.Context) {
	var totalCursos, totalInscricoes, totalConcluidos, totalAlunos int64
	h.db.Table("curso").Where("ativo = true").Count(&totalCursos)
	h.db.Table("inscricao_curso").Count(&totalInscricoes)
	h.db.Table("inscricao_curso").Where("concluido = true").Count(&totalConcluidos)
	h.db.Table("inscricao_curso").Distinct("aluno_id").Count(&totalAlunos)

	c.JSON(http.StatusOK, gin.H{
		"totalCursos":     totalCursos,
		"totalInscricoes": totalInscricoes,
		"totalConcluidos": totalConcluidos,
		"alunosAtivos":    totalAlunos,
	})
}

func (h *StatsHandler) statsAdmin(c *gin.Context) {
	var totalCursos, totalInscricoes, totalConcluidos, totalAlunos int64
	h.db.Table("curso").Where("ativo = true").Count(&totalCursos)
	h.db.Table("inscricao_curso").Count(&totalInscricoes)
	h.db.Table("inscricao_curso").Where("concluido = true").Count(&totalConcluidos)
	h.db.Table("inscricao_curso").Distinct("aluno_id").Count(&totalAlunos)

	c.JSON(http.StatusOK, gin.H{
		"totalCursos":     totalCursos,
		"totalInscricoes": totalInscricoes,
		"totalConcluidos": totalConcluidos,
		"totalAlunos":     totalAlunos,
	})
}
