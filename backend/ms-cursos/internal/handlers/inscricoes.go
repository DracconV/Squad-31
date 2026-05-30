package handlers

import (
	"context"
	"errors"
	"log"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"

	kafkapkg "github.com/seed-educa/ms-cursos/internal/kafka"
	"github.com/seed-educa/ms-cursos/internal/models"
)

type InscricaoHandler struct {
	db       *gorm.DB
	producer *kafkapkg.Producer
}

func NewInscricaoHandler(db *gorm.DB, producer *kafkapkg.Producer) *InscricaoHandler {
	return &InscricaoHandler{db: db, producer: producer}
}

// Inscrever godoc
// @Summary      Inscreve o aluno autenticado em um curso
// @Description  Idempotente — retorna 409 se o aluno já estiver inscrito.
// @Tags         Inscrições
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        body  body      object{cursoId=string}  true  "UUID do curso"
// @Success      201   {object}  models.InscricaoCurso
// @Failure      400   {object}  map[string]string
// @Failure      404   {object}  map[string]string
// @Failure      409   {object}  map[string]string
// @Router       /inscricoes [post]
func (h *InscricaoHandler) Inscrever(c *gin.Context) {
	alunoID, err := uuid.Parse(c.GetString("userID"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "userID inválido no token"})
		return
	}

	var input struct {
		CursoID uuid.UUID `json:"cursoId" binding:"required"`
	}
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": err.Error()})
		return
	}

	// Verifica se o curso existe
	var curso models.Curso
	if err := h.db.First(&curso, "id = ? AND ativo = true", input.CursoID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			c.JSON(http.StatusNotFound, gin.H{"erro": "curso não encontrado"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao verificar curso"})
		return
	}

	// Idempotência: não duplicar inscrição
	var existing models.InscricaoCurso
	err = h.db.Where("aluno_id = ? AND curso_id = ?", alunoID, input.CursoID).First(&existing).Error
	if err == nil {
		c.JSON(http.StatusConflict, gin.H{"erro": "aluno já inscrito neste curso", "inscricao": existing})
		return
	}
	if !errors.Is(err, gorm.ErrRecordNotFound) {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao verificar inscrição"})
		return
	}

	inscricao := models.InscricaoCurso{
		ID:            uuid.New(),
		AlunoID:       alunoID,
		CursoID:       input.CursoID,
		DataInscricao: time.Now(),
		Concluido:     false,
	}
	if err := h.db.Create(&inscricao).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao criar inscrição"})
		return
	}
	c.JSON(http.StatusCreated, inscricao)
}

// ListarMinhas godoc
// @Summary      Lista inscrições do aluno autenticado
// @Description  Retorna todas as inscrições com nome e descrição do curso.
// @Tags         Inscrições
// @Produce      json
// @Security     BearerAuth
// @Success      200  {array}   models.InscricaoCurso
// @Failure      400  {object}  map[string]string
// @Router       /inscricoes/minhas [get]
func (h *InscricaoHandler) ListarMinhas(c *gin.Context) {
	alunoID, err := uuid.Parse(c.GetString("userID"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "userID inválido"})
		return
	}

	type InscricaoComCurso struct {
		models.InscricaoCurso
		NomeCurso      string `json:"nomeCurso"`
		DescricaoCurso string `json:"descricaoCurso"`
	}

	var resultado []InscricaoComCurso
	err = h.db.
		Table("inscricao_curso ic").
		Select("ic.*, c.nome as nome_curso, c.descricao as descricao_curso").
		Joins("JOIN curso c ON c.id = ic.curso_id").
		Where("ic.aluno_id = ?", alunoID).
		Order("ic.data_inscricao desc").
		Scan(&resultado).Error
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao listar inscrições"})
		return
	}
	if resultado == nil {
		resultado = []InscricaoComCurso{}
	}
	c.JSON(http.StatusOK, resultado)
}

// Concluir godoc
// @Summary      Marca inscrição como concluída e dispara geração de certificado
// @Description  Publica evento no Kafka para o ms-certificados gerar o PDF automaticamente. PROFESSOR e ADMIN podem concluir qualquer inscrição; ALUNO só a própria.
// @Tags         Inscrições
// @Produce      json
// @Security     BearerAuth
// @Param        id   path      string  true  "UUID da inscrição"
// @Success      200  {object}  models.InscricaoCurso
// @Failure      400  {object}  map[string]string
// @Failure      403  {object}  map[string]string
// @Failure      404  {object}  map[string]string
// @Failure      409  {object}  map[string]string
// @Router       /inscricoes/{id}/concluir [put]
func (h *InscricaoHandler) Concluir(c *gin.Context) {
	inscricaoID, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "id inválido"})
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

	if inscricao.Concluido {
		c.JSON(http.StatusConflict, gin.H{"erro": "inscrição já concluída"})
		return
	}

	// Controle de acesso
	perfil := c.GetString("perfil")
	callerID := c.GetString("userID")
	isAdmin := perfil == "ADMIN_SEED" || perfil == "ADMIN_ESCOLA" || perfil == "PROFESSOR"
	if !isAdmin && inscricao.AlunoID.String() != callerID {
		c.JSON(http.StatusForbidden, gin.H{"erro": "acesso negado"})
		return
	}

	// Busca dados para o evento Kafka
	var curso models.Curso
	h.db.First(&curso, "id = ?", inscricao.CursoID)

	var usuario struct{ Nome string }
	h.db.Table("usuario").Select("nome").Where("id = ?", inscricao.AlunoID).Scan(&usuario)

	agora := time.Now()
	if err := h.db.Model(&inscricao).Update("concluido", true).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao concluir inscrição"})
		return
	}

	// Publica evento Kafka em goroutine com retry (falha não bloqueia resposta)
	if h.producer != nil {
		go func() {
			evt := kafkapkg.EventoInscricaoConcluida{
				AlunoID:     inscricao.AlunoID.String(),
				CursoID:     inscricao.CursoID.String(),
				NomeAluno:   usuario.Nome,
				NomeCurso:   curso.Nome,
				ConcluidoEm: agora,
			}
			var lastErr error
			for attempt := 1; attempt <= 3; attempt++ {
				ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
				lastErr = h.producer.PublicarConclusao(ctx, evt)
				cancel()
				if lastErr == nil {
					return
				}
				log.Printf("[kafka] tentativa %d falhou para inscricao-concluida alunoId=%s: %v", attempt, evt.AlunoID, lastErr)
				if attempt < 3 {
					time.Sleep(time.Duration(attempt) * 2 * time.Second)
				}
			}
			log.Printf("[kafka] ERRO DEFINITIVO ao publicar inscricao-concluida alunoId=%s cursoId=%s: %v", evt.AlunoID, evt.CursoID, lastErr)
		}()
	}

	inscricao.Concluido = true
	c.JSON(http.StatusOK, inscricao)
}
