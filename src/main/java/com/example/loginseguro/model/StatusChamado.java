package com.example.loginseguro.model;

/**
 * Ciclo de vida do chamado: ABERTO -> EM_ANDAMENTO -> RESOLVIDO.
 * (um chamado aberto também pode ser resolvido direto)
 */
public enum StatusChamado {
    ABERTO("Aberto"),
    EM_ANDAMENTO("Em andamento"),
    RESOLVIDO("Resolvido");

    private final String rotulo;

    StatusChamado(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() { return rotulo; }
}
