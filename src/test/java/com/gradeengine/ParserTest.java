package com.gradeengine;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

    @Test
    void funcaoMedia() throws ErroDeParse {
        // media(P1, 3.0)
        List<Token> tokens = List.of(
            Token.identificador("media"),
            Token.LEFT_PAREN,
            Token.identificador("P1"),
            Token.COMMA,
            Token.numero(3.0),
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
            Token.identificador("if"),
            Token.LEFT_PAREN,
            Token.identificador("P1"),
            Token.GREATER,
            Token.identificador("P2"),
            Token.PLUS,
            Token.identificador("P3"),
            Token.COMMA,
            Token.numero(10.0),
            Token.COMMA,
            Token.numero(2.0),
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
            Token.identificador("max"),
            Token.LEFT_PAREN,
            Token.identificador("P1"),
            Token.COMMA,
            Token.identificador("P2"),
            Token.COMMA,
            Token.identificador("P3"),
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
        List<Token> tokens = List.of(Token.identificador("min"), Token.LEFT_PAREN, Token.RIGHT_PAREN);
        Parser parser = new Parser(tokens);

        assertThrows(ErroDeParse.QuantidadeDeArgumentosInvalida.class, parser::parse);
    }

    @Test
    void comparacaoMaior() throws ErroDeParse {
        List<Token> tokens = List.of(Token.identificador("P1"), Token.GREATER, Token.identificador("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Maior(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoMenor() throws ErroDeParse {
        List<Token> tokens = List.of(Token.identificador("P1"), Token.LESS, Token.identificador("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Menor(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoMaiorIgual() throws ErroDeParse {
        List<Token> tokens = List.of(Token.identificador("P1"), Token.GREATER_EQ, Token.identificador("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.MaiorIgual(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoMenorIgual() throws ErroDeParse {
        List<Token> tokens = List.of(Token.identificador("P1"), Token.LESS_EQ, Token.identificador("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.MenorIgual(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoIgual() throws ErroDeParse {
        List<Token> tokens = List.of(Token.identificador("P1"), Token.EQUAL, Token.identificador("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Igual(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void comparacaoDiferente() throws ErroDeParse {
        List<Token> tokens = List.of(Token.identificador("P1"), Token.NOT_EQUAL, Token.identificador("P2"));
        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Diferente(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void deveRetornarSoma() throws ErroDeParse {
        List<Token> tokens = List.of(
            Token.identificador("P1"), Token.PLUS, Token.numero(3.0), Token.STAR, Token.numero(5.0)
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
            Token.identificador("P1"), Token.numero(3.0), Token.PLUS, Token.STAR, Token.numero(5.0)
        );
        Parser parser = new Parser(tokens);

        assertThrows(ErroDeParse.ExpressaoInvalida.class, parser::parse);
    }

    @Test
    void deveRetornarSomaComParentesesEOperadorExterno() throws ErroDeParse {
        // (P1 + P2) * P3
        List<Token> tokens = List.of(
            Token.LEFT_PAREN,
            Token.identificador("P1"),
            Token.PLUS,
            Token.identificador("P2"),
            Token.RIGHT_PAREN,
            Token.STAR,
            Token.identificador("P3")
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
            Token.identificador("P1"),
            Token.PLUS,
            Token.identificador("P2"),
            Token.RIGHT_PAREN,
            Token.RIGHT_PAREN
        );

        Parser parser = new Parser(tokens);

        Expr esperado = new Expr.Soma(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(esperado, parser.parse());
    }

    @Test
    void deveRetornarErroDeProfundidadeExcedidaParaParentesesDemaisAninhados() {
        // ((((...(P1)...)))) com mais níveis do que o parser aceita
        int niveis = 10_000;
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < niveis; i++) {
            tokens.add(Token.LEFT_PAREN);
        }
        tokens.add(Token.identificador("P1"));
        for (int i = 0; i < niveis; i++) {
            tokens.add(Token.RIGHT_PAREN);
        }

        Parser parser = new Parser(tokens);

        assertThrows(ErroDeParse.ProfundidadeExcedida.class, parser::parse);
    }
}
