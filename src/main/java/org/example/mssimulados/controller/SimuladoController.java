package org.example.mssimulados.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.mssimulados.model.SessaoSimulado;
import org.example.mssimulados.service.TempoSimuladoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/simulado")
@RequiredArgsConstructor
public class SimuladoController {
    private final TempoSimuladoService tempoSimuladoService;

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<?> iniciar(@PathVariable Long id, HttpSession session) {
        SessaoSimulado sessao = new SessaoSimulado(id, LocalDateTime.now(), 0, new java.util.HashMap<>());
        session.setAttribute("sessao_simulado_" + id, sessao);
        return ResponseEntity.ok(sessao);
    }

    @GetMapping("/{id}/sessao")
    public ResponseEntity<?> getSessao(@PathVariable Long id, HttpSession session) {
        SessaoSimulado sessao = (SessaoSimulado) session.getAttribute("sessao_simulado_" + id);
        if (sessao == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(sessao);
    }

    @PostMapping("/{id}/encerrar")
    public ResponseEntity<?> encerrar(@PathVariable Long id) {
        tempoSimuladoService.encerrarPorTempo(id);
        return ResponseEntity.ok("Simulado encerrado!");
    }
}