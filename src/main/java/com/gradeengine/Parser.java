package com.gradeengine;

import java.util.ArrayList;
import java.util.List;

final class Parser {

    private static final int PROFUNDIDADE_MAXIMA = 200;

    private final List<Token> tokens;
    private int cursor = 0;
    private int profundidadeAtual = 0;

    Parser(List<Token> tokens) {
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
        Token atual = peek();
        if (atual == null) {
            throw new ErroDeParse.ExpressaoInvalida();
        }

        if (atual.is(TipoToken.NUMERO)) {
            advance();
            return new Expr.Numero(atual.valorNumero());
        }

        if (atual.is(TipoToken.IDENTIFICADOR)) {
            advance();
            String nome = atual.texto();
            Token proximo = peek();

            if (proximo != null && proximo.is(TipoToken.PARENTESE_ESQ)) {
                return switch (nome) {
                    case "if" -> parseIf(nome);
                    case "media" -> parseMedia(nome);
                    case "min", "max" -> parseMinMax(nome);
                    default -> throw new ErroDeParse.FuncaoInvalida(nome);
                };
            }

            return new Expr.Variavel(nome);
        }

        if (atual.is(TipoToken.PARENTESE_ESQ)) {
            advance();
            Expr expr = comparacao();
            Token fechamento = peek();
            if (fechamento != null && fechamento.is(TipoToken.PARENTESE_DIR)) {
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
            Token atual = peek();
            if (atual == null) break;

            if (atual.is(TipoToken.MAIS)) {
                advance();
                esquerda = new Expr.Soma(esquerda, termo());
            } else if (atual.is(TipoToken.MENOS)) {
                advance();
                esquerda = new Expr.Subtracao(esquerda, termo());
            } else {
                break;
            }
        }

        return esquerda;
    }

    private Expr comparacao() throws ErroDeParse {
        Expr esquerda = expressao();
        Token atual = peek();

        if (atual == null) {
            return esquerda;
        }

        return switch (atual.tipo()) {
            case MAIOR -> {
                advance();
                yield new Expr.Maior(esquerda, expressao());
            }
            case MENOR -> {
                advance();
                yield new Expr.Menor(esquerda, expressao());
            }
            case MAIOR_IGUAL -> {
                advance();
                yield new Expr.MaiorIgual(esquerda, expressao());
            }
            case MENOR_IGUAL -> {
                advance();
                yield new Expr.MenorIgual(esquerda, expressao());
            }
            case IGUAL -> {
                advance();
                yield new Expr.Igual(esquerda, expressao());
            }
            case DIFERENTE -> {
                advance();
                yield new Expr.Diferente(esquerda, expressao());
            }
            default -> esquerda;
        };
    }

    private Expr termo() throws ErroDeParse {
        Expr esquerda = fator();

        while (true) {
            Token atual = peek();
            if (atual == null) break;

            if (atual.is(TipoToken.ASTERISCO)) {
                advance();
                esquerda = new Expr.Multiplicacao(esquerda, fator());
            } else if (atual.is(TipoToken.BARRA)) {
                advance();
                esquerda = new Expr.Divisao(esquerda, fator());
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
        advance(); // consome o '('

        Token atual = peek();
        if (atual != null && atual.is(TipoToken.PARENTESE_DIR)) {
            advance();
            return new Expr.Funcao(nome, params);
        }

        while (true) {
            params.add(comparacao());

            Token separador = peek();
            if (separador != null && separador.is(TipoToken.VIRGULA)) {
                advance();
                continue;
            }
            if (separador != null && separador.is(TipoToken.PARENTESE_DIR)) {
                advance();
                break;
            }
            throw new ErroDeParse.ParenteseNaoFechado();
        }

        return new Expr.Funcao(nome, params);
    }

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

    Token peek() {
        if (cursor >= tokens.size()) {
            return null;
        }
        return tokens.get(cursor);
    }

    Token advance() {
        if (cursor >= tokens.size()) {
            return null;
        }
        return tokens.get(cursor++);
    }
}
