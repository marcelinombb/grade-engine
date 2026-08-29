package com.gradeengine.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parser recursivo descendente (recursive descent) para a gramática de
 * fórmulas de cálculo de notas.
 *
 * A gramática é organizada em camadas, da mais "solta" para a mais
 * "apertada", cada uma delegando para a próxima antes de tentar resolver
 * seu próprio nível de precedência:
 *
 * comparacao := expressao (('>' | '<' | '>=' | '<=' | '==' | '!=') expressao)?
 * expressao  := termo (('+' | '-') termo)*
 * termo      := fator (('*' | '/') fator)*
 * fator      := NUMBER | IDENT | IDENT '(' (comparacao (',' comparacao)*)? ')' | '(' comparacao ')'
 */
public final class Parser {

    /**
     * Limite de níveis de aninhamento (parênteses ou argumentos de função)
     * que o parser desce antes de recusar a fórmula. Sem isso, uma fórmula
     * com milhares de parênteses aninhados (entrada de usuário não
     * confiável) estouraria a pilha de chamadas Java (StackOverflowError)
     * em vez de falhar com um erro tratável.
     */
    private static final int PROFUNDIDADE_MAXIMA = 200;

    private final List<Token> tokens;
    private int cursor = 0;
    private int profundidadeAtual = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() throws ErroDeParse {
        Expr expr = comparacao();
        if (aindaFaltamTokens()) {
            throw new ErroDeParse.ExpressaoInvalida();
        }
        return expr;
    }

    private Expr fator() throws ErroDeParse {
        profundidadeAtual++;
        if (profundidadeAtual > PROFUNDIDADE_MAXIMA) {
            throw new ErroDeParse.ProfundidadeExcedida(PROFUNDIDADE_MAXIMA);
        }
        try {
            return fatorSemLimite();
        } finally {
            profundidadeAtual--;
        }
    }

    private Expr fatorSemLimite() throws ErroDeParse {
        Optional<Token> atual = peek();

        if (atual.isPresent() && atual.get() instanceof Token.Number numero) {
            advance();
            return new Expr.Numero(numero.value());
        }

        if (atual.isPresent() && atual.get() instanceof Token.Ident ident) {
            advance();
            String nome = ident.name();
            Optional<Token> proximo = peek();

            if (proximo.isPresent() && proximo.get() instanceof Token.LeftParen) {
                return switch (nome) {
                    case "if" -> parseIf(nome);
                    case "media" -> parseMedia(nome);
                    case "min", "max" -> parseMinMax(nome);
                    default -> throw new ErroDeParse.FuncaoInvalida(nome);
                };
            }

            return new Expr.Variavel(nome);
        }

        if (atual.isPresent() && atual.get() instanceof Token.LeftParen) {
            advance();
            Expr expr = comparacao();
            Optional<Token> fechamento = peek();
            if (fechamento.isPresent() && fechamento.get() instanceof Token.RightParen) {
                advance();
                return expr;
            }
            throw new ErroDeParse.ParenteseNaoFechado();
        }

        throw new ErroDeParse.ExpressaoInvalida();
    }

    private Expr expressao() throws ErroDeParse {
        Expr esquerda = termo();

        while (true) {
            Optional<Token> atual = peek();
            if (atual.isPresent() && atual.get() instanceof Token.Plus) {
                advance();
                Expr direita = termo();
                esquerda = new Expr.Soma(esquerda, direita);
            } else if (atual.isPresent() && atual.get() instanceof Token.Minus) {
                advance();
                Expr direita = termo();
                esquerda = new Expr.Subtracao(esquerda, direita);
            } else {
                break;
            }
        }

        return esquerda;
    }

