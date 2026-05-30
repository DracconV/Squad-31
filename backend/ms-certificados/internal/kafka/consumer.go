package kafka

import (
	"context"
	"encoding/json"
	"log"
	"time"

	kafkalib "github.com/segmentio/kafka-go"
)

type EventoInscricaoConcluida struct {
	AlunoID     string    `json:"alunoId"`
	CursoID     string    `json:"cursoId"`
	NomeAluno   string    `json:"nomeAluno"`
	NomeCurso   string    `json:"nomeCurso"`
	ConcluidoEm time.Time `json:"concluidoEm"`
}

type Consumer struct {
	reader *kafkalib.Reader
	jobs   chan<- EventoInscricaoConcluida
}

func NewConsumer(brokers []string, topic, groupID string, jobs chan<- EventoInscricaoConcluida) *Consumer {
	r := kafkalib.NewReader(kafkalib.ReaderConfig{
		Brokers:        brokers,
		Topic:          topic,
		GroupID:        groupID,
		MinBytes:       1,
		MaxBytes:       1 << 20, // 1 MB
		CommitInterval: time.Second,
	})
	return &Consumer{reader: r, jobs: jobs}
}

func (c *Consumer) Run(ctx context.Context) {
	defer c.reader.Close()
	for {
		msg, err := c.reader.ReadMessage(ctx)
		if err != nil {
			if ctx.Err() != nil {
				return
			}
			log.Printf("kafka: erro ao ler mensagem: %v", err)
			continue
		}

		var evento EventoInscricaoConcluida
		if err := json.Unmarshal(msg.Value, &evento); err != nil {
			log.Printf("kafka: payload inválido (offset %d): %v", msg.Offset, err)
			continue
		}

		select {
		case c.jobs <- evento:
		case <-ctx.Done():
			return
		}
	}
}
