package com.example.loginseguro.controller;

import com.example.loginseguro.dto.ChamadoForm;
import com.example.loginseguro.dto.ResolverRequest;
import com.example.loginseguro.model.Chamado;
import com.example.loginseguro.model.Prioridade;
import com.example.loginseguro.model.StatusChamado;
import com.example.loginseguro.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Telas web dos chamados (Thymeleaf).
 * As regras ficam no ChamadoService; aqui só ligamos as telas a ele.
 */
@Controller
@RequestMapping("/chamados")
public class ChamadoPageController {

    private final ChamadoService chamadoService;

    public ChamadoPageController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    /** Lista de chamados, com filtro opcional por status. */
    @GetMapping
    public String lista(Authentication auth,
                        @RequestParam(required = false) StatusChamado status,
                        Model model) {
        List<Chamado> chamados = chamadoService.listarVisiveis(auth);
        if (status != null) {
            chamados = chamados.stream().filter(c -> c.getStatus() == status).toList();
        }
        model.addAttribute("chamados", chamados);
        model.addAttribute("statusFiltro", status);
        model.addAttribute("todosStatus", StatusChamado.values());
        model.addAttribute("nomes", chamadoService.nomesDosUsuarios());
        model.addAttribute("veTodos", chamadoService.podeVerTodos(auth));
        return "chamados/lista";
    }

    /** Formulário vazio para abrir chamado. */
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ChamadoForm());
        model.addAttribute("prioridades", Prioridade.values());
        return "chamados/novo";
    }

    /** Recebe o formulário de abertura. */
    @PostMapping
    public String abrir(Authentication auth,
                        @Valid @ModelAttribute("form") ChamadoForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("prioridades", Prioridade.values());
            return "chamados/novo";
        }
        Chamado chamado = chamadoService.abrir(auth, form.toRequest());
        redirect.addFlashAttribute("sucesso", "Chamado aberto com sucesso!");
        return "redirect:/chamados/" + chamado.getId();
    }

    /** Detalhe de um chamado. */
    @GetMapping("/{id}")
    public String detalhe(Authentication auth, @PathVariable String id, Model model) {
        model.addAttribute("chamado", chamadoService.consultar(auth, id));
        model.addAttribute("nomes", chamadoService.nomesDosUsuarios());
        model.addAttribute("podeAtender", chamadoService.podeAtender(auth));
        return "chamados/detalhe";
    }

    /** Técnico assume o chamado (ABERTO -> EM_ANDAMENTO). */
    @PostMapping("/{id}/atender")
    public String atender(Authentication auth, @PathVariable String id, RedirectAttributes redirect) {
        try {
            chamadoService.atender(auth, id);
            redirect.addFlashAttribute("sucesso", "Você assumiu este chamado.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        return "redirect:/chamados/" + id;
    }

    /** Técnico registra a solução e encerra o chamado. */
    @PostMapping("/{id}/resolver")
    public String resolver(Authentication auth,
                           @PathVariable String id,
                           @RequestParam(defaultValue = "") String solucao,
                           RedirectAttributes redirect) {
        if (solucao.isBlank()) {
            redirect.addFlashAttribute("erro", "Descreva a solução antes de resolver o chamado.");
            return "redirect:/chamados/" + id;
        }
        if (solucao.length() > 2000) {
            redirect.addFlashAttribute("erro", "A solução pode ter no máximo 2000 caracteres.");
            return "redirect:/chamados/" + id;
        }
        try {
            chamadoService.resolver(auth, id, new ResolverRequest(solucao));
            redirect.addFlashAttribute("sucesso", "Chamado resolvido!");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("erro", e.getReason());
        }
        return "redirect:/chamados/" + id;
    }
}
