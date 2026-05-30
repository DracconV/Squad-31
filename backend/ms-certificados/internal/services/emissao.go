package services

import (
	"context"
	"fmt"
	"log"
	"time"

	"github.com/google/uuid"
	"github.com/johnfercher/maroto/v2"
	"github.com/johnfercher/maroto/v2/pkg/components/col"
	"github.com/johnfercher/maroto/v2/pkg/components/image"
	"github.com/johnfercher/maroto/v2/pkg/components/row"
	"github.com/johnfercher/maroto/v2/pkg/components/text"
	"github.com/johnfercher/maroto/v2/pkg/config"
	"github.com/johnfercher/maroto/v2/pkg/consts/align"
	"github.com/johnfercher/maroto/v2/pkg/consts/fontstyle"
	"github.com/johnfercher/maroto/v2/pkg/props"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"github.com/skip2/go-qrcode"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"

	kafkapkg "github.com/seed-educa/ms-certificados/internal/kafka"
	"github.com/seed-educa/ms-certificados/internal/models"
)

var (
	metricEmitidos = promauto.NewCounter(prometheus.CounterOpts{
		Name: "certificados_emitidos_total",
		Help: "Total de certificados emitidos com sucesso",
	})
	metricErros = promauto.NewCounter(prometheus.CounterOpts{
		Name: "certificados_erro_total",
		Help: "Total de erros na emissão de certificados",
	})
	metricDuracao = promauto.NewHistogram(prometheus.HistogramOpts{
		Name:    "certificados_emissao_segundos",
		Help:    "Tempo de emissão de cada certificado",
		Buckets: prometheus.DefBuckets,
	})
)

type Uploader interface {
	UploadPDF(ctx context.Context, nome string, data []byte) (string, error)
}

type EmissaoService struct {
	db      *gorm.DB
	storage Uploader
}

func NewEmissaoService(db *gorm.DB, s Uploader) *EmissaoService {
	return &EmissaoService{db: db, storage: s}
}

func (s *EmissaoService) Worker(ctx context.Context, jobs <-chan kafkapkg.EventoInscricaoConcluida) {
	for {
		select {
		case <-ctx.Done():
			return
		case evento, ok := <-jobs:
			if !ok {
				return
			}
			if err := s.Emitir(ctx, evento); err != nil {
				metricErros.Inc()
				log.Printf("emissao: erro: %v", err)
			}
		}
	}
}

func (s *EmissaoService) Emitir(ctx context.Context, evento kafkapkg.EventoInscricaoConcluida) error {
	inicio := time.Now()
	defer func() { metricDuracao.Observe(time.Since(inicio).Seconds()) }()

	alunoID, err := uuid.Parse(evento.AlunoID)
	if err != nil {
		return fmt.Errorf("alunoId inválido: %w", err)
	}
	cursoID, err := uuid.Parse(evento.CursoID)
	if err != nil {
		return fmt.Errorf("cursoId inválido: %w", err)
	}

	var existing models.Certificado
	if err := s.db.First(&existing, "aluno_id = ? AND curso_id = ?", alunoID, cursoID).Error; err == nil {
		log.Printf("emissao: certificado já existe aluno=%s curso=%s", alunoID, cursoID)
		return nil
	}

	certID := uuid.New()

	qrPNG, err := gerarQRCode(certID.String())
	if err != nil {
		return fmt.Errorf("qr: %w", err)
	}

	pdfBytes, err := gerarPDF(evento.NomeAluno, evento.NomeCurso, evento.ConcluidoEm, qrPNG)
	if err != nil {
		return fmt.Errorf("pdf: %w", err)
	}

	urlPDF, err := s.storage.UploadPDF(ctx, certID.String()+".pdf", pdfBytes)
	if err != nil {
		return fmt.Errorf("upload: %w", err)
	}

	cert := models.Certificado{
		ID:        certID,
		AlunoID:   alunoID,
		CursoID:   cursoID,
		QRCode:    certID.String(),
		URLPDF:    urlPDF,
		EmitidoEm: time.Now(),
		Valido:    true,
	}
	result := s.db.Clauses(clause.OnConflict{DoNothing: true}).Create(&cert)
	if result.Error != nil {
		return fmt.Errorf("db: %w", result.Error)
	}
	if result.RowsAffected == 0 {
		log.Printf("emissao: certificado já existia (corrida de workers), ignorando aluno=%s curso=%s", alunoID, cursoID)
		return nil
	}

	metricEmitidos.Inc()
	log.Printf("emissao: certificado %s emitido (aluno=%s, curso=%s)", certID, alunoID, cursoID)
	return nil
}

func gerarQRCode(token string) ([]byte, error) {
	return qrcode.Encode(token, qrcode.Medium, 256)
}

func gerarPDF(nomeAluno, nomeCurso string, concluidoEm time.Time, qrPNG []byte) ([]byte, error) {
	cfg := config.NewBuilder().Build()
	m := maroto.New(cfg)

	cinza := &props.Color{Red: 100, Green: 100, Blue: 100}

	m.AddRows(
		row.New(25).Add(col.New(12).Add(
			text.New("CERTIFICADO DE CONCLUSÃO", props.Text{
				Style: fontstyle.Bold,
				Size:  22,
				Align: align.Center,
				Top:   10,
			}),
		)),
		row.New(12).Add(col.New(12).Add(
			text.New("Certificamos que", props.Text{
				Size:  13,
				Align: align.Center,
				Top:   3,
			}),
		)),
		row.New(18).Add(col.New(12).Add(
			text.New(nomeAluno, props.Text{
				Style: fontstyle.Bold,
				Size:  18,
				Align: align.Center,
				Top:   3,
			}),
		)),
		row.New(12).Add(col.New(12).Add(
			text.New("concluiu com êxito o curso", props.Text{
				Size:  13,
				Align: align.Center,
			}),
		)),
		row.New(16).Add(col.New(12).Add(
			text.New(nomeCurso, props.Text{
				Style: fontstyle.Bold,
				Size:  16,
				Align: align.Center,
				Top:   2,
			}),
		)),
		row.New(12).Add(col.New(12).Add(
			text.New(fmt.Sprintf("Concluído em %s", concluidoEm.Format("02/01/2006")), props.Text{
				Size:  11,
				Align: align.Center,
				Top:   4,
			}),
		)),
		row.New(40).Add(col.New(12).Add(
			image.NewFromBytes(qrPNG, "png", props.Rect{
				Center:  true,
				Percent: 45,
			}),
		)),
		row.New(8).Add(col.New(12).Add(
			text.New("Escaneie para verificar a autenticidade", props.Text{
				Size:  9,
				Align: align.Center,
				Color: cinza,
			}),
		)),
	)

	doc, err := m.Generate()
	if err != nil {
		return nil, err
	}
	return doc.GetBytes(), nil
}
