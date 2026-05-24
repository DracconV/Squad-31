package kafka_test

import (
	"context"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"github.com/seed-educa/ms-cursos/internal/kafka"
)

func TestNewProducer(t *testing.T) {
	p := kafka.NewProducer([]string{"localhost:9092"}, "test-topic")
	require.NotNil(t, p)
	p.Close() // não deve entrar em pânico
}

func TestEventoInscricaoConcluida_Campos(t *testing.T) {
	agora := time.Now()
	ev := kafka.EventoInscricaoConcluida{
		AlunoID:     "aluno-1",
		CursoID:     "curso-1",
		NomeAluno:   "João",
		NomeCurso:   "Matemática",
		ConcluidoEm: agora,
	}

	assert.Equal(t, "aluno-1", ev.AlunoID)
	assert.Equal(t, "curso-1", ev.CursoID)
	assert.Equal(t, "João", ev.NomeAluno)
	assert.Equal(t, "Matemática", ev.NomeCurso)
	assert.Equal(t, agora, ev.ConcluidoEm)
}

// TestPublicarConclusao_SemBroker verifica que PublicarConclusao retorna erro
// quando nenhum broker Kafka está disponível (porta 1 → conexão recusada).
func TestPublicarConclusao_SemBroker(t *testing.T) {
	p := kafka.NewProducer([]string{"localhost:1"}, "test-topic")
	defer p.Close()

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	ev := kafka.EventoInscricaoConcluida{
		AlunoID:     "aluno-1",
		CursoID:     "curso-1",
		NomeAluno:   "Teste",
		NomeCurso:   "Curso Teste",
		ConcluidoEm: time.Now(),
	}

	err := p.PublicarConclusao(ctx, ev)
	assert.Error(t, err, "esperado erro pois não há broker disponível")
}
