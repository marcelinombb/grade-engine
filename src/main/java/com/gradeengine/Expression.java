package com.gradeengine;

import com.gradeengine.domain.Avaliacao;
import com.gradeengine.domain.ContextoDeCalculo;
import com.gradeengine.domain.ErroDoMotor;

/**
 * Uma fórmula já parseada e validada, pronta para receber valores de
 * variável e ser avaliada -- estilo exp4j (ExpressionBuilder().build()).
 * É uma fachada fluente sobre GradeEngine + ContextoDeCalculo, não um
 * motor paralelo: por baixo, setVariable(...) só popula o mesmo
 * ContextoDeCalculo que o resto da API já usa.
 *
 * Não é thread-safe: cada thread que avalia a mesma fórmula concorrentemente
 * deve ter sua própria Expression (a AST em si é compartilhada via cache
 * da GradeEngine, mas o ContextoDeCalculo de uma Expression é mutável).
 */
public final class Expression {

    private final GradeEngine engine;
    private final String formula;
    private final ContextoDeCalculo contexto = new ContextoDeCalculo();

    Expression(GradeEngine engine, String formula) {
        this.engine = engine;
        this.formula = formula;
    }

    public Expression setVariable(String nome, double valor) {
        contexto.adicionar(new Avaliacao(nome, valor));
        return this;
    }

    public double evaluate() throws ErroDoMotor {
        return engine.avaliar(formula, contexto);
    }
}
