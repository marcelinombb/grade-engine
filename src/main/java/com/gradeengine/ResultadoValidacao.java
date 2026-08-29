package com.gradeengine;

/**
 * Resultado de validar uma fórmula sem avaliá-la -- pensado para o
 * momento de cadastro (quem está configurando a fórmula quer saber se ela
 * é válida e, se não for, por quê, sem precisar de try/catch).
 */
public sealed interface ResultadoValidacao {

    boolean valida();

    record Valida(String descricao) implements ResultadoValidacao {
        @Override
        public boolean valida() {
            return true;
        }
    }

    record Invalida(String mensagemDeErro) implements ResultadoValidacao {
        @Override
        public boolean valida() {
            return false;
        }
    }
}
