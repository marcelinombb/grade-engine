package com.gradeengine;

import com.gradeengine.domain.Avaliacao;
import com.gradeengine.domain.ContextoDeCalculo;
import com.gradeengine.domain.ErroDeCalculo;
import com.gradeengine.domain.ErroDeParse;
import com.gradeengine.domain.ErroDoMotor;
import com.gradeengine.domain.ErroLexico;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradeEngineTest {

    private static ContextoDeCalculo contextoCom(String codigo, double nota) {
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        contexto.adicionar(new Avaliacao(codigo, nota));
        return contexto;
    }

    @Test
    void avaliaFormulaSimples() throws ErroDoMotor {
        GradeEngine engine = new GradeEngine();
        ContextoDeCalculo contexto = contextoCom("P1", 8.0);

        assertEquals(8.0, engine.avaliar("P1", contexto));
    }

    @Test
    void avaliaFuncaoMedia() throws ErroDoMotor {
        GradeEngine engine = new GradeEngine();
        ContextoDeCalculo contexto = new ContextoDeCalculo();
        contexto.adicionar(new Avaliacao("P1", 6.0));
        contexto.adicionar(new Avaliacao("P2", 10.0));

        assertEquals(8.0, engine.avaliar("media(P1, P2)", contexto));
    }

    @Test
    void validarRetornaDescricaoSemPrecisarDeContexto() throws ErroDoMotor {
        GradeEngine engine = new GradeEngine();

        assertEquals("Variável: P1 + Número: 3.0", engine.validar("P1 + 3"));
    }

    @Test
    void erroLexicoPropagaComCausaCorreta() {
        GradeEngine engine = new GradeEngine();
        ContextoDeCalculo contexto = new ContextoDeCalculo();

        ErroDoMotor erro = assertThrows(ErroDoMotor.class, () -> engine.avaliar("@", contexto));
        assertInstanceOf(ErroLexico.CaractereInvalido.class, erro.getCause());
    }

    @Test
    void erroDeParsePropagaComCausaCorreta() {
        GradeEngine engine = new GradeEngine();
        ContextoDeCalculo contexto = new ContextoDeCalculo();

        ErroDoMotor erro = assertThrows(ErroDoMotor.class, () -> engine.avaliar("P1 +", contexto));
        assertInstanceOf(ErroDeParse.ExpressaoInvalida.class, erro.getCause());
    }

    @Test
    void erroDeCalculoPropagaComCausaCorreta() {
        GradeEngine engine = new GradeEngine();
        ContextoDeCalculo contexto = new ContextoDeCalculo();

        ErroDoMotor erro = assertThrows(ErroDoMotor.class, () -> engine.avaliar("P1 / 0", contexto));
        assertInstanceOf(ErroDeCalculo.DivisaoPorZero.class, erro.getCause());
    }

    @Test
    void reutilizaAstEmCacheParaFormulaRepetida() throws ErroDoMotor {
        GradeEngine engine = new GradeEngine();
        String formula = "media(P1, P2)";

        ContextoDeCalculo contextoAluno1 = new ContextoDeCalculo();
        contextoAluno1.adicionar(new Avaliacao("P1", 5.0));
        contextoAluno1.adicionar(new Avaliacao("P2", 7.0));

        ContextoDeCalculo contextoAluno2 = new ContextoDeCalculo();
        contextoAluno2.adicionar(new Avaliacao("P1", 9.0));
        contextoAluno2.adicionar(new Avaliacao("P2", 9.0));

        engine.avaliar(formula, contextoAluno1);
        assertEquals(1, engine.tamanhoCache());

        engine.avaliar(formula, contextoAluno2);
        assertEquals(1, engine.tamanhoCache(), "reavaliar a mesma fórmula não deve criar nova entrada no cache");
    }

    @Test
    void formulasDiferentesGeramEntradasDeCacheSeparadas() throws ErroDoMotor {
        GradeEngine engine = new GradeEngine();
        ContextoDeCalculo contexto = contextoCom("P1", 5.0);

        engine.avaliar("P1", contexto);
        engine.avaliar("P1 + 1", contexto);

        assertEquals(2, engine.tamanhoCache());
    }

    @Test
    void validarFormulaRetornaValidaSemPrecisarDeTryCatch() {
        GradeEngine engine = new GradeEngine();

        ResultadoValidacao resultado = engine.validarFormula("media(P1, P2)");

        assertTrue(resultado.valida());
        assertInstanceOf(ResultadoValidacao.Valida.class, resultado);
        assertEquals(
            "media(Variável: P1, Variável: P2)",
            ((ResultadoValidacao.Valida) resultado).descricao()
        );
    }

    @Test
    void validarFormulaRetornaInvalidaComMensagemParaErroLexico() {
        GradeEngine engine = new GradeEngine();

        ResultadoValidacao resultado = engine.validarFormula("P1 @ P2");

        assertFalse(resultado.valida());
        assertInstanceOf(ResultadoValidacao.Invalida.class, resultado);
        assertTrue(((ResultadoValidacao.Invalida) resultado).mensagemDeErro().contains("@"));
    }

    @Test
    void validarFormulaRetornaInvalidaComMensagemParaErroDeParse() {
        GradeEngine engine = new GradeEngine();

        ResultadoValidacao resultado = engine.validarFormula("min()");

        assertFalse(resultado.valida());
        assertInstanceOf(ResultadoValidacao.Invalida.class, resultado);
    }

    @Test
    void validarFormulaNaoLancaExceptionParaFormulaInvalida() {
        GradeEngine engine = new GradeEngine();

        assertDoesNotThrow(() -> engine.validarFormula("P1 +"));
    }

    @Test
    void validarFormulaReutilizaCacheDeAvaliacoesAnteriores() throws ErroDoMotor {
        GradeEngine engine = new GradeEngine();
        String formula = "media(P1, P2)";

        ContextoDeCalculo contexto = new ContextoDeCalculo();
        contexto.adicionar(new Avaliacao("P1", 5.0));
        contexto.adicionar(new Avaliacao("P2", 7.0));

        engine.avaliar(formula, contexto);
        assertEquals(1, engine.tamanhoCache());

        engine.validarFormula(formula);
        assertEquals(1, engine.tamanhoCache(), "validarFormula não deve reparsear uma fórmula já em cache");
    }
}
