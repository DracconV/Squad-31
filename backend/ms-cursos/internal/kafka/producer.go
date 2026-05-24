package kafka

import (
	"context"
	"encoding/json"
	"time"

	kafkago "github.com/segmentio/kafka-go"
)

// EventoInscricaoConcluida é publicado no tópico "inscricao-concluida"
// e consumido pelo ms-certificados para emitir o certificado.
type EventoInscricaoConcluida struct {
	AlunoID     string    `json:"alunoId"`
	CursoID     string    `json:"cursoId"`
	NomeAluno   string    `json:"nomeAluno"`
	NomeCurso   string    `json:"nomeCurso"`
	ConcluidoEm time.Time `json:"concluidoEm"`
}

type Producer struct {
	writer *kafkago.Writer
}

func NewProducer(brokers []string, topic string) *Producer {
	w := &kafkago.Writer{
		Addr:         kafkago.TCP(brokers...),
		Topic:        topic,
		Balancer:     &kafkago.LeastBytes{},
		RequiredAcks: kafkago.RequireOne,
		Async:        false,
	}
	return &Producer{writer: w}
}

// PublicarConclusao envia o evento de conclusão para o Kafka.
func (p *Producer) PublicarConclusao(ctx context.Context, evento EventoInscricaoConcluida) error {
	data, err := json.Marshal(evento)
	if err != nil {
		return err
	}
	return p.writer.WriteMessages(ctx, kafkago.Message{
		Key:   []byte(evento.AlunoID + ":" + evento.CursoID),
		Value: data,
	})
}

func (p *Producer) Close() {
	_ = p.writer.Close()
}
