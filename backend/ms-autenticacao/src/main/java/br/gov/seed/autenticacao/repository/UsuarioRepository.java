package br.gov.seed.autenticacao.repository;

import br.gov.seed.autenticacao.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByMatricula(String matricula);

    // Filtra no banco em vez de carregar toda a tabela em memória
    List<Usuario> findByInstituicao_Id(UUID instituicaoId);

    @Query("SELECT u FROM Usuario u WHERE u.matricula = :matricula AND u.instituicao.id = :instituicaoId")
    Optional<Usuario> findByMatriculaAndInstituicao(
        @Param("matricula") String matricula,
        @Param("instituicaoId") UUID instituicaoId
    );

    boolean existsByMatricula(String matricula);

    boolean existsByCpf(String cpf);
}
