package com.gradeengine.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ContextoDeCalculo {

    private final Map<String, Double> notas = new HashMap<>();

    public void adicionar(Avaliacao avaliacao) {
        notas.put(avaliacao.codigo(), avaliacao.nota());
    }

    public Optional<Double> buscar(String codigo) {
        return Optional.ofNullable(notas.get(codigo));
    }
}
