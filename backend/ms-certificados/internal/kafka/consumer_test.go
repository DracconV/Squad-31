package kafka_test

import (
	"context"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	kafkapkg "github.com/seed-educa/ms-certificados/internal/kafka"
)

func TestNewConsumer(t *testing.T) {
	jobs := make(chan kafkapkg.EventoInscricaoConcluida, 1)
	c := kafkapkg.NewConsumer([]string{"localhost:9092"}, "test-topic", "test-group", jobs)
	require.NotNil(t, c)
}

func TestEventoInscricaoConcluida_Campos(t *testing.T) {
	agora := time.Now()
	ev := kafkapkg.EventoInscricaoConcluida{
		AlunoID:     "aluno-1",
		CursoID:     "curso-1",
		NomeAluno:   "Maria",
		NomeCurso:   "Programação",
		ConcluidoEm: agora,
	}

	assert.Equal(t, "aluno-1", ev.AlunoID)
	assert.Equal(t, "curso-1", ev.CursoID)
	assert.Equal(t, "Maria", ev.NomeAluno)
	assert.Equal(t, "Programação", ev.NomeCurso)
	assert.Equal(t, agora, ev.ConcluidoEm)
}

// TestConsumer_Run_ContextCancelado verifica que Run retorna quando o contexto é cancelado.
// Usa porta inválida (localhost:1) para garantir falha rápida de conexão.
func TestConsumer_Run_ContextCancelado(t *testing.T) {
	jobs := make(chan kafkapkg.EventoInscricaoConcluida, 1)
	// Porta 1 → conexão recusada imediatamente, sem Kafka real
	c := kafkapkg.NewConsumer([]string{"localhost:1"}, "test-topic", "test-group", jobs)

	ctx, cancel := context.WithCancel(context.Background())
	cancel() // cancela antes de chamar Run

	done := make(chan struct{})
	go func() {
		c.Run(ctx)
		close(done)
	}()

	select {
	case <-done:
		// Run retornou corretamente após cancelamento do contexto
	case <-time.After(10 * time.Second):
		t.Fatal("Run não retornou após cancelamento do contexto")
	}
}
