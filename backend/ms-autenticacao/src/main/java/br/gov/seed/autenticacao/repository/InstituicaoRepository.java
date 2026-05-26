package br.gov.seed.autenticacao.repository;

import br.gov.seed.autenticacao.entity.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InstituicaoRepository extends JpaRepository<Instituicao, UUID> {
    List<Instituicao> findByAtivoTrueOrderByNomeAsc();
    boolean existsByCodigoInep(String codigoInep);
}
