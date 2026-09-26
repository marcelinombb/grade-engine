package com.gradeengine.io;

import com.gradeengine.ContextoDeCalculo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lê um CSV no formato "uma linha por aluno, uma coluna por avaliação":
 *
 * aluno,P1,P2,P3
 * Joao,8.0,6.5,9.0
 * Maria,7.0,8.0,10.0
 *
 * A primeira coluna identifica o aluno; todas as demais colunas do
 * cabeçalho viram avaliações (código = nome da coluna) no
 * ContextoDeCalculo daquela linha.
 */
public final class CsvContextoReader {

    private static final String COLUNA_ALUNO = "aluno";

    private CsvContextoReader() {
    }

    public static List<ContextoDeAluno> ler(Reader reader) throws ErroDeLeituraDeDados {
        CSVFormat formato = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .build();

        List<ContextoDeAluno> resultado = new ArrayList<>();

        try (CSVParser parser = new CSVParser(reader, formato)) {
            if (!parser.getHeaderNames().contains(COLUNA_ALUNO)) {
                throw new ErroDeLeituraDeDados("CSV precisa de uma coluna '" + COLUNA_ALUNO + "'");
            }

            for (CSVRecord linha : parser) {
                resultado.add(linhaParaContexto(linha));
            }
        } catch (IOException e) {
            throw new ErroDeLeituraDeDados("Falha ao ler CSV: " + e.getMessage(), e);
        }

        return resultado;
    }

    private static ContextoDeAluno linhaParaContexto(CSVRecord linha) throws ErroDeLeituraDeDados {
        String aluno = linha.get(COLUNA_ALUNO);
        ContextoDeCalculo contexto = new ContextoDeCalculo();

        for (Map.Entry<String, String> coluna : linha.toMap().entrySet()) {
            String codigo = coluna.getKey();
            if (codigo.equals(COLUNA_ALUNO)) {
                continue;
            }

            String valorBruto = coluna.getValue();
            try {
                contexto.adicionar(codigo, Double.parseDouble(valorBruto));
            } catch (NumberFormatException e) {
                throw new ErroDeLeituraDeDados(
                    "Valor inválido para '" + codigo + "' na linha do aluno '" + aluno + "': '" + valorBruto + "'"
                );
            }
        }

        return new ContextoDeAluno(aluno, contexto);
    }
}
