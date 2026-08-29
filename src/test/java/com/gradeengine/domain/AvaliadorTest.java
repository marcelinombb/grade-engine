package com.gradeengine.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AvaliadorTest {

    private static ContextoDeCalculo contextoCom(Object[]... pares) {
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        for (Object[] par : pares) {
            contexto.adicionar(new Avaliacao((String) par[0], (Double) par[1]));
        }
        return contexto;
    }

    @Test
    void avaliaNumero() throws ErroDeCalculo {
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        assertEquals(7.0, Avaliador.avaliar(new Expr.Numero(7.0), contexto));
    }

    @Test
    void avaliaVariavelExistente() throws ErroDeCalculo {
        ContextoDeCalculo contexto = contextoCom(new Object[]{"P1", 8.5});
        assertEquals(8.5, Avaliador.avaliar(new Expr.Variavel("P1"), contexto));
    }

    @Test
    void avaliaVariavelInexistenteRetornaErro() {
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        var erro = assertThrows(ErroDeCalculo.VariavelInexistente.class,
            () -> Avaliador.avaliar(new Expr.Variavel("P1"), contexto));
        assertEquals("P1", erro.variavel());
    }

    @Test
    void avaliaSoma() throws ErroDeCalculo {
        ContextoDeCalculo contexto = contextoCom(new Object[]{"P1", 3.0}, new Object[]{"P2", 4.0});
        Expr expr = new Expr.Soma(new Expr.Variavel("P1"), new Expr.Variavel("P2"));

        assertEquals(7.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaSubtracao() throws ErroDeCalculo {
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        Expr expr = new Expr.Subtracao(new Expr.Numero(10.0), new Expr.Numero(4.0));

        assertEquals(6.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaMultiplicacao() throws ErroDeCalculo {
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        Expr expr = new Expr.Multiplicacao(new Expr.Numero(3.0), new Expr.Numero(5.0));

        assertEquals(15.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaDivisao() throws ErroDeCalculo {
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        Expr expr = new Expr.Divisao(new Expr.Numero(10.0), new Expr.Numero(2.0));

        assertEquals(5.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaDivisaoPorZeroRetornaErro() {
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        Expr expr = new Expr.Divisao(new Expr.Numero(10.0), new Expr.Numero(0.0));

        assertThrows(ErroDeCalculo.DivisaoPorZero.class, () -> Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaMedia() throws ErroDeCalculo {
        ContextoDeCalculo contexto = contextoCom(new Object[]{"P1", 6.0}, new Object[]{"P2", 10.0});
        Expr expr = new Expr.Funcao("media", List.of(new Expr.Variavel("P1"), new Expr.Variavel("P2")));

        assertEquals(8.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaMinComDoisArgumentos() throws ErroDeCalculo {
        ContextoDeCalculo contexto = contextoCom(new Object[]{"P1", 6.0}, new Object[]{"P2", 10.0});
        Expr expr = new Expr.Funcao("min", List.of(new Expr.Variavel("P1"), new Expr.Variavel("P2")));

        assertEquals(6.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaMaxComDoisArgumentos() throws ErroDeCalculo {
        ContextoDeCalculo contexto = contextoCom(new Object[]{"P1", 6.0}, new Object[]{"P2", 10.0});
        Expr expr = new Expr.Funcao("max", List.of(new Expr.Variavel("P1"), new Expr.Variavel("P2")));

        assertEquals(10.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaMinComVariosArgumentosNegativosERepetidos() throws ErroDeCalculo {
        Expr expr = new Expr.Funcao("min", List.of(
            new Expr.Numero(-3.0), new Expr.Numero(-3.0), new Expr.Numero(-10.0), new Expr.Numero(5.0)
        ));
        ContextoDeCalculo contexto = new ContextoDeCalculo();

        assertEquals(-10.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaMaxComVariosArgumentosNegativosERepetidos() throws ErroDeCalculo {
        Expr expr = new Expr.Funcao("max", List.of(
            new Expr.Numero(-3.0), new Expr.Numero(-3.0), new Expr.Numero(-10.0), new Expr.Numero(5.0)
        ));
        ContextoDeCalculo contexto = new ContextoDeCalculo();

        assertEquals(5.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaFuncaoInvalidaRetornaErro() {
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        Expr expr = new Expr.Funcao("desconhecida", List.of());

        assertThrows(ErroDeCalculo.FuncaoInvalida.class, () -> Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaIfCondicaoVerdadeira() throws ErroDeCalculo {
        ContextoDeCalculo contexto = contextoCom(new Object[]{"P1", 8.0});
        Expr expr = new Expr.Funcao("if", List.of(
            new Expr.Maior(new Expr.Variavel("P1"), new Expr.Numero(6.0)),
            new Expr.Numero(10.0),
            new Expr.Numero(2.0)
        ));

        assertEquals(10.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaIfCondicaoFalsa() throws ErroDeCalculo {
        ContextoDeCalculo contexto = contextoCom(new Object[]{"P1", 4.0});
        Expr expr = new Expr.Funcao("if", List.of(
            new Expr.Maior(new Expr.Variavel("P1"), new Expr.Numero(6.0)),
            new Expr.Numero(10.0),
            new Expr.Numero(2.0)
        ));

        assertEquals(2.0, Avaliador.avaliar(expr, contexto));
    }

    @Test
    void avaliaTodosOperadoresDeComparacao() throws ErroDeCalculo {
        ContextoDeCalculo contexto = new ContextoDeCalculo();

        record Caso(Expr comparacao, boolean esperado) {}

        List<Caso> casos = List.of(
            new Caso(new Expr.Maior(new Expr.Numero(5.0), new Expr.Numero(3.0)), true),
            new Caso(new Expr.Maior(new Expr.Numero(3.0), new Expr.Numero(5.0)), false),
            new Caso(new Expr.Menor(new Expr.Numero(3.0), new Expr.Numero(5.0)), true),
            new Caso(new Expr.Menor(new Expr.Numero(5.0), new Expr.Numero(3.0)), false),
            new Caso(new Expr.MaiorIgual(new Expr.Numero(5.0), new Expr.Numero(5.0)), true),
            new Caso(new Expr.MaiorIgual(new Expr.Numero(3.0), new Expr.Numero(5.0)), false),
            new Caso(new Expr.MenorIgual(new Expr.Numero(5.0), new Expr.Numero(5.0)), true),
            new Caso(new Expr.MenorIgual(new Expr.Numero(6.0), new Expr.Numero(5.0)), false),
            new Caso(new Expr.Igual(new Expr.Numero(5.0), new Expr.Numero(5.0)), true),
            new Caso(new Expr.Igual(new Expr.Numero(5.0), new Expr.Numero(6.0)), false),
            new Caso(new Expr.Diferente(new Expr.Numero(5.0), new Expr.Numero(6.0)), true),
            new Caso(new Expr.Diferente(new Expr.Numero(5.0), new Expr.Numero(5.0)), false)
        );

        for (Caso caso : casos) {
            Expr expr = new Expr.Funcao("if", List.of(caso.comparacao(), new Expr.Numero(1.0), new Expr.Numero(0.0)));
            double resultado = Avaliador.avaliar(expr, contexto);
            assertEquals(caso.esperado() ? 1.0 : 0.0, resultado);
        }
    }
}
