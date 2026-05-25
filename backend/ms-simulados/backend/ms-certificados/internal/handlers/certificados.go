package handlers

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"

	"github.com/seed-educa/ms-certificados/internal/models"
)

type CertificadoHandler struct {
	db *gorm.DB
}

func NewCertificadoHandler(db *gorm.DB) *CertificadoHandler {
	return &CertificadoHandler{db: db}
}

// GET /verificar-certificado/:qr
//
// Endpoint público — qualquer pessoa pode validar a autenticidade
// de um certificado escaneando o QR Code. Não expõe dados sensíveis
// do aluno, só confirma se o certificado existe e está válido.
func (h *CertificadoHandler) VerificarPublico(c *gin.Context) {
	qr := c.Param("qr")
	if qr == "" {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "qr é obrigatório"})
		return
	}

	var cert models.Certificado
	err := h.db.First(&cert, "qr_code = ?", qr).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			c.JSON(http.StatusNotFound, gin.H{
				"valido":   false,
				"motivo":   "certificado não encontrado",
			})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao consultar"})
		return
	}

	if !cert.Valido {
		c.JSON(http.StatusOK, gin.H{
			"valido":     false,
			"motivo":     "certificado revogado",
			"emitidoEm":  cert.EmitidoEm,
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"valido":    true,
		"emitidoEm": cert.EmitidoEm,
		// Nome do aluno e curso virão de um JOIN quando integrarmos
		// com ms-cursos / ms-autenticacao. Por enquanto só o id.
		"alunoId":   cert.AlunoID,
		"cursoId":   cert.CursoID,
	})
}

// GET /certificados/:aluno/:curso
//
// Endpoint autenticado: retorna o certificado de um aluno num curso.
// O front usa para baixar o PDF (campo urlPdf) ou exibir o QR.
func (h *CertificadoHandler) BuscarPorAlunoCurso(c *gin.Context) {
	alunoID, err := uuid.Parse(c.Param("aluno"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "aluno inválido"})
		return
	}
	cursoID, err := uuid.Parse(c.Param("curso"))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"erro": "curso inválido"})
		return
	}

	var cert models.Certificado
	err = h.db.First(&cert, "aluno_id = ? AND curso_id = ?", alunoID, cursoID).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			c.JSON(http.StatusNotFound, gin.H{"erro": "certificado não encontrado"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao buscar"})
		return
	}
	c.JSON(http.StatusOK, cert)
}
