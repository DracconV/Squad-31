package br.gov.seed.autenticacao.service;

import br.gov.seed.autenticacao.dto.InstituicaoDTO;
import br.gov.seed.autenticacao.entity.Instituicao;
import br.gov.seed.autenticacao.repository.InstituicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstituicaoService {

    private final InstituicaoRepository instituicaoRepository;

    public List<InstituicaoDTO.Response> listarAtivas() {
        return instituicaoRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(InstituicaoDTO.Response::from)
                .toList();
    }

    public InstituicaoDTO.Response buscarPorId(UUID id) {
        Instituicao inst = instituicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instituicao nao encontrada: " + id));
        return InstituicaoDTO.Response.from(inst);
    }

    @Transactional
    public InstituicaoDTO.Response criar(InstituicaoDTO.CriarRequest request) {
        if (instituicaoRepository.existsByCodigoInep(request.codigoInep())) {
            throw new IllegalArgumentException("Codigo INEP ja cadastrado: " + request.codigoInep());
        }

        Instituicao instituicao = Instituicao.builder()
                .nome(request.nome())
                .municipio(request.municipio())
                .codigoInep(request.codigoInep())
                .ativo(true)
                .build();

        return InstituicaoDTO.Response.from(instituicaoRepository.save(instituicao));
    }
}
