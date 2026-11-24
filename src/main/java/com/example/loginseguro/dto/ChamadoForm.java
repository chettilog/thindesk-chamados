package com.example.loginseguro.dto;

import com.example.loginseguro.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados do formulário "Abrir chamado" (tela web).
 * O solicitante não vem do formulário: é sempre o usuário logado.
 */
public class ChamadoForm {

    @NotBlank(message = "Informe um título")
    @Size(max = 120, message = "O título pode ter no máximo 120 caracteres")
    private String titulo;

    @NotBlank(message = "Descreva o problema")
    @Size(max = 2000, message = "A descrição pode ter no máximo 2000 caracteres")
    private String descricao;

    @NotNull(message = "Escolha a prioridade")
    private Prioridade prioridade = Prioridade.MEDIA;

    public ChamadoRequest toRequest() {
        return new ChamadoRequest(titulo, descricao, prioridade);
    }

    // ===== Getters e Setters =====

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
}
