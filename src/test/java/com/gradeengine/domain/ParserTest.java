package com.gradeengine.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void funcaoMedia() throws ErroDeParse {
        // media(P1, 3.0)
        List<Token> tokens = List.of(
            new Token.Ident("media"),
            Token.LEFT_PAREN,
            new Token.Ident("P1"),
            Token.COMMA,
            new Token.Number(3.0),
            Token.RIGHT_PAREN
        );

        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Funcao("media", List.of(new Expr.Variavel("P1"), new Expr.Numero(3.0)));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void funcaoIf2() throws ErroDeParse {
        // if(P1 > P2 + P3, 10, 2)
        List<Token> tokens = List.of(
            new Token.Ident("if"),
            Token.LEFT_PAREN,
            new Token.Ident("P1"),
            Token.GREATER,
            new Token.Ident("P2"),
            Token.PLUS,
            new Token.Ident("P3"),
            Token.COMMA,
            new Token.Number(10.0),
            Token.COMMA,
            new Token.Number(2.0),
            Token.RIGHT_PAREN
        );

        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Funcao("if", List.of(
            new Expr.Maior(new Expr.Variavel("P1"), new Expr.Soma(new Expr.Variavel("P2"), new Expr.Variavel("P3"))),
            new Expr.Numero(10.0),
            new Expr.Numero(2.0)
        ));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void funcaoMax() throws ErroDeParse {
        List<Token> tokens = List.of(
            new Token.Ident("max"),
            Token.LEFT_PAREN,
            new Token.Ident("P1"),
            Token.COMMA,
            new Token.Ident("P2"),
            Token.COMMA,
            new Token.Ident("P3"),
            Token.RIGHT_PAREN
        );

        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Funcao("max", List.of(
            new Expr.Variavel("P1"), new Expr.Variavel("P2"), new Expr.Variavel("P3")
        ));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void funcaoMinSemArgumentosRetornaErro() {
        List<Token> tokens = List.of(new Token.Ident("min"), Token.LEFT_PAREN, Token.RIGHT_PAREN);
        Parser parser = new Parser(tokens);

        assertThrows(ErroDeParse.QuantidadeDeArgumentosInvalida.class, parser::parse);
    }

    @Test
    void comparacaoMaior() throws ErroDeParse {
        List<Token> tokens = List.of(new Token.Ident("P1"), Token.GREATER, new Token.Ident("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Maior(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoMenor() throws ErroDeParse {
        List<Token> tokens = List.of(new Token.Ident("P1"), Token.LESS, new Token.Ident("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Menor(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoMaiorIgual() throws ErroDeParse {
        List<Token> tokens = List.of(new Token.Ident("P1"), Token.GREATER_EQ, new Token.Ident("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.MaiorIgual(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoMenorIgual() throws ErroDeParse {
        List<Token> tokens = List.of(new Token.Ident("P1"), Token.LESS_EQ, new Token.Ident("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.MenorIgual(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoIgual() throws ErroDeParse {
        List<Token> tokens = List.of(new Token.Ident("P1"), Token.EQUAL, new Token.Ident("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Igual(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoDiferente() throws ErroDeParse {
        List<Token> tokens = List.of(new Token.Ident("P1"), Token.NOT_EQUAL, new Token.Ident("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Diferente(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void deveRetornarSoma() throws ErroDeParse {
        List<Token> tokens = List.of(
            new Token.Ident("P1"), Token.PLUS, new Token.Number(3.0), Token.STAR, new Token.Number(5.0)
        );

        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Soma(
            new Expr.Variavel("P1"),
            new Expr.Multiplicacao(new Expr.Numero(3.0), new Expr.Numero(5.0))
        );

        assertEquals(esperado, parser.parse());
    }

    @Test
    void deveRetornarTokenInesperado() {
        List<Token> tokens = List.of(Token.PLUS);
        Parser parser = new Parser(tokens);

        assertThrows(ErroDeParse.ExpressaoInvalida.class, parser::parse);
    }

    @Test
    void deveRetornarTokenInesperado2() {
        List<Token> tokens = List.of(
            new Token.Ident("P1"), new Token.Number(3.0), Token.PLUS, Token.STAR, new Token.Number(5.0)
        );
        Parser parser = new Parser(tokens);

        assertThrows(ErroDeParse.ExpressaoInvalida.class, parser::parse);
    }

    @Test
    void deveRetornarSomaComParentesesEOperadorExterno() throws ErroDeParse {
        // (P1 + P2) * P3
        List<Token> tokens = List.of(
            Token.LEFT_PAREN,
            new Token.Ident("P1"),
            Token.PLUS,
            new Token.Ident("P2"),
            Token.RIGHT_PAREN,
            Token.STAR,
            new Token.Ident("P3")
        );

        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Multiplicacao(
            new Expr.Soma(new Expr.Variavel("P1"), new Expr.Variavel("P2")),
            new Expr.Variavel("P3")
        );

        assertEquals(esperado, parser.parse());
    }

    @Test
    void deveRetornarSomaComParentesesAninhados() throws ErroDeParse {
        // ((P1 + P2))
        List<Token> tokens = List.of(
            Token.LEFT_PAREN,
            Token.LEFT_PAREN,
            new Token.Ident("P1"),
            Token.PLUS,
            new Token.Ident("P2"),
            Token.RIGHT_PAREN,
            Token.RIGHT_PAREN
        );

        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Soma(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void deveRetornarErroDeProfundidadeExcedidaParaParentesesDemaisAninhados() {
        // ((((...(P1)...)))) com mais níveis do que o parser aceita --
        // sem o limite, isso estouraria a pilha (StackOverflowError) em vez
        // de falhar com um erro tratável.
        int niveis = 10_000;
        List<Token> tokens = new java.util.ArrayList<>();
        for (int i = 0; i < niveis; i++) {
            tokens.add(Token.LEFT_PAREN);
        }
        tokens.add(new Token.Ident("P1"));
        for (int i = 0; i < niveis; i++) {
            tokens.add(Token.RIGHT_PAREN);
        }

        Parser parser = new Parser(tokens);

        assertThrows(ErroDeParse.ProfundidadeExcedida.class, parser::parse);
    }
}
