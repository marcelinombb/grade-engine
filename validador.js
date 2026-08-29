/**
 * Validador de fórmulas de cálculo de notas, em JavaScript puro.
 *
 * Reimplementação fiel da gramática do motor Java (com.gradeengine.domain
 * Lexer + Parser): mesmos tokens, mesma precedência, mesmas mensagens de
 * erro e o mesmo limite de profundidade de aninhamento (200 níveis).
 * Não avalia a fórmula (não precisa de contexto de notas) -- só confirma
 * que ela é sintaticamente válida, para dar feedback imediato no momento
 * de cadastro, antes de enviar ao backend.
 *
 * Gramática:
 *   comparacao := expressao (('>' | '<' | '>=' | '<=' | '=' | '!') expressao)?
 *   expressao  := termo (('+' | '-') termo)*
 *   termo      := fator (('*' | '/') fator)*
 *   fator      := NUMBER | IDENT | IDENT '(' (comparacao (',' comparacao)*)? ')' | '(' comparacao ')'
 *
 * Uso:
 *   const resultado = validarFormula("media(P1, P2)");
 *   resultado.valida       // true | false
 *   resultado.descricao    // string, só quando valida === true
 *   resultado.erro         // string, só quando valida === false
 */
(function (root, factory) {
    if (typeof module === "object" && module.exports) {
        module.exports = factory();
    } else {
        root.validarFormula = factory().validarFormula;
    }
})(typeof self !== "undefined" ? self : this, function () {
    "use strict";

    const PROFUNDIDADE_MAXIMA = 200;

    class ErroDeFormula extends Error {
        constructor(mensagem) {
            super(mensagem);
        }
    }

    // ---------------------------------------------------------------
    // Lexer
    // ---------------------------------------------------------------

    const TIPO = Object.freeze({
        NUMBER: "NUMBER",
        IDENT: "IDENT",
        PLUS: "PLUS",
        MINUS: "MINUS",
        STAR: "STAR",
        SLASH: "SLASH",
        LEFT_PAREN: "LEFT_PAREN",
        RIGHT_PAREN: "RIGHT_PAREN",
        COMMA: "COMMA",
        GREATER: "GREATER",
        LESS: "LESS",
        GREATER_EQ: "GREATER_EQ",
        LESS_EQ: "LESS_EQ",
        EQUAL: "EQUAL",
        NOT_EQUAL: "NOT_EQUAL",
        AND: "AND",
        OR: "OR",
    });

    function token(tipo, valor) {
        return { tipo, valor };
    }

    function isDigit(ch) {
        return ch >= "0" && ch <= "9";
    }

    // Equivalente a Character.isLetterOrDigit para o alfabeto usado nas
    // fórmulas: letras ASCII/unicode básicas, dígitos e '_'.
    function isLetterOrDigit(ch) {
        return /[\p{L}\p{N}_]/u.test(ch);
    }

    function isComparacao(ch) {
        return ch === "<" || ch === ">" || ch === "=" || ch === "!";
    }

    class Lexer {
        constructor(input) {
            this.input = input;
            this.cursor = 0;
        }

        peek() {
            if (this.cursor >= this.input.length) return null;
            return this.input[this.cursor];
        }

        advance() {
            const ch = this.peek();
            this.cursor += 1;
            return ch;
        }

        tokens() {
            const resultado = [];
            let tok;
            while ((tok = this.nextToken()) !== null) {
                resultado.push(tok);
            }
            return resultado;
        }

        nextToken() {
            for (;;) {
                const ch = this.advance();
                if (ch === null) return null;

                switch (ch) {
                    case " ":
                    case "\n":
                    case "\t":
                    case "\r":
                        continue;
                    case "+":
                        return token(TIPO.PLUS);
                    case "-":
                        return token(TIPO.MINUS);
                    case "*":
                        return token(TIPO.STAR);
                    case "/":
                        return token(TIPO.SLASH);
                    case "(":
                        return token(TIPO.LEFT_PAREN);
                    case ")":
                        return token(TIPO.RIGHT_PAREN);
                    case ",":
                        return token(TIPO.COMMA);
                    case "|":
                        return token(TIPO.OR);
                    case "&":
                        return token(TIPO.AND);
                    default:
                        if (isComparacao(ch)) return this.consumeComparationChars(ch);
                        if (isDigit(ch)) return this.consumeNumberChars(ch);
                        if (isLetterOrDigit(ch)) return this.consumeIdentifierChars(ch);
                        throw new ErroDeFormula(`Caractere inválido na fórmula: '${ch}'`);
                }
            }
        }

        consumeNumberChars(chInicial) {
            let numero = chInicial;
            let hasDot = false;

            for (;;) {
                const peeked = this.peek();
                if (peeked === null) break;

                if (isDigit(peeked) || peeked === ".") {
                    if (peeked === "." && hasDot) {
                        throw new ErroDeFormula(`Numero inválido ${numero}${peeked}`);
                    } else if (peeked === ".") {
                        hasDot = true;
                    }
                    numero += this.advance();
                } else {
                    break;
                }
            }

            const valor = Number(numero);
            if (Number.isNaN(valor)) {
                throw new ErroDeFormula(`Número inválido na fórmula: ${numero}`);
            }
            return token(TIPO.NUMBER, valor);
        }

        consumeIdentifierChars(chInicial) {
            let identifier = chInicial;

            for (;;) {
                const peeked = this.peek();
                if (peeked === null) break;

                if (isLetterOrDigit(peeked)) {
                    identifier += this.advance();
                } else {
                    break;
                }
            }

            return token(TIPO.IDENT, identifier);
        }

        consumeComparationChars(ch) {
            switch (ch) {
                case "=":
                    this.advance();
                    return token(TIPO.EQUAL);
                case "<":
                    if (this.peek() === "=") {
                        this.advance();
                        return token(TIPO.LESS_EQ);
                    }
                    return token(TIPO.LESS);
                case ">":
                    if (this.peek() === "=") {
                        this.advance();
                        return token(TIPO.GREATER_EQ);
                    }
                    return token(TIPO.GREATER);
                case "!":
                    this.advance();
                    return token(TIPO.NOT_EQUAL);
                default:
                    throw new ErroDeFormula(`caractere inválido: ${ch}`);
            }
        }
    }

    // ---------------------------------------------------------------
    // Parser (recursivo descendente) -- produz uma AST mínima, só o
    // suficiente para validar a gramática e descrever o resultado.
    // ---------------------------------------------------------------

    function expr(tipo, props) {
        return Object.assign({ tipo }, props);
    }

    const COMPARACOES = new Set(["Maior", "Menor", "MaiorIgual", "MenorIgual", "Igual", "Diferente"]);

    class Parser {
        constructor(tokens) {
            this.tokens = tokens;
            this.cursor = 0;
            this.profundidadeAtual = 0;
        }

        peek() {
            if (this.cursor >= this.tokens.length) return null;
            return this.tokens[this.cursor];
        }

        advance() {
            const tok = this.peek();
            this.cursor += 1;
            return tok;
        }

        aindaFaltamTokens() {
            return this.cursor < this.tokens.length;
        }

        parse() {
            const resultado = this.comparacao();
            if (this.aindaFaltamTokens()) {
                throw new ErroDeFormula("Expressão inválida na fórmula");
            }
            return resultado;
        }

        fator() {
            this.profundidadeAtual += 1;
            if (this.profundidadeAtual > PROFUNDIDADE_MAXIMA) {
                throw new ErroDeFormula(
                    `Fórmula excede a profundidade máxima permitida (${PROFUNDIDADE_MAXIMA} níveis de aninhamento)`
                );
            }
            try {
                return this.fatorSemLimite();
            } finally {
                this.profundidadeAtual -= 1;
            }
        }

        fatorSemLimite() {
            const atual = this.peek();

            if (atual && atual.tipo === TIPO.NUMBER) {
                this.advance();
                return expr("Numero", { valor: atual.valor });
            }

            if (atual && atual.tipo === TIPO.IDENT) {
                this.advance();
                const nome = atual.valor;
                const proximo = this.peek();

                if (proximo && proximo.tipo === TIPO.LEFT_PAREN) {
                    if (nome === "if") return this.parseIf(nome);
                    if (nome === "media") return this.parseMedia(nome);
                    if (nome === "min" || nome === "max") return this.parseMinMax(nome);
                    throw new ErroDeFormula(`A função '${nome}' não existe`);
                }

                return expr("Variavel", { nome });
            }

            if (atual && atual.tipo === TIPO.LEFT_PAREN) {
                this.advance();
                const interno = this.comparacao();
                const fechamento = this.peek();
                if (fechamento && fechamento.tipo === TIPO.RIGHT_PAREN) {
                    this.advance();
                    return interno;
                }
                throw new ErroDeFormula("Parêntese não fechado na fórmula");
            }

            throw new ErroDeFormula("Expressão inválida na fórmula");
        }

        expressao() {
            let esquerda = this.termo();

            for (;;) {
                const atual = this.peek();
                if (atual && atual.tipo === TIPO.PLUS) {
                    this.advance();
                    esquerda = expr("Soma", { esquerda, direita: this.termo() });
                } else if (atual && atual.tipo === TIPO.MINUS) {
                    this.advance();
                    esquerda = expr("Subtracao", { esquerda, direita: this.termo() });
                } else {
                    break;
                }
            }

            return esquerda;
        }

        comparacao() {
            const esquerda = this.expressao();
            const atual = this.peek();

            if (atual === null) return esquerda;

            switch (atual.tipo) {
                case TIPO.GREATER:
                    this.advance();
                    return expr("Maior", { esquerda, direita: this.expressao() });
                case TIPO.LESS:
                    this.advance();
                    return expr("Menor", { esquerda, direita: this.expressao() });
                case TIPO.GREATER_EQ:
                    this.advance();
                    return expr("MaiorIgual", { esquerda, direita: this.expressao() });
                case TIPO.LESS_EQ:
                    this.advance();
                    return expr("MenorIgual", { esquerda, direita: this.expressao() });
                case TIPO.EQUAL:
                    this.advance();
                    return expr("Igual", { esquerda, direita: this.expressao() });
                case TIPO.NOT_EQUAL:
                    this.advance();
                    return expr("Diferente", { esquerda, direita: this.expressao() });
                default:
                    return esquerda;
            }
        }

        termo() {
            let esquerda = this.fator();

            for (;;) {
                const atual = this.peek();
                if (atual && atual.tipo === TIPO.STAR) {
                    this.advance();
                    esquerda = expr("Multiplicacao", { esquerda, direita: this.fator() });
                } else if (atual && atual.tipo === TIPO.SLASH) {
                    this.advance();
                    esquerda = expr("Divisao", { esquerda, direita: this.fator() });
                } else {
                    break;
                }
            }

            return esquerda;
        }

        consumeFunction(nome) {
            const args = [];

            this.advance(); // consome o '(' -- fator() só espiou, não consumiu

            const atual = this.peek();
            if (atual && atual.tipo === TIPO.RIGHT_PAREN) {
                this.advance();
                return expr("Funcao", { nome, args });
            }

            for (;;) {
                args.push(this.comparacao());

                const separador = this.peek();
                if (separador && separador.tipo === TIPO.COMMA) {
                    this.advance();
                    continue;
                }
                if (separador && separador.tipo === TIPO.RIGHT_PAREN) {
                    this.advance();
                    break;
                }
                throw new ErroDeFormula("Parêntese não fechado na fórmula");
            }

            return expr("Funcao", { nome, args });
        }

        parseChamadaComAridadeMinima(nome, minimo) {
            const chamada = this.consumeFunction(nome);

            if (chamada.args.length < minimo) {
                throw new ErroDeFormula(
                    `${chamada.args.length} argumentos encontrados, onde no mínimo ${minimo} ` +
                        `eram esperados para ${nome}(...).`
                );
            }

            return chamada;
        }

        parseIf(nome) {
            const chamada = this.parseChamadaComAridadeMinima(nome, 3);
            if (!COMPARACOES.has(chamada.args[0].tipo)) {
                throw new ErroDeFormula("O primeiro argumento deve ser uma comparação");
            }
            return chamada;
        }

        parseMedia(nome) {
            return this.parseChamadaComAridadeMinima(nome, 1);
        }

        parseMinMax(nome) {
            return this.parseChamadaComAridadeMinima(nome, 1);
        }
    }

    // ---------------------------------------------------------------
    // Descrição legível da AST (equivalente a Expr.descrever no Java)
    // ---------------------------------------------------------------

    const OPERADORES = {
        Soma: "+",
        Subtracao: "-",
        Multiplicacao: "*",
        Divisao: "/",
        Maior: ">",
        Menor: "<",
        MaiorIgual: ">=",
        MenorIgual: "<=",
        Igual: "==",
        Diferente: "!=",
    };

    function descrever(node) {
        switch (node.tipo) {
            case "Numero":
                return `Número: ${node.valor}`;
            case "Variavel":
                return `Variável: ${node.nome}`;
            case "Funcao": {
                const args = node.args.map(descrever);
                if (node.nome === "if") {
                    return `if(${args[0]}, ${args[1]}, ${args[2]})`;
                }
                return `${node.nome}(${args.join(", ")})`;
            }
            default:
                return `${descrever(node.esquerda)} ${OPERADORES[node.tipo]} ${descrever(node.direita)}`;
        }
    }

    // ---------------------------------------------------------------
    // API pública
    // ---------------------------------------------------------------

    function validarFormula(formula) {
        try {
            const tokens = new Lexer(formula).tokens();
            const ast = new Parser(tokens).parse();
            return { valida: true, descricao: descrever(ast) };
        } catch (erro) {
            if (erro instanceof ErroDeFormula) {
                return { valida: false, erro: erro.message };
            }
            throw erro;
        }
    }

    return { validarFormula };
});
