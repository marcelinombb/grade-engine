package com.gradeengine.domain;

import java.util.List;

public final class Avaliador {

    private Avaliador() {
    }

    public static double avaliar(Expr expr, ContextoDeCalculo contexto) throws ErroDeCalculo {
        return switch (expr) {
            case Expr.Numero n -> n.valor();
            case Expr.Variavel v -> contexto.buscar(v.nome())
                .orElseThrow(() -> new ErroDeCalculo.VariavelInexistente(v.nome()));
            case Expr.Soma s -> avaliar(s.esquerda(), contexto) + avaliar(s.direita(), contexto);
            case Expr.Subtracao s -> avaliar(s.esquerda(), contexto) - avaliar(s.direita(), contexto);
            case Expr.Multiplicacao m -> avaliar(m.esquerda(), contexto) * avaliar(m.direita(), contexto);
            case Expr.Divisao d -> {
                double divisor = avaliar(d.direita(), contexto);
                if (divisor == 0.0) {
                    throw new ErroDeCalculo.DivisaoPorZero();
                }
                yield avaliar(d.esquerda(), contexto) / divisor;
            }
            case Expr.Funcao f -> avaliarFuncao(f.nome(), f.args(), contexto);
            default -> throw new ErroDeCalculo.ImpossivelAvaliar();
        };
    }

    private static double avaliarFuncao(String caller, List<Expr> exprList, ContextoDeCalculo contexto) throws ErroDeCalculo {
        return switch (caller) {
            case "media" -> media(exprList, contexto);
            case "if" -> comparacao(exprList, contexto);
            case "min" -> min(exprList, contexto);
            case "max" -> max(exprList, contexto);
            default -> throw new ErroDeCalculo.FuncaoInvalida();
        };
    }

    private static double max(List<Expr> exprList, ContextoDeCalculo contexto) throws ErroDeCalculo {
        double resultado = avaliar(exprList.get(0), contexto);
        for (int i = 1; i < exprList.size(); i++) {
            resultado = Math.max(resultado, avaliar(exprList.get(i), contexto));
        }
        return resultado;
    }

    private static double min(List<Expr> exprList, ContextoDeCalculo contexto) throws ErroDeCalculo {
        double resultado = avaliar(exprList.get(0), contexto);
        for (int i = 1; i < exprList.size(); i++) {
            resultado = Math.min(resultado, avaliar(exprList.get(i), contexto));
        }
        return resultado;
    }

    private static double media(List<Expr> exprList, ContextoDeCalculo contexto) throws ErroDeCalculo {
        double soma = 0.0;
        for (Expr expr : exprList) {
            soma += avaliar(expr, contexto);
        }
        return soma / exprList.size();
    }

    private static double comparacao(List<Expr> exprList, ContextoDeCalculo contexto) throws ErroDeCalculo {
        boolean condicao = switch (exprList.get(0)) {
            case Expr.Maior m -> avaliar(m.esquerda(), contexto) > avaliar(m.direita(), contexto);
            case Expr.MaiorIgual m -> avaliar(m.esquerda(), contexto) >= avaliar(m.direita(), contexto);
            case Expr.Menor m -> avaliar(m.esquerda(), contexto) < avaliar(m.direita(), contexto);
            case Expr.MenorIgual m -> avaliar(m.esquerda(), contexto) <= avaliar(m.direita(), contexto);
            case Expr.Igual i -> avaliar(i.esquerda(), contexto) == avaliar(i.direita(), contexto);
            case Expr.Diferente d -> avaliar(d.esquerda(), contexto) != avaliar(d.direita(), contexto);
            default -> throw new ErroDeCalculo.FuncaoInvalida();
        };

        return condicao ? avaliar(exprList.get(1), contexto) : avaliar(exprList.get(2), contexto);
    }
}
