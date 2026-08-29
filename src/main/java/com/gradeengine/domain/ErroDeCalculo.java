package com.gradeengine.domain;

public sealed class ErroDeCalculo extends Exception {

    private ErroDeCalculo(String mensagem) {
        super(mensagem);
    }

    public static final class VariavelInexistente extends ErroDeCalculo {
        private final String variavel;

        public VariavelInexistente(String variavel) {
            super("A fórmula faz referência à avaliação '" + variavel + "' que não existe");
            this.variavel = variavel;
        }

        public String variavel() {
            return variavel;
        }
    }

    public static final class FuncaoInvalida extends ErroDeCalculo {
        public FuncaoInvalida() {
            super("Função inválida");
        }
    }

    public static final class DivisaoPorZero extends ErroDeCalculo {
        public DivisaoPorZero() {
            super("Divisão por zero");
        }
    }

    public static final class ImpossivelAvaliar extends ErroDeCalculo {
        public ImpossivelAvaliar() {
            super("Não foi possivel avaliar expressão");
        }
    }
}
