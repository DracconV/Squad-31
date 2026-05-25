package models

import (
	"time"

	"github.com/google/uuid"
)

// Certificado espelha a tabela `certificado` da migration
// V6__criar_cursos_e_certificados.sql.
type Certificado struct {
	ID        uuid.UUID `gorm:"type:uuid;primaryKey" json:"id"`
	AlunoID   uuid.UUID `gorm:"type:uuid;not null;column:aluno_id" json:"alunoId"`
	CursoID   uuid.UUID `gorm:"type:uuid;not null;column:curso_id" json:"cursoId"`
	QRCode    string    `gorm:"size:255;not null;uniqueIndex;column:qr_code" json:"qrCode"`
	URLPDF    string    `gorm:"size:500;column:url_pdf" json:"urlPdf,omitempty"`
	EmitidoEm time.Time `gorm:"column:emitido_em;not null;default:now()" json:"emitidoEm"`
	Valido    bool      `gorm:"not null;default:true" json:"valido"`
}

func (Certificado) TableName() string { return "certificado" }
