package com.example.loginseguro.dto;

import com.example.loginseguro.model.Chamado;

import java.util.List;

/**
 * Números e gráficos exibidos no dashboard.
 */
public class DashboardResumo {

    private long total;
    private long abertos;
    private long emAndamento;
    private long resolvidos;
    private long altaPendentes;
    private int taxaResolucao;
    private String tempoMedioResolucao;
    private List<Barra> porStatus;
    private List<Barra> porPrioridade;
    private List<Barra> ultimosDias;
    private List<Barra> porTecnico;
    private List<Chamado> recentes;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getAbertos() { return abertos; }
    public void setAbertos(long abertos) { this.abertos = abertos; }

    public long getEmAndamento() { return emAndamento; }
    public void setEmAndamento(long emAndamento) { this.emAndamento = emAndamento; }

    public long getResolvidos() { return resolvidos; }
    public void setResolvidos(long resolvidos) { this.resolvidos = resolvidos; }

    public long getAltaPendentes() { return altaPendentes; }
    public void setAltaPendentes(long altaPendentes) { this.altaPendentes = altaPendentes; }

    public int getTaxaResolucao() { return taxaResolucao; }
    public void setTaxaResolucao(int taxaResolucao) { this.taxaResolucao = taxaResolucao; }

    public String getTempoMedioResolucao() { return tempoMedioResolucao; }
    public void setTempoMedioResolucao(String tempoMedioResolucao) { this.tempoMedioResolucao = tempoMedioResolucao; }

    public List<Barra> getPorStatus() { return porStatus; }
    public void setPorStatus(List<Barra> porStatus) { this.porStatus = porStatus; }

    public List<Barra> getPorPrioridade() { return porPrioridade; }
    public void setPorPrioridade(List<Barra> porPrioridade) { this.porPrioridade = porPrioridade; }

    public List<Barra> getUltimosDias() { return ultimosDias; }
    public void setUltimosDias(List<Barra> ultimosDias) { this.ultimosDias = ultimosDias; }

    public List<Barra> getPorTecnico() { return porTecnico; }
    public void setPorTecnico(List<Barra> porTecnico) { this.porTecnico = porTecnico; }

    public List<Chamado> getRecentes() { return recentes; }
    public void setRecentes(List<Chamado> recentes) { this.recentes = recentes; }
}
