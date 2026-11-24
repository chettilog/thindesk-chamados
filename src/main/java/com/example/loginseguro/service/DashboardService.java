package com.example.loginseguro.service;

import com.example.loginseguro.dto.Barra;
import com.example.loginseguro.dto.DashboardResumo;
import com.example.loginseguro.model.Chamado;
import com.example.loginseguro.model.Prioridade;
import com.example.loginseguro.model.StatusChamado;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Calcula os indicadores do dashboard a partir de uma lista de chamados.
 * A lista já vem filtrada pelo perfil (todos os chamados ou só os do usuário).
 */
@Service
public class DashboardService {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");

    public DashboardResumo montar(List<Chamado> chamados, Map<String, String> nomes) {
        DashboardResumo r = new DashboardResumo();

        long abertos = contarStatus(chamados, StatusChamado.ABERTO);
        long emAndamento = contarStatus(chamados, StatusChamado.EM_ANDAMENTO);
        long resolvidos = contarStatus(chamados, StatusChamado.RESOLVIDO);
        long total = chamados.size();

        r.setTotal(total);
        r.setAbertos(abertos);
        r.setEmAndamento(emAndamento);
        r.setResolvidos(resolvidos);
        r.setTaxaResolucao(total > 0 ? (int) Math.round(resolvidos * 100.0 / total) : 0);
        r.setAltaPendentes(chamados.stream()
                .filter(c -> prioridadeDe(c) == Prioridade.ALTA && c.getStatus() != StatusChamado.RESOLVIDO)
                .count());
        r.setTempoMedioResolucao(tempoMedio(chamados));

        // Distribuição por status
        r.setPorStatus(List.of(
                new Barra(StatusChamado.ABERTO.getRotulo(), abertos, total, "status-ABERTO"),
                new Barra(StatusChamado.EM_ANDAMENTO.getRotulo(), emAndamento, total, "status-EM_ANDAMENTO"),
                new Barra(StatusChamado.RESOLVIDO.getRotulo(), resolvidos, total, "status-RESOLVIDO")
        ));

        // Distribuição por prioridade
        List<Barra> porPrioridade = new ArrayList<>();
        for (Prioridade p : Prioridade.values()) {
            long qtd = chamados.stream().filter(c -> prioridadeDe(c) == p).count();
            porPrioridade.add(new Barra(p.getRotulo(), qtd, total, "prio-" + p.name()));
        }
        r.setPorPrioridade(porPrioridade);

        // Chamados abertos nos últimos 7 dias
        LocalDate hoje = LocalDate.now();
        Map<LocalDate, Long> porDia = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            porDia.put(hoje.minusDays(i), 0L);
        }
        for (Chamado c : chamados) {
            if (c.getDataAbertura() == null) continue;
            LocalDate dia = c.getDataAbertura().toLocalDate();
            if (porDia.containsKey(dia)) {
                porDia.put(dia, porDia.get(dia) + 1);
            }
        }
        long maxDia = Collections.max(porDia.values());
        List<Barra> ultimosDias = new ArrayList<>();
        porDia.forEach((dia, qtd) -> ultimosDias.add(new Barra(dia.format(DIA), qtd, maxDia, "")));
        r.setUltimosDias(ultimosDias);

        // Chamados resolvidos por técnico
        Map<String, Long> resolvidosPorTecnico = new HashMap<>();
        for (Chamado c : chamados) {
            if (c.getStatus() == StatusChamado.RESOLVIDO && c.getTecnicoId() != null) {
                String nome = nomes.getOrDefault(c.getTecnicoId(), "Técnico removido");
                resolvidosPorTecnico.merge(nome, 1L, Long::sum);
            }
        }
        long maxTecnico = resolvidosPorTecnico.values().stream().max(Long::compare).orElse(0L);
        List<Barra> porTecnico = new ArrayList<>();
        resolvidosPorTecnico.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> porTecnico.add(new Barra(e.getKey(), e.getValue(), maxTecnico, "")));
        r.setPorTecnico(porTecnico);

        // 5 chamados mais recentes (a lista já vem ordenada por data de abertura)
        r.setRecentes(chamados.stream().limit(5).toList());

        return r;
    }

    private long contarStatus(List<Chamado> chamados, StatusChamado status) {
        return chamados.stream().filter(c -> c.getStatus() == status).count();
    }

    // Chamados antigos (antes da prioridade existir) contam como MEDIA
    private Prioridade prioridadeDe(Chamado c) {
        return c.getPrioridade() != null ? c.getPrioridade() : Prioridade.MEDIA;
    }

    private String tempoMedio(List<Chamado> chamados) {
        OptionalDouble mediaMinutos = chamados.stream()
                .filter(c -> c.getDataAbertura() != null && c.getDataResolucao() != null)
                .mapToLong(c -> Duration.between(c.getDataAbertura(), c.getDataResolucao()).toMinutes())
                .average();

        if (mediaMinutos.isEmpty()) {
            return "—";
        }
        long minutos = Math.round(mediaMinutos.getAsDouble());
        if (minutos < 60) {
            return minutos + " min";
        }
        long horas = minutos / 60;
        if (horas < 24) {
            return horas + "h " + (minutos % 60) + "min";
        }
        return (horas / 24) + "d " + (horas % 24) + "h";
    }
}
