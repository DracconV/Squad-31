package br.gov.seed.autenticacao.repository;

import br.gov.seed.autenticacao.entity.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResetTokenRepository extends JpaRepository<ResetToken, UUID> {
    Optional<ResetToken> findByTokenAndUsadoFalse(String token);
}
