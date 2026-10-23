package com.example.loginseguro.controller;

import com.example.loginseguro.dto.ChamadoRequest;
import com.example.loginseguro.dto.ResolverRequest;
import com.example.loginseguro.model.Chamado;
import com.example.loginseguro.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoRestController {

    private final ChamadoService chamadoService;

    public ChamadoRestController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<Chamado> abrir(Authentication auth, @Valid @RequestBody ChamadoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.abrir(auth, req));
    }

    @GetMapping("/usuario/{id}")
    public List<Chamado> listarPorUsuario(Authentication auth, @PathVariable String id) {
        return chamadoService.listarPorUsuario(auth, id);
    }

    @GetMapping("/{id}")
    public Chamado consultar(Authentication auth, @PathVariable String id) {
        return chamadoService.consultar(auth, id);
    }

    @PutMapping("/{id}/resolver")
    public Chamado resolver(Authentication auth, @PathVariable String id, @Valid @RequestBody ResolverRequest req) {
        return chamadoService.resolver(auth, id, req);
    }
}