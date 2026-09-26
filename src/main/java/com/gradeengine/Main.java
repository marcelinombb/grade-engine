package com.gradeengine;

public final class Main {

    public static void main(String[] args) {
        String formula = "max(10, 5, 8, 26)";

        ContextoDeCalculo contexto = new ContextoDeCalculo();
        contexto.adicionar("P1", 10.0);

        GradeEngine engine = new GradeEngine();

        try {
            System.out.println("descrição: " + engine.validar(formula));
            System.out.println("avaliação: " + engine.avaliar(formula, contexto));
        } catch (ErroDoMotor erro) {
            System.err.println("Erro ao processar fórmula: " + erro.getMessage());
        }
    }
}
