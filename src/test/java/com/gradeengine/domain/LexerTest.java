package com.gradeengine.domain;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LexerTest {

    @Test
    void deveRetornarEmptyQuandoSoTemEspaco() throws ErroLexico {
        Lexer lexer = new Lexer(" ");
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    @Test
    void deveRetornarTokenP1() throws ErroLexico {
        Lexer lexer = new Lexer("P1 + 7.5");
        assertEquals(Optional.of(new Token.Ident("P1")), lexer.nextToken());
    }

    @Test
    void deveRetornarTokenPlus() throws ErroLexico {
        Lexer lexer = new Lexer("P1 + 7.5");
        lexer.nextToken();
        assertEquals(Optional.of(Token.PLUS), lexer.nextToken());
    }

    @Test
    void deveRetornarTokenNumber() throws ErroLexico {
        Lexer lexer = new Lexer("P1 + 7.5");
        lexer.nextToken();
        lexer.nextToken();
        assertEquals(Optional.of(new Token.Number(7.5)), lexer.nextToken());
    }

    @Test
    void deveRetornarErroCaractereInvalido() {
        Lexer lexer = new Lexer("@");
        assertThrows(ErroLexico.CaractereInvalido.class, lexer::nextToken);
    }

    @Test
    void deveRetornarErroMultiplosPontos() {
        Lexer lexer = new Lexer("7.5.3");
        assertThrows(ErroLexico.MultiplosPontos.class, lexer::nextToken);
    }

    @Test
    void deveRetornarTokenNumberComMultiplosDigitos() throws ErroLexico {
        Lexer lexer = new Lexer("12.75");
        assertEquals(Optional.of(new Token.Number(12.75)), lexer.nextToken());
    }

    @Test
    void deveIgnorarEspacosIrregularesEntreTokens() throws ErroLexico {
        Lexer lexer = new Lexer("  P1   +  7.5  ");
        assertEquals(Optional.of(new Token.Ident("P1")), lexer.nextToken());
        assertEquals(Optional.of(Token.PLUS), lexer.nextToken());
        assertEquals(Optional.of(new Token.Number(7.5)), lexer.nextToken());
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    @Test
    void deveRetornarTokenGreater() throws ErroLexico {
        Lexer lexer = new Lexer(">");
        assertEquals(Optional.of(Token.GREATER), lexer.nextToken());
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    @Test
    void deveRetornarTokenLess() throws ErroLexico {
        Lexer lexer = new Lexer("<");
        assertEquals(Optional.of(Token.LESS), lexer.nextToken());
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    @Test
    void deveRetornarTokenGreaterEq() throws ErroLexico {
        Lexer lexer = new Lexer(">=");
        assertEquals(Optional.of(Token.GREATER_EQ), lexer.nextToken());
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    @Test
    void deveRetornarTokenLessEq() throws ErroLexico {
        Lexer lexer = new Lexer("<=");
        assertEquals(Optional.of(Token.LESS_EQ), lexer.nextToken());
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    // Decisão de design intencional (portada do Rust): igualdade e diferença
    // usam um único caractere ("=" e "!"), não os pares "==" / "!=".
    @Test
    void deveRetornarTokenEqualComUmUnicoCaractere() throws ErroLexico {
        Lexer lexer = new Lexer("=");
        assertEquals(Optional.of(Token.EQUAL), lexer.nextToken());
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    @Test
    void deveRetornarTokenNotEqualComUmUnicoCaractere() throws ErroLexico {
        Lexer lexer = new Lexer("!");
        assertEquals(Optional.of(Token.NOT_EQUAL), lexer.nextToken());
        assertEquals(Optional.empty(), lexer.nextToken());
    }

    @Test
    void deveRetornarTokensDeComparacaoEmUmaExpressaoCompleta() throws ErroLexico {
        Lexer lexer = new Lexer("P1 > P2");
        assertEquals(Optional.of(new Token.Ident("P1")), lexer.nextToken());
        assertEquals(Optional.of(Token.GREATER), lexer.nextToken());
        assertEquals(Optional.of(new Token.Ident("P2")), lexer.nextToken());
        assertEquals(Optional.empty(), lexer.nextToken());
    }
}
