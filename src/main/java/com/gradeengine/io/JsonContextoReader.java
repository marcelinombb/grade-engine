package com.gradeengine.io;

import com.gradeengine.domain.Avaliacao;
import com.gradeengine.domain.ContextoDeCalculo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Lê um array JSON no formato "um objeto por aluno, uma propriedade por
 * avaliação":
 *
 * [
 *   {"aluno": "Joao", "P1": 8.0, "P2": 6.5, "P3": 9.0},
 *   {"aluno": "Maria", "P1": 7.0, "P2": 8.0, "P3": 10.0}
 * ]
 *
 * A propriedade "aluno" identifica o aluno; todas as demais propriedades
 * do objeto viram avaliações (código = nome da propriedade) no
 * ContextoDeCalculo daquele objeto.
 */
public final class JsonContextoReader {

    private static final String CAMPO_ALUNO = "aluno";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonContextoReader() {
    }

    public static List<ContextoDeAluno> ler(Reader reader) throws ErroDeLeituraDeDados {
        JsonNode raiz;
        try {
            raiz = MAPPER.readTree(reader);
        } catch (IOException e) {
            throw new ErroDeLeituraDeDados("JSON malformado: " + e.getMessage(), e);
        }

        if (!raiz.isArray()) {
            throw new ErroDeLeituraDeDados("JSON precisa ser um array de objetos, um por aluno");
        }

        List<ContextoDeAluno> resultado = new ArrayList<>();
        for (JsonNode objeto : raiz) {
            resultado.add(objetoParaContexto(objeto));
        }

        return resultado;
    }

    private static ContextoDeAluno objetoParaContexto(JsonNode objeto) throws ErroDeLeituraDeDados {
        JsonNode alunoNode = objeto.get(CAMPO_ALUNO);
        if (alunoNode == null || !alunoNode.isTextual()) {
            throw new ErroDeLeituraDeDados("Cada objeto precisa de um campo '" + CAMPO_ALUNO + "' textual");
        }
        String aluno = alunoNode.asText();

        ContextoDeCalculo contexto = new ContextoDeCalculo();
        Iterator<Map.Entry<String, JsonNode>> campos = objeto.fields();
        while (campos.hasNext()) {
            Map.Entry<String, JsonNode> campo = campos.next();
            String codigo = campo.getKey();
            if (codigo.equals(CAMPO_ALUNO)) {
                continue;
            }

            JsonNode valor = campo.getValue();
            if (!valor.isNumber()) {
                throw new ErroDeLeituraDeDados(
                    "Valor inválido para '" + codigo + "' no objeto do aluno '" + aluno + "': " + valor
                );
            }

            contexto.adicionar(new Avaliacao(codigo, valor.asDouble()));
        }

        return new ContextoDeAluno(aluno, contexto);
    }
}
