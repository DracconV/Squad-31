package models

import (
	"time"

	"github.com/google/uuid"
)

// Curso espelha a tabela `curso` criada na migration
// V6__criar_cursos_e_certificados.sql do ms-autenticacao.
//
// Não usamos auto-migrate do GORM — o schema é controlado pelo
// Flyway, este struct só serve para mapeamento O/R.
type Curso struct {
	ID        uuid.UUID `gorm:"type:uuid;primaryKey" json:"id"`
	Nome      string    `gorm:"size:255;not null"   json:"nome"      binding:"required"`
	Descricao string    `gorm:"type:text"           json:"descricao"`
	Ativo     bool      `gorm:"not null;default:true" json:"ativo"`
	CriadoEm  time.Time `gorm:"column:criado_em;not null;default:now()" json:"criadoEm"`
}

func (Curso) TableName() string { return "curso" }

// Modulo — tabela `modulo`.
type Modulo struct {
	ID                   uuid.UUID  `gorm:"type:uuid;primaryKey" json:"id"`
	Nome                 string     `gorm:"size:255;not null"   json:"nome"`
	Ordem                int        `gorm:"not null"            json:"ordem"`
	CursoID              uuid.UUID  `gorm:"type:uuid;not null;column:curso_id" json:"cursoId"`
	PrerequisitoModuloID *uuid.UUID `gorm:"type:uuid;column:prerequisito_modulo_id" json:"prerequisitoModuloId,omitempty"`
}

func (Modulo) TableName() string { return "modulo" }

// InscricaoCurso — tabela `inscricao_curso`.
type InscricaoCurso struct {
	ID            uuid.UUID `gorm:"type:uuid;primaryKey" json:"id"`
	AlunoID       uuid.UUID `gorm:"type:uuid;not null;column:aluno_id" json:"alunoId"`
	CursoID       uuid.UUID `gorm:"type:uuid;not null;column:curso_id" json:"cursoId"`
	DataInscricao time.Time `gorm:"column:data_inscricao;not null;default:now()" json:"dataInscricao"`
	Concluido     bool      `gorm:"not null;default:false" json:"concluido"`
}

func (InscricaoCurso) TableName() string { return "inscricao_curso" }
