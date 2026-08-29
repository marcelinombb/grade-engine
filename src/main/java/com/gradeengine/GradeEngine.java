package com.gradeengine;

import com.gradeengine.domain.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fachada pública do motor: recebe o texto de uma fórmula, faz o parse uma
 * única vez (guardando a AST em cache por texto de fórmula) e avalia contra
 * um ContextoDeCalculo. Pensada para o caso de uso real -- a mesma fórmula
 * é reavaliada para muitos alunos -- então parse repetido é custo evitável.
 */
public final class GradeEngine {

    private final Map<String, Expr> cache = new ConcurrentHashMap<>();

    public double avaliar(String formula, ContextoDeCalculo contexto) throws ErroDoMotor {
        Expr expressao = obterOuParsear(formula);

        try {
            return Avaliador.avaliar(expressao, contexto);
        } catch (ErroDeCalculo erro) {
            throw ErroDoMotor.calculo(erro);
        }
    }

    /**
     * Só faz o parse (útil para validar uma fórmula no momento do cadastro,
     * antes de qualquer avaliação) e já deixa o resultado em cache.
     */
    public String validar(String formula) throws ErroDoMotor {
        return Expr.descrever(obterOuParsear(formula));
    }

    /**
     * Mesma checagem de validar(...), mas sem exception: o motivo do erro
     * (se houver) vem embutido no retorno, para uso direto em um fluxo de
     * cadastro sem try/catch.
     */
    public ResultadoValidacao validarFormula(String formula) {
        try {
            return new ResultadoValidacao.Valida(validar(formula));
        } catch (ErroDoMotor erro) {
            return new ResultadoValidacao.Invalida(erro.getMessage());
        }
    }

    /**
     * Expõe a AST já parseada (e cacheada) para quem precisa inspecioná-la
     * antes de avaliar -- hoje, só ExpressionBuilder.build(), que valida as
     * variáveis declaradas contra as efetivamente referenciadas na fórmula.
     */
    Expr parsear(String formula) throws ErroDoMotor {
        return obterOuParsear(formula);
    }

    private Expr obterOuParsear(String formula) throws ErroDoMotor {
        Expr cacheada = cache.get(formula);
        if (cacheada != null) {
            return cacheada;
        }

        List<Token> tokens;
        try {
            tokens = new Lexer(formula).tokens();
        } catch (ErroLexico erro) {
            throw ErroDoMotor.lexico(erro);
        }

        Expr expressao;
        try {
            expressao = new Parser(tokens).parse();
        } catch (ErroDeParse erro) {
            throw ErroDoMotor.parse(erro);
        }

        cache.put(formula, expressao);
        return expressao;
    }

    int tamanhoCache() {
        return cache.size();
    }
}
