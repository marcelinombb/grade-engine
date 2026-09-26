package com.gradeengine;

public sealed class ErroDeParse extends Exception {

    private ErroDeParse(String mensagem) {
        super(mensagem);
    }

    public static final class ParenteseNaoFechado extends ErroDeParse {
        public ParenteseNaoFechado() {
            super("Parêntese não fechado na fórmula");
        }
    }

    public static final class ExpressaoInvalida extends ErroDeParse {
        public ExpressaoInvalida() {
            super("Expressão inválida na fórmula");
        }
    }

    public static final class FuncaoInvalida extends ErroDeParse {
        public FuncaoInvalida(String nome) {
            super("A função '" + nome + "' não existe");
        }
    }

    public static final class QuantidadeDeArgumentosInvalida extends ErroDeParse {
        public QuantidadeDeArgumentosInvalida(String mensagem) {
            super(mensagem);
        }
    }

    public static final class ArgumentoInvalido extends ErroDeParse {
        public ArgumentoInvalido(String mensagem) {
            super(mensagem);
        }
    }

    public static final class ProfundidadeExcedida extends ErroDeParse {
        public ProfundidadeExcedida(int limite) {
            super("Fórmula excede a profundidade máxima permitida (" + limite + " níveis de aninhamento)");
        }
    }

    public static final class VariavelNaoDeclarada extends ErroDeParse {
        public VariavelNaoDeclarada(String mensagem) {
            super(mensagem);
        }
    }
}
