package com.example.loginseguro.dto;

/**
 * Um item de gráfico de barras do dashboard (rótulo, valor e % da barra).
 */
public class Barra {

    private final String rotulo;
    private final long valor;
    private final int percentual;
    private final String classe;

    public Barra(String rotulo, long valor, long maximo, String classe) {
        this.rotulo = rotulo;
        this.valor = valor;
        this.percentual = maximo > 0 ? (int) Math.round(valor * 100.0 / maximo) : 0;
        this.classe = classe;
    }

    public String getRotulo() { return rotulo; }
    public long getValor() { return valor; }
    public int getPercentual() { return percentual; }
    public String getClasse() { return classe; }
}
