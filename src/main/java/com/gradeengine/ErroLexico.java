package com.gradeengine;

public sealed class ErroLexico extends Exception {

    private ErroLexico(String mensagem) {
        super(mensagem);
    }

    public static final class CaractereInvalido extends ErroLexico {
        public CaractereInvalido(String caractere) {
            super("Caractere inválido na fórmula: '" + caractere + "'");
        }
    }

    public static final class MultiplosPontos extends ErroLexico {
        public MultiplosPontos(String mensagem) {
            super(mensagem);
        }
    }

    public static final class NumeroInvalido extends ErroLexico {
        public NumeroInvalido(String mensagem) {
            super("Número inválido na fórmula: " + mensagem);
        }
    }
}
