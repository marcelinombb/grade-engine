# Grade Engine (Java)

Motor de interpretação de fórmulas de cálculo de notas escolares. Recebe uma
fórmula como texto (ex: `media(P1, P2, P3)`, `if(P1 >= 6, 10, 0)`), interpreta
via um pipeline clássico de linguagem (lexer → parser → AST → avaliador) e
calcula o resultado contra as notas de um aluno.

Esta é a versão Java do motor originalmente escrito em Rust
(diretório irmão [`../exitus-grade-engine`](../exitus-grade-engine)), portada
para integração com sistemas Java/JVM existentes.

## Sumário

- [Por que existe](#por-que-existe)
- [A linguagem de fórmulas](#a-linguagem-de-fórmulas)
- [Instalação](#instalação)
- [Instalando em um projeto Spring Boot](#instalando-em-um-projeto-spring-boot)
- [Uso](#uso)
  - [API direta (`GradeEngine`)](#api-direta-gradeengine)
  - [API fluente (`ExpressionBuilder`)](#api-fluente-expressionbuilder)
  - [Lendo notas de CSV](#lendo-notas-de-csv)
  - [Lendo notas de JSON](#lendo-notas-de-json)
  - [Avaliação em lote (uma turma inteira)](#avaliação-em-lote-uma-turma-inteira)
  - [Tratamento de erros](#tratamento-de-erros)
- [Validador de fórmulas em JavaScript](#validador-de-fórmulas-em-javascript)
  - [Página do validador (GitHub Pages)](#página-do-validador-github-pages)
- [Arquitetura](#arquitetura)
- [Limites e proteções](#limites-e-proteções)
- [Rodando localmente](#rodando-localmente)
  - [Pré-requisitos](#pré-requisitos)
  - [Clonando e compilando](#clonando-e-compilando)
  - [Executando os testes](#executando-os-testes)
  - [Empacotando e executando o projeto](#empacotando-e-executando-o-projeto)
  - [Testando o validador JavaScript e interface web](#testando-o-validador-javascript-e-interface-web)
- [Contribuindo](#contribuindo)
  - [Fluxo de desenvolvimento](#fluxo-de-desenvolvimento)
  - [Convenções e estilo de código](#convenções-e-estilo-de-código)
  - [Adicionando uma nova função embutida (ex: `soma(...)`)](#adicionando-uma-nova-função-embutida-ex-soma)
  - [Paridade com o validador JavaScript](#paridade-com-o-validador-javascript)
  - [Checklist para Pull Request](#checklist-para-pull-request)
- [Licença](#licença)

## Por que existe

Sistemas de notas escolares frequentemente precisam de fórmulas de cálculo
configuráveis por quem administra o sistema (coordenação pedagógica, não
programadores) — "a nota final é a média de P1 e P2, mas se a frequência for
menor que 75% a nota vira 0", por exemplo. Hardcodar essas regras em Java
significa alterar e reimplantar código a cada mudança de critério.

O `grade-engine` resolve isso interpretando a fórmula como uma pequena DSL
(domain-specific language) de tempo de execução: a fórmula é texto
configurável, e o motor faz o parse e a avaliação sob demanda.

## A linguagem de fórmulas

```
comparacao := expressao (('>' | '<' | '>=' | '<=' | '==' | '!=') expressao)?
expressao  := termo (('+' | '-') termo)*
termo      := fator (('*' | '/') fator)*
fator      := NUMBER | IDENT | IDENT '(' (comparacao (',' comparacao)*)? ')' | '(' comparacao ')'
```

- **Números**: `7`, `7.5`
- **Variáveis**: qualquer identificador alfanumérico (`P1`, `nota_final`) —
  resolvido em tempo de avaliação contra um `ContextoDeCalculo`
- **Aritmética**: `+ - * /`
- **Comparação**: `> < >= <= = !` (igualdade e diferença usam um único
  caractere: `=` e `!`, não `==`/`!=`)
- **Funções embutidas**:
  - `media(a, b, ...)` — média aritmética, mínimo 1 argumento
  - `min(a, b, ...)` / `max(a, b, ...)` — mínimo 1 argumento
  - `if(condicao, se_verdadeiro, se_falso)` — o primeiro argumento **precisa**
    ser uma expressão de comparação
- **Parênteses** para agrupar: `(P1 + P2) * P3`

Exemplos válidos:

```
media(P1, P2, P3)
if(media(P1, P2) >= 6, 10, 0)
(P1 + P2 + P3) / 3
max(P1, P2) - min(P1, P2)
```

## Instalação

Requer **JDK 21+**. O projeto usa Maven.

```bash
mvn install
```

Isso publica `com.gradeengine:grade-engine:0.1.0` no seu repositório Maven
local (`~/.m2`). Em outro projeto Maven, declare a dependência:

```xml
<dependency>
    <groupId>com.gradeengine</groupId>
    <artifactId>grade-engine</artifactId>
    <version>0.1.0</version>
</dependency>
```

Para gerar apenas o `.jar` sem instalar no repositório local:

```bash
mvn package
# gera target/grade-engine-0.1.0.jar e target/grade-engine-0.1.0-sources.jar
```

## Instalando em um projeto Spring Boot

### Via Maven (repositório local)

Forma recomendada quando o projeto Spring Boot também usa Maven e roda na
mesma máquina (ou CI) onde o `grade-engine` foi instalado.

1. No diretório deste projeto, publique no repositório Maven local:

   ```bash
   mvn install
   ```

2. No `pom.xml` do projeto Spring Boot, declare a dependência:

   ```xml
   <dependency>
       <groupId>com.gradeengine</groupId>
       <artifactId>grade-engine</artifactId>
       <version>0.1.0</version>
   </dependency>
   ```

3. Use normalmente em um `@Service`/`@Component`:

   ```java
   @Service
   public class NotaService {

       private final GradeEngine engine = new GradeEngine();

       public double calcularNota(String formula, ContextoDeCalculo contexto) throws ErroDoMotor {
           return engine.avaliar(formula, contexto);
       }
   }
   ```

   Uma única instância de `GradeEngine` compartilhada (ex: como bean
   `@Bean`/singleton) é o uso recomendado — o cache de AST por fórmula só
   ajuda se a mesma instância for reaproveitada entre requisições:

   ```java
   @Configuration
   public class GradeEngineConfig {
       @Bean
       public GradeEngine gradeEngine() {
           return new GradeEngine();
       }
   }
   ```

### Via `.jar` local (sem repositório compartilhado)

Forma mais direta quando não há um repositório Maven interno disponível
ainda, ou para testar rapidamente sem publicar nada.

1. Gere o `.jar`:

   ```bash
   mvn package
   # gera target/grade-engine-0.1.0.jar
   ```

2. Copie `target/grade-engine-0.1.0.jar` para dentro do projeto Spring Boot,
   por exemplo em `libs/grade-engine-0.1.0.jar`.

3. Declare a dependência apontando para o arquivo local, com `system` scope:

   ```xml
   <dependency>
       <groupId>com.gradeengine</groupId>
       <artifactId>grade-engine</artifactId>
       <version>0.1.0</version>
       <scope>system</scope>
       <systemPath>${project.basedir}/libs/grade-engine-0.1.0.jar</systemPath>
   </dependency>
   ```

   `scope=system` não resolve dependências transitivas automaticamente —
   como o `grade-engine` depende de `jackson-databind` e `commons-csv` (usados
   pelos leitores de CSV/JSON), declare-as também no `pom.xml` consumidor:

   ```xml
   <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-databind</artifactId>
       <version>2.18.2</version>
   </dependency>
   <dependency>
       <groupId>org.apache.commons</groupId>
       <artifactId>commons-csv</artifactId>
       <version>1.11.0</version>
   </dependency>
   ```

   > `scope=system` é uma solução de curto prazo — o Maven considera essa
   > forma de dependência descontinuada e ela não é empacotada corretamente
   > por plugins como o `spring-boot-maven-plugin` em alguns cenários de
   > fat-jar. Assim que houver um repositório Maven interno (Nexus/Artifactory)
   > disponível, prefira publicar o `grade-engine` lá e usar uma dependência
   > normal, como na seção anterior.

### Em um projeto Gradle

O equivalente do passo via Maven, para quem usa Gradle no Spring Boot:

```kotlin
// depois de "mvn install" neste projeto
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.gradeengine:grade-engine:0.1.0")
}
```

## Uso

### API direta (`GradeEngine`)

A forma mais simples: uma instância de `GradeEngine` guarda em cache a AST de
cada fórmula já parseada (por texto de fórmula), então reavaliar a mesma
fórmula para muitos alunos não reparseia a cada chamada.

```java
GradeEngine engine = new GradeEngine();

// Forma 1: ContextoDeCalculo com encadeamento fluente
ContextoDeCalculo contexto = new ContextoDeCalculo()
    .adicionar("P1", 8.0)
    .adicionar("P2", 6.0);

double nota = engine.avaliar("media(P1, P2)", contexto); // 7.0

// Forma 2: Map padrão do Java (direto e conciso)
double notaMap = engine.avaliar("media(P1, P2)", Map.of("P1", 8.0, "P2", 6.0)); // 7.0
```

Para validar uma fórmula sem avaliar (útil no momento de cadastro, antes de
existir qualquer contexto de aluno), há duas variantes:

```java
// lança ErroDoMotor se a fórmula for inválida
String descricao = engine.validar("if(P1 >= 6, 10, 0)");
// "if(Variável: P1 >= Número: 6.0, Número: 10.0, Número: 0.0)"
```

```java
// nunca lança exception -- o motivo do erro (se houver) vem no retorno,
// ideal para um fluxo de cadastro sem try/catch
ResultadoValidacao resultado = engine.validarFormula("media(P1,)");

if (resultado.valida()) {
    String descricao = ((ResultadoValidacao.Valida) resultado).descricao();
} else {
    String motivo = ((ResultadoValidacao.Invalida) resultado).mensagemDeErro();
}

// ou, com pattern matching:
switch (resultado) {
    case ResultadoValidacao.Valida v -> System.out.println("OK: " + v.descricao());
    case ResultadoValidacao.Invalida i -> System.out.println("Inválida: " + i.mensagemDeErro());
}
```

Ambos reaproveitam o mesmo cache de AST da `GradeEngine` — validar uma
fórmula já avaliada antes (ou vice-versa) não reparseia.

### API fluente (`ExpressionBuilder`)

Estilo builder, para quem prefere declarar variáveis explicitamente antes de
avaliar (inspirado na API do [exp4j](https://www.objecthunter.net/exp4j/)):

```java
Expression e = new ExpressionBuilder("media(P1, P2)")
    .variable("P1")
    .variable("P2")
    .build();

e.setVariable("P1", 8.0);
e.setVariable("P2", 6.0);

double resultado = e.evaluate(); // 7.0
```

`.variable(...)` é opcional, mas quando usado, `build()` valida nos dois
sentidos — falha cedo em vez de deixar o erro aparecer só em `evaluate()`:

```java
// falha em build(): a fórmula usa P2, que nunca foi declarada
new ExpressionBuilder("P1 + P2").variable("P1").build();

// falha em build(): P2 foi declarada mas a fórmula não a usa (typo comum)
new ExpressionBuilder("P1 + 1").variables("P1", "P2").build();
```

Para compartilhar o cache de AST entre várias `Expression`s da mesma
fórmula, passe uma `GradeEngine` existente:

```java
GradeEngine engine = new GradeEngine();
Expression e1 = new ExpressionBuilder(engine, "media(P1, P2)").build();
Expression e2 = new ExpressionBuilder(engine, "media(P1, P2)").build(); // reusa a AST em cache
```

`Expression` não é thread-safe — cada thread que avalia a mesma fórmula
concorrentemente deve ter sua própria instância (a `GradeEngine` em si,
essa sim, pode ser compartilhada entre threads).

### Lendo notas de CSV

Formato esperado: uma linha por aluno, uma coluna por avaliação, com a
coluna `aluno` identificando cada linha.

```csv
aluno,P1,P2,P3
Joao,8.0,6.5,9.0
Maria,7.0,8.0,10.0
```

```java
List<ContextoDeAluno> turma = CsvContextoReader.ler(new FileReader("notas.csv"));
```

### Lendo notas de JSON

Formato esperado: array de objetos, um por aluno, com a propriedade
`"aluno"` identificando cada objeto.

```json
[
  {"aluno": "Joao", "P1": 8.0, "P2": 6.5, "P3": 9.0},
  {"aluno": "Maria", "P1": 7.0, "P2": 8.0, "P3": 10.0}
]
```

```java
List<ContextoDeAluno> turma = JsonContextoReader.ler(new FileReader("notas.json"));
```

Ambos os leitores lançam `ErroDeLeituraDeDados` para formato inválido
(coluna/campo `aluno` ausente, valor não numérico, JSON malformado) — um
erro de **dados**, distinto de `ErroDoMotor` (erro do **motor de cálculo**).

### Avaliação em lote (uma turma inteira)

```java
List<ContextoDeAluno> turma = CsvContextoReader.ler(new FileReader("notas.csv"));
List<ResultadoAluno> notas = AvaliacaoEmLote.avaliar(new GradeEngine(), "media(P1, P2, P3)", turma);

for (ResultadoAluno resultado : notas) {
    switch (resultado) {
        case ResultadoAluno.Ok ok -> System.out.println(ok.aluno() + ": " + ok.nota());
        case ResultadoAluno.Erro erro -> System.out.println(erro.aluno() + " falhou: " + erro.erro().getMessage());
    }
}
```

Um erro no cálculo de **um** aluno (ex: fórmula referencia uma avaliação que
esse aluno específico não tem) não interrompe o processamento dos demais —
o erro vira um valor (`ResultadoAluno.Erro`), não uma exception que aborta o
lote inteiro.

### Tratamento de erros

Todo erro do pipeline de cálculo (léxico, sintático ou de avaliação) chega
como `ErroDoMotor` (checked exception), preservando a causa original via
`getCause()`:

```java
try {
    engine.avaliar("P1 / 0", contexto);
} catch (ErroDoMotor erro) {
    if (erro.getCause() instanceof ErroDeCalculo.DivisaoPorZero) {
        // tratar divisão por zero especificamente
    }
    System.err.println(erro.getMessage());
}
```

Hierarquia de erros:

| Camada | Tipo | Quando ocorre |
|---|---|---|
| Léxica | `ErroLexico.CaractereInvalido` | caractere que não pertence à gramática |
| | `ErroLexico.MultiplosPontos` | número com mais de um `.` |
| | `ErroLexico.NumeroInvalido` | número malformado |
| Sintática | `ErroDeParse.ExpressaoInvalida` | estrutura inválida |
| | `ErroDeParse.ParenteseNaoFechado` | parêntese sem fechamento |
| | `ErroDeParse.FuncaoInvalida` | chamada a função inexistente |
| | `ErroDeParse.QuantidadeDeArgumentosInvalida` | ex: `min()` sem argumentos |
| | `ErroDeParse.ArgumentoInvalido` | ex: primeiro argumento de `if` não é comparação |
| | `ErroDeParse.ProfundidadeExcedida` | fórmula excede 200 níveis de aninhamento |
| | `ErroDeParse.VariavelNaoDeclarada` | descompasso em `ExpressionBuilder.variable(...)` |
| Avaliação | `ErroDeCalculo.VariavelInexistente` | fórmula referencia avaliação ausente do contexto |
| | `ErroDeCalculo.DivisaoPorZero` | divisão por zero |
| | `ErroDeCalculo.FuncaoInvalida` | função sem implementação de avaliação |
| | `ErroDeCalculo.ImpossivelAvaliar` | nó da AST sem regra de avaliação |

## Validador de fórmulas em JavaScript

[`validador.js`](validador.js) é uma reimplementação standalone, em JS puro,
do lexer + parser do motor Java — mesmos tokens, mesma precedência, mesmas
mensagens de erro e o mesmo limite de 200 níveis de aninhamento. Não avalia a
fórmula (não precisa de contexto de notas), só confirma que ela é
sintaticamente válida.

Pensado para rodar no navegador, no formulário onde a fórmula é cadastrada:
dá feedback imediato sobre a validade antes de enviar ao backend, sem round
trip ao servidor. Funciona tanto como módulo Node (`require`) quanto direto
no navegador via `<script>`.

```js
const { validarFormula } = require("./validador.js");

validarFormula("media(P1, P2)");
// { valida: true, descricao: "media(Variável: P1, Variável: P2)" }

validarFormula("media(P1,)");
// { valida: false, erro: "Expressão inválida na fórmula" }

validarFormula("if(P1 + P2, 1, 2)");
// { valida: false, erro: "O primeiro argumento deve ser uma comparação" }
```

Esta é uma segunda implementação da mesma gramática (não gerada a partir do
`Parser.java`), então uma mudança na gramática do motor Java precisa ser
replicada aqui manualmente para as duas ficarem em paridade — ver a seção
[Contribuindo](#contribuindo).

### Página do validador (GitHub Pages)

[`docs/index.html`](docs/index.html) é a interface visual do validador —
campo de fórmula com feedback imediato, exemplos clicáveis e referência da
gramática. É publicada automaticamente pelo workflow
[`.github/workflows/pages.yml`](.github/workflows/pages.yml) a cada push em
`main` que altere `docs/`, `validador.js` ou o próprio workflow.

O workflow copia `validador.js` (a fonte de verdade) para `docs/` antes de
publicar, então o arquivo em `docs/` não precisa (e não deve) ser mantido
manualmente em sincronia — editar `validador.js` na raiz do projeto é
suficiente.

Para rodar a página localmente sem esperar o deploy:

```bash
cp validador.js docs/validador.js
cd docs && python3 -m http.server 8080
# abrir http://localhost:8080/index.html
```

## Arquitetura

```
src/main/java/com/gradeengine/
├── GradeEngine.java          fachada pública principal: cache de AST + avaliação
├── ContextoDeCalculo.java    mapa fluente de notas (ou use Map<String, Double>)
├── Expression.java           fachada fluente para avaliação
├── ExpressionBuilder.java    builder de expressões (estilo exp4j)
├── ResultadoValidacao.java   resultado selado de validação (Valida/Invalida)
├── Erro*.java                hierarquia de erros tratáveis (ErroDoMotor, etc.)
├── Lexer.java                (package-private) análise léxica rápida sem boxing
├── Token.java                (package-private) tokens (record + TipoToken enum)
├── TipoToken.java            (package-private) enum com os tipos de token
├── Parser.java               (package-private) parser recursivo descendente
├── Expr.java                 (package-private) nós da AST (sealed interface)
├── Avaliador.java            (package-private) interpretador da AST
├── Main.java                 exemplo executável
└── io/                       leitura externa isolada do core
    ├── CsvContextoReader.java
    ├── JsonContextoReader.java
    ├── ContextoDeAluno.java
    ├── ResultadoAluno.java
    └── AvaliacaoEmLote.java
```

Princípios de design:

- **Encapsulamento e API enxuta** — Detalhes internos de compilação (`Lexer`,
  `Parser`, `Expr`, `Token`, `Avaliador`) são package-private no pacote raiz
  `com.gradeengine`. Quem consome a biblioteca só interage com a API pública
  (`GradeEngine`, `ContextoDeCalculo`, `ExpressionBuilder` e exceções).
- **Core sem dependências externas** — O motor de cálculo não depende de nenhuma
  biblioteca externa (Jackson e Commons CSV vivem isolados exclusivamente no
  subpacote `io`).
- **Cache de AST por texto de fórmula** — `GradeEngine` mantém um
  `ConcurrentHashMap<String, Expr>` interno. Parse acontece uma vez por
  fórmula distinta; chamadas concorrentes de múltiplas threads reusam a
  mesma AST com segurança.
- **Erros como tipos, não strings** — Cada camada tem sua própria hierarquia
  `sealed class extends Exception`, permitindo `instanceof`/pattern matching
  para tratamento específico, sem parsing de mensagem de erro.

## Limites e proteções

- **Profundidade máxima de aninhamento**: 200 níveis de parênteses ou
  argumentos de função aninhados. Acima disso, `Parser` lança
  `ErroDeParse.ProfundidadeExcedida` em vez de estourar a pilha
  (`StackOverflowError`) — relevante porque fórmulas frequentemente vêm de
  entrada de usuário não confiável.

## Rodando localmente

### Pré-requisitos

Antes de iniciar, certifique-se de ter instalado em sua máquina:

- **Java (JDK) 21 ou superior** (ex.: [Eclipse Temurin](https://adoptium.net/), OpenJDK)
- **Apache Maven 3.8+**
- **Git**
- *(Opcional)* **Node.js 18+** (para executar o `validador.js` via CLI) ou **Python 3** (para servir a página de validação localmente)

Verifique suas versões no terminal:

```bash
java -version
mvn -version
git --version
```

### Clonando e compilando

1. Clone o repositório e acesse o diretório do projeto:

   ```bash
   git clone https://github.com/marcelinombb/grade-engine.git
   cd grade-engine
   ```

2. Compile as classes do projeto:

   ```bash
   mvn clean compile
   ```

### Executando os testes

A suíte de testes unitários cobre todas as camadas do motor (lexer, parser, avaliador e leitores de I/O):

- Rodar todos os testes:

  ```bash
  mvn test
  ```

- Rodar uma classe de teste específica:

  ```bash
  mvn test -Dtest=GradeEngineTest
  ```

- Rodar um método de teste específico:

  ```bash
  mvn test -Dtest=GradeEngineTest#deveAvaliarFormulaSimples
  ```

O workflow de CI [`.github/workflows/ci.yml`](.github/workflows/ci.yml) executa `mvn test` automaticamente a cada push ou PR para a branch `main`.

### Empacotando e executando o projeto

1. Gere o pacote JAR:

   ```bash
   mvn package
   ```

   Isso irá gerar os artefatos na pasta `target/`:
   - `target/grade-engine-0.1.0.jar` (JAR executável da biblioteca)
   - `target/grade-engine-0.1.0-sources.jar` (código-fonte)

2. Execute o exemplo interativo da classe [`Main.java`](src/main/java/com/gradeengine/Main.java):

   ```bash
   java -jar target/grade-engine-0.1.0.jar
   ```

   Ou execute diretamente a partir das classes compiladas (sem necessidade de empacotar):

   ```bash
   java -cp target/classes com.gradeengine.Main
   ```

   Saída esperada:
   ```text
   descrição: max(Número: 10.0, Número: 5.0, Número: 8.0, Número: 26.0)
   avaliação: 26.0
   ```

3. (Opcional) Instale o pacote no seu repositório local Maven (`~/.m2`), tornando-o disponível como dependência para outros projetos na mesma máquina:

   ```bash
   mvn clean install
   ```

### Testando o validador JavaScript e interface web

- **Via Node.js (linha de comando)**:
  Você pode testar a biblioteca standalone [`validador.js`](validador.js) diretamente no terminal:

  ```bash
  node -e 'const { validarFormula } = require("./validador.js"); console.log(validarFormula("media(P1, P2)"));'
  ```

- **Interface Web Local (GitHub Pages)**:
  Para testar a interface de validação visual [`docs/index.html`](docs/index.html):

  ```bash
  cp validador.js docs/validador.js
  cd docs && python3 -m http.server 8080
  ```

  Abra `http://localhost:8080/index.html` em seu navegador.

## Contribuindo

Contribuições são muito bem-vindas! Seja reportando bugs, sugerindo melhorias na documentação ou adicionando novas capacidades de cálculo à gramática.

### Fluxo de desenvolvimento

1. **Crie uma branch a partir de `main`**:
   ```bash
   git checkout -b feature/nome-da-mudanca
   # ou para correções:
   git checkout -b fix/descricao-do-bug
   ```

2. **Garanta a baseline**:
   Antes de alterar qualquer código, rode a suíte completa de testes para garantir que tudo está verde:
   ```bash
   mvn test
   ```

3. **Faça suas alterações**:
   Implemente a funcionalidade ou correção mantendo o foco do commit e a qualidade da base de código.

4. **Commits bem estruturados**:
   Procure seguir o padrão [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat: adiciona função mediana ao motor`
   - `fix: corrige precedência de operadores de divisão`
   - `test: adiciona casos de borda para divisão por zero`
   - `docs: atualiza instruções de build local no README`
   - `refactor: simplifica casamento de padrões no parser`

5. **Validação local**:
   Rode os testes novamente e confirme que nenhum teste existente quebrou e que todos os novos testes passaram:
   ```bash
   mvn clean test
   ```

6. **Abra um Pull Request**:
   - Envie a branch para o repositório remoto (`git push origin feature/nome-da-mudanca`).
   - Abra o PR apontando para a branch `main`.
   - No corpo do PR, descreva claramente **o que** foi alterado, **por que** foi alterado e como os testes foram executados.

### Convenções e estilo de código

- **Domínio em português**: nomes de classes, métodos, enums, records e mensagens de exceções voltadas ao domínio devem permanecer em português (ex: `ContextoDeCalculo`, `Avaliador`, `ErroDoMotor`).
- **Java 21 idiomático**:
  - Modelagem da AST e de tokens com `sealed interface` / `sealed class` e `record`.
  - Controle de fluxo e desconstrução usando `switch` pattern matching exaustivo — evite cascatas de `if (x instanceof Y)`.
  - Imutabilidade por padrão nas classes de domínio.
- **Isolamento de camadas**:
  - O pacote raiz `com.gradeengine` é mantido **100% puro**, sem dependência de bibliotecas de terceiros (como Jackson ou Commons CSV).
  - Toda operação de leitura, serialização ou parsing externo reside exclusivamente no pacote `io`.
- **Erros como tipos**:
  - Evite lançar exceções genéricas com strings soltas. Crie ou estenda tipos específicos em `ErroLexico`, `ErroDeParse` ou `ErroDeCalculo`.
- **Cobertura de testes**:
  - Toda regra nova de gramática ou nova função requer testes unitários cobrindo o fluxo feliz e os casos de borda/erro (aridade incorreta, argumentos inválidos, etc.).

### Adicionando uma nova função embutida (ex: `soma(...)`)

Ao introduzir uma nova função na linguagem de fórmulas, siga estes passos em ordem:

1. **Parser**: se a função precisar de validação de aridade ou forma especial (como `if` exige que o primeiro argumento seja comparação), adicione um método auxiliar `parseX(nome)` em `Parser.java` (seguindo o padrão de `parseMedia` ou `parseMinMax`) e registre no `switch` dentro de `fatorSemLimite()`.
2. **Avaliador**: adicione o `case` correspondente em `avaliarFuncao(...)` dentro de `Avaliador.java`, delegando para um método de cálculo privado.
3. **Testes**:
   - Adicione testes em `LexerTest` (se a função requerer novos tokens ou palavras reservadas).
   - Adicione testes em `ParserTest` verificando a `Expr.Funcao` construída.
   - Adicione testes em `AvaliadorTest` cobrindo o resultado numérico e possíveis exceções de cálculo.
4. **Paridade com a versão Rust**: se a alteração for aplicável ao motor irmão em Rust ([`../exitus-grade-engine`](../exitus-grade-engine)), considere portá-la lá também para manter paridade comportamental.
5. **Paridade com o Validador JavaScript**: replique a nova função e regras sintáticas em [`validador.js`](validador.js).

### Paridade com o validador JavaScript

O arquivo [`validador.js`](validador.js) é uma reimplementação independente do lexer e parser para validação em navegadores e formulários web.

- Não é gerado automaticamente a partir do código Java.
- Se você adicionar uma nova função, operador ou limite de aninhamento no Java, é obrigatório atualizar [`validador.js`](validador.js) para mantê-los sincronizados.
- Sempre valide as alterações no JS rodando:
  ```bash
  node -e 'const { validarFormula } = require("./validador.js"); console.log(validarFormula("sua_nova_funcao(...)"));'
  ```

### Checklist para Pull Request

Antes de submeter o PR, certifique-se de preencher os seguintes critérios:

- [ ] Todos os testes passam localmente com `mvn clean test`.
- [ ] Novos testes unitários foram criados para a funcionalidade ou bugfix.
- [ ] O código adota os padrões do Java 21 (`records`, tipos selados, pattern matching) e convenções de domínio em português.
- [ ] Se a gramática ou funções foram alteradas, `validador.js` foi atualizado.
- [ ] A documentação no `README.md` reflete as novas capacidades ou alterações de configuração.

## Licença

Distribuído sob a licença MIT — veja [`LICENSE`](LICENSE) para o texto completo.
