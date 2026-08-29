package com.gradeengine;

import com.gradeengine.domain.ErroDeParse;
import com.gradeengine.domain.ErroDoMotor;
import com.gradeengine.domain.Expr;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Builder fluente para construir uma Expression, no molde do
 * ExpressionBuilder da lib exp4j:
 *
 * Expression e = new ExpressionBuilder("media(P1, P2)")
 *     .variable("P1")
 *     .variable("P2")
 *     .build();
 *
 * e.setVariable("P1", 8.0);
 * e.setVariable("P2", 6.0);
 * double resultado = e.evaluate();
 *
 * .variable(...) é opcional, mas se usado, build() valida que toda
 * variável declarada é de fato referenciada na fórmula e que toda
 * variável referenciada na fórmula foi declarada -- erro de configuração
 * (nome errado, variável esquecida) falha em build(), não em evaluate().
 */
public final class ExpressionBuilder {

    private final GradeEngine engine;
    private final String formula;
    private final Set<String> variaveisDeclaradas = new LinkedHashSet<>();

    public ExpressionBuilder(String formula) {
        this(new GradeEngine(), formula);
    }

    public ExpressionBuilder(GradeEngine engine, String formula) {
        this.engine = engine;
        this.formula = formula;
    }

    public ExpressionBuilder variable(String nome) {
        variaveisDeclaradas.add(nome);
        return this;
    }

    public ExpressionBuilder variables(String... nomes) {
        for (String nome : nomes) {
            variable(nome);
        }
        return this;
    }

    public Expression build() throws ErroDoMotor {
        Expr expressao = engine.parsear(formula);

        if (!variaveisDeclaradas.isEmpty()) {
            validarVariaveis(expressao);
        }

        return new Expression(engine, formula);
    }

    private void validarVariaveis(Expr expressao) throws ErroDoMotor {
        Set<String> referenciadas = Expr.variaveisReferenciadas(expressao);

        Set<String> naoDeclaradas = new LinkedHashSet<>(referenciadas);
        naoDeclaradas.removeAll(variaveisDeclaradas);

        Set<String> naoUsadas = new LinkedHashSet<>(variaveisDeclaradas);
        naoUsadas.removeAll(referenciadas);

        if (!naoDeclaradas.isEmpty()) {
            throw ErroDoMotor.parse(new ErroDeParse.VariavelNaoDeclarada(
                "A fórmula referencia a(s) variável(is) " + naoDeclaradas
                    + " que não foi(ram) declarada(s) via .variable(...)"
            ));
        }

        if (!naoUsadas.isEmpty()) {
            throw ErroDoMotor.parse(new ErroDeParse.VariavelNaoDeclarada(
                "A(s) variável(is) " + naoUsadas + " foi(ram) declarada(s) via .variable(...) "
                    + "mas não é(são) usada(s) na fórmula"
            ));
        }
    }
}
