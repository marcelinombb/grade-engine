package com.gradeengine;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

sealed interface Expr {

    record Numero(double valor) implements Expr {}
    record Variavel(String nome) implements Expr {}
    record Soma(Expr esquerda, Expr direita) implements Expr {}
    record Subtracao(Expr esquerda, Expr direita) implements Expr {}
    record Multiplicacao(Expr esquerda, Expr direita) implements Expr {}
    record Divisao(Expr esquerda, Expr direita) implements Expr {}
    record Maior(Expr esquerda, Expr direita) implements Expr {}
    record Menor(Expr esquerda, Expr direita) implements Expr {}
    record Igual(Expr esquerda, Expr direita) implements Expr {}
    record Diferente(Expr esquerda, Expr direita) implements Expr {}
    record MaiorIgual(Expr esquerda, Expr direita) implements Expr {}
    record MenorIgual(Expr esquerda, Expr direita) implements Expr {}
    record Funcao(String nome, List<Expr> args) implements Expr {}

    static Optional<Funcao> asChamada(Expr expr) {
        if (expr instanceof Funcao funcao) {
            return Optional.of(funcao);
        }
        return Optional.empty();
    }

    static Set<String> variaveisReferenciadas(Expr expr) {
        Set<String> nomes = new LinkedHashSet<>();
        coletarVariaveis(expr, nomes);
        return nomes;
    }

    private static void coletarVariaveis(Expr expr, Set<String> acumulador) {
        switch (expr) {
            case Numero n -> {}
            case Variavel v -> acumulador.add(v.nome());
            case Soma e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case Subtracao e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case Multiplicacao e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case Divisao e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case Maior e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case Menor e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case Igual e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case Diferente e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case MaiorIgual e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case MenorIgual e -> { coletarVariaveis(e.esquerda(), acumulador); coletarVariaveis(e.direita(), acumulador); }
            case Funcao f -> f.args().forEach(arg -> coletarVariaveis(arg, acumulador));
        }
    }

    static boolean isComparacao(Expr expr) {
        return switch (expr) {
            case Maior m -> true;
            case Menor m -> true;
            case MaiorIgual m -> true;
            case MenorIgual m -> true;
            case Igual i -> true;
            case Diferente d -> true;
            default -> false;
        };
    }

    static String descrever(Expr expr) {
        return switch (expr) {
            case Numero n -> "Número: " + n.valor();
            case Variavel v -> "Variável: " + v.nome();
            case Soma s -> descrever(s.esquerda()) + " + " + descrever(s.direita());
            case Subtracao s -> descrever(s.esquerda()) + " - " + descrever(s.direita());
            case Multiplicacao m -> descrever(m.esquerda()) + " * " + descrever(m.direita());
            case Divisao d -> descrever(d.esquerda()) + " / " + descrever(d.direita());
            case Maior m -> descrever(m.esquerda()) + " > " + descrever(m.direita());
            case Menor m -> descrever(m.esquerda()) + " < " + descrever(m.direita());
            case MaiorIgual m -> descrever(m.esquerda()) + " >= " + descrever(m.direita());
            case MenorIgual m -> descrever(m.esquerda()) + " <= " + descrever(m.direita());
            case Igual i -> descrever(i.esquerda()) + " == " + descrever(i.direita());
            case Diferente d -> descrever(d.esquerda()) + " != " + descrever(d.direita());
            case Funcao f -> {
                String argsStr = f.args().stream().map(Expr::descrever).collect(Collectors.joining(", "));
                yield switch (f.nome()) {
                    case "if" -> "if(" + descrever(f.args().get(0)) + ", " + descrever(f.args().get(1)) + ", " + descrever(f.args().get(2)) + ")";
                    default -> f.nome() + "(" + argsStr + ")";
                };
            }
        };
    }
}