    private Expr comparacao() throws ErroDeParse {
        Expr esquerda = expressao();
        Optional<Token> atual = peek();

        if (atual.isEmpty()) {
            return esquerda;
        }

        return switch (atual.get()) {
            case Token.Greater g -> {
                advance();
                yield new Expr.Maior(esquerda, expressao());
            }
            case Token.Less l -> {
                advance();
                yield new Expr.Menor(esquerda, expressao());
            }
            case Token.GreaterEq g -> {
                advance();
                yield new Expr.MaiorIgual(esquerda, expressao());
            }
            case Token.LessEq l -> {
                advance();
                yield new Expr.MenorIgual(esquerda, expressao());
            }
            case Token.Equal e -> {
                advance();
                yield new Expr.Igual(esquerda, expressao());
            }
            case Token.NotEqual n -> {
                advance();
                yield new Expr.Diferente(esquerda, expressao());
            }
            default -> esquerda;
        };
    }

    private Expr termo() throws ErroDeParse {
        Expr esquerda = fator();

        while (true) {
            Optional<Token> atual = peek();
            if (atual.isPresent() && atual.get() instanceof Token.Star) {
                advance();
                Expr direita = fator();
                esquerda = new Expr.Multiplicacao(esquerda, direita);
            } else if (atual.isPresent() && atual.get() instanceof Token.Slash) {
                advance();
                Expr direita = fator();
                esquerda = new Expr.Divisao(esquerda, direita);
            } else {
                break;
            }
        }

        return esquerda;
    }

    boolean aindaFaltamTokens() {
        return cursor < tokens.size();
    }

    private Expr.Funcao consumeFunction(String nome) throws ErroDeParse {
        List<Expr> params = new ArrayList<>();

        advance(); // consome o '(' -- fator() só espiou, não consumiu

        Optional<Token> atual = peek();
        if (atual.isPresent() && atual.get() instanceof Token.RightParen) {
            advance();
            return new Expr.Funcao(nome, params);
        }

        while (true) {
            params.add(comparacao());

            Optional<Token> separador = peek();
            if (separador.isPresent() && separador.get() instanceof Token.Comma) {
                advance();
                continue;
            }
            if (separador.isPresent() && separador.get() instanceof Token.RightParen) {
                advance();
                break;
            }
            throw new ErroDeParse.ParenteseNaoFechado();
        }

        return new Expr.Funcao(nome, params);
    }

    /**
     * Consome uma chamada de função e valida que ela tem pelo menos
     * `minimo` argumentos. Devolve a Expr.Funcao original e seus
     * argumentos já desembrulhados, concentrando a única checagem
     * de tipo (instanceof Expr.Funcao) aqui -- os chamadores
     * (parseIf/parseMedia/parseMinMax) não precisam repetir isso.
     */
    private record ChamadaValidada(Expr.Funcao chamada, List<Expr> args) {}

    private ChamadaValidada parseChamadaComAridadeMinima(String nome, int minimo) throws ErroDeParse {
        Expr.Funcao chamada = consumeFunction(nome);
        List<Expr> args = chamada.args();

        if (args.size() < minimo) {
            throw new ErroDeParse.QuantidadeDeArgumentosInvalida(
                args.size() + " argumentos encontrados, onde no mínimo " + minimo
                    + " eram esperados para " + nome + "(...)."
            );
        }

        return new ChamadaValidada(chamada, args);
    }

    private Expr parseIf(String nome) throws ErroDeParse {
        ChamadaValidada resultado = parseChamadaComAridadeMinima(nome, 3);

        if (!Expr.isComparacao(resultado.args().get(0))) {
            throw new ErroDeParse.ArgumentoInvalido("O primeiro argumento deve ser uma comparação");
        }

        return resultado.chamada();
    }

    private Expr parseMedia(String nome) throws ErroDeParse {
        return parseChamadaComAridadeMinima(nome, 1).chamada();
    }

    private Expr parseMinMax(String nome) throws ErroDeParse {
        return parseChamadaComAridadeMinima(nome, 1).chamada();
    }

    private Optional<Token> peek() {
        if (cursor >= tokens.size()) {
            return Optional.empty();
        }
        return Optional.of(tokens.get(cursor));
    }

    private Optional<Token> advance() {
        Optional<Token> token = peek();
        cursor += 1;
        return token;
    }
}
