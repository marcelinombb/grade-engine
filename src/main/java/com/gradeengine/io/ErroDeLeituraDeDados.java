package com.gradeengine.io;

/**
 * Falha ao interpretar o arquivo/payload de entrada (CSV ou JSON) em si --
 * coluna ausente, JSON malformado, valor não numérico onde se esperava uma
 * nota. Deliberadamente separado de ErroDoMotor: aqui o problema é o
 * formato dos dados, não a fórmula nem o cálculo.
 */
public final class ErroDeLeituraDeDados extends Exception {
    public ErroDeLeituraDeDados(String mensagem) {
        super(mensagem);
    }

    public ErroDeLeituraDeDados(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
