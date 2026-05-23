package handlers

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"

	"github.com/seed-educa/ms-certificados/internal/models"
	"github.com/seed-educa/ms-certificados/internal/storage"
)

type CertificadoHandler struct {
	db      *gorm.DB
	storage *storage.MinioStorage
}

func NewCertificadoHandler(db *gorm.DB, s *storage.MinioStorage) *CertificadoHandler {
	return &CertificadoHandler{db: db, storage: s}
}

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
				"valido": false,
				"motivo": "certificado não encontrado",
			})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao consultar"})
		return
	}

	if !cert.Valido {
		c.JSON(http.StatusOK, gin.H{
			"valido":    false,
			"motivo":    "certificado revogado",
			"emitidoEm": cert.EmitidoEm,
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"valido":    true,
		"emitidoEm": cert.EmitidoEm,
		"alunoId":   cert.AlunoID,
		"cursoId":   cert.CursoID,
	})
}

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

	userID := c.GetString("userID")
	perfil := c.GetString("perfil")
	isAdmin := perfil == "ADMIN_SEED" || perfil == "ADMIN_ESCOLA"

	if !isAdmin && alunoID.String() != userID {
		c.JSON(http.StatusForbidden, gin.H{"erro": "acesso negado"})
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

// DownloadPDF serve o PDF do certificado diretamente do MinIO.
func (h *CertificadoHandler) DownloadPDF(c *gin.Context) {
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

	userID := c.GetString("userID")
	perfil := c.GetString("perfil")
	isAdmin := perfil == "ADMIN_SEED" || perfil == "ADMIN_ESCOLA"

	if !isAdmin && alunoID.String() != userID {
		c.JSON(http.StatusForbidden, gin.H{"erro": "acesso negado"})
		return
	}

	var cert models.Certificado
	err = h.db.First(&cert, "aluno_id = ? AND curso_id = ?", alunoID, cursoID).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			c.JSON(http.StatusNotFound, gin.H{"erro": "certificado não encontrado"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao buscar certificado"})
		return
	}

	// Extrai o nome do objeto do campo url_pdf: "/certificados/uuid.pdf" → "uuid.pdf"
	objectName := strings.TrimPrefix(cert.URLPDF, "/certificados/")
	if objectName == "" || objectName == cert.URLPDF {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "url do PDF inválida"})
		return
	}

	pdfBytes, err := h.storage.GetPDF(context.Background(), objectName)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"erro": "falha ao baixar PDF"})
		return
	}

	filename := fmt.Sprintf("certificado-%s.pdf", cert.QRCode[:8])
	c.Header("Content-Disposition", fmt.Sprintf("attachment; filename=%q", filename))
	c.Header("Content-Type", "application/pdf")
	c.Data(http.StatusOK, "application/pdf", pdfBytes)
}
