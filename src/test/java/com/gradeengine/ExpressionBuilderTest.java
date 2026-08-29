package com.gradeengine;

import com.gradeengine.domain.ErroDeParse;
import com.gradeengine.domain.ErroDoMotor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionBuilderTest {

    @Test
    void avaliaFormulaComUmaVariavel() throws ErroDoMotor {
        // equivalente ao exemplo "3 * sin(y) - 2" do exp4j, usando as
        // operações que o motor de fato suporta.
        Expression e = new ExpressionBuilder("3 * y - 2")
            .variable("y")
            .build();

        e.setVariable("y", 4.0);

        assertEquals(10.0, e.evaluate());
    }

    @Test
    void avaliaFormulaComVariasVariaveisEFuncao() throws ErroDoMotor {
        Expression e = new ExpressionBuilder("media(P1, P2)")
            .variables("P1", "P2")
            .build();

        e.setVariable("P1", 6.0);
        e.setVariable("P2", 10.0);

        assertEquals(8.0, e.evaluate());
    }

    @Test
    void funcionaSemDeclararVariaveis() throws ErroDoMotor {
        // .variable(...) é opcional -- sem nenhuma declaração, build() não
        // valida nada, igual ao uso direto de GradeEngine.avaliar(...).
        Expression e = new ExpressionBuilder("P1 + P2").build();

        e.setVariable("P1", 3.0);
        e.setVariable("P2", 4.0);

        assertEquals(7.0, e.evaluate());
    }

    @Test
    void buildFalhaQuandoVariavelUsadaNaoFoiDeclarada() {
        ExpressionBuilder builder = new ExpressionBuilder("P1 + P2").variable("P1");

        ErroDoMotor erro = assertThrows(ErroDoMotor.class, builder::build);
        assertInstanceOf(ErroDeParse.VariavelNaoDeclarada.class, erro.getCause());
    }

    @Test
    void buildFalhaQuandoVariavelDeclaradaNaoEhUsada() {
        ExpressionBuilder builder = new ExpressionBuilder("P1 + 1").variables("P1", "P2");

        ErroDoMotor erro = assertThrows(ErroDoMotor.class, builder::build);
        assertInstanceOf(ErroDeParse.VariavelNaoDeclarada.class, erro.getCause());
    }

    @Test
    void reutilizaCacheDeAstEntreExpressoesDaMesmaEngine() throws ErroDoMotor {
        GradeEngine engine = new GradeEngine();

        Expression e1 = new ExpressionBuilder(engine, "media(P1, P2)").build();
        e1.setVariable("P1", 5.0);
        e1.setVariable("P2", 7.0);
        e1.evaluate();

        Expression e2 = new ExpressionBuilder(engine, "media(P1, P2)").build();
        e2.setVariable("P1", 9.0);
        e2.setVariable("P2", 9.0);
        e2.evaluate();

        assertEquals(1, engine.tamanhoCache());
    }
}
