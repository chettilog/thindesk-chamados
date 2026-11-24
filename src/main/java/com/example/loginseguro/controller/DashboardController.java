package com.example.loginseguro.controller;

import com.example.loginseguro.model.Chamado;
import com.example.loginseguro.service.ChamadoService;
import com.example.loginseguro.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * Dashboard com os indicadores dos chamados.
 * GESTOR, TECNICO e ADMIN veem todos os chamados; USUARIO vê só os próprios.
 */
@Controller
public class DashboardController {

    private final ChamadoService chamadoService;
    private final DashboardService dashboardService;

    public DashboardController(ChamadoService chamadoService, DashboardService dashboardService) {
        this.chamadoService = chamadoService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        List<Chamado> chamados = chamadoService.listarVisiveis(auth);
        Map<String, String> nomes = chamadoService.nomesDosUsuarios();

        model.addAttribute("resumo", dashboardService.montar(chamados, nomes));
        model.addAttribute("nomes", nomes);
        model.addAttribute("veTodos", chamadoService.podeVerTodos(auth));
        return "dashboard";
    }
}
