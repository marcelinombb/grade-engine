package com.gradeengine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ContextoDeCalculo {

    private final Map<String, Double> notas = new HashMap<>();

    public ContextoDeCalculo() {
    }

    public ContextoDeCalculo(Map<String, Double> notasIniciais) {
        if (notasIniciais != null) {
            this.notas.putAll(notasIniciais);
        }
    }

    public static ContextoDeCalculo de(Map<String, Double> notas) {
        return new ContextoDeCalculo(notas);
    }

    public static ContextoDeCalculo vazio() {
        return new ContextoDeCalculo();
    }

    public ContextoDeCalculo adicionar(String codigo, double nota) {
        notas.put(codigo, nota);
        return this;
    }

    public Optional<Double> buscar(String codigo) {
        return Optional.ofNullable(notas.get(codigo));
    }

    public Double obter(String codigo) {
        return notas.get(codigo);
    }

    public boolean contem(String codigo) {
        return notas.containsKey(codigo);
    }

    public Map<String, Double> notas() {
        return Collections.unmodifiableMap(notas);
    }
}
