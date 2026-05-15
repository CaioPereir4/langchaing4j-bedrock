# LangChain4j com AWS Bedrock e Java

Projeto de exemplo com Spring Boot demonstrando diferentes formas de integrar aplicações Java com modelos de IA generativa hospedados no AWS Bedrock usando o LangChain4j.

O objetivo do repositório é servir como referência prática para:

- usar a interface de baixo nível `ChatModel`;
- usar a interface de alto nível `AiServices`;
- adicionar memória de conversa com LangChain4j;
- transmitir respostas token por token com Spring WebFlux;
- gerar saídas estruturadas a partir de chamadas ao modelo.

## Tecnologias

- Java 25
- Spring Boot 4.0.6
- Maven Wrapper
- LangChain4j 1.14.1
- LangChain4j Bedrock 1.14.1
- AWS SDK para acesso ao Bedrock Runtime
- Spring WebMVC
- Spring WebFlux
- Spring Validation
- Spring Data JPA
- PostgreSQL
- Lombok

## Estrutura dos exemplos

Os exemplos ficam em `src/main/java/one_agentic/backend_management/app`:

| Pasta | Descrição |
| --- | --- |
| `chat` | Integração simples usando `ChatModel`, a interface de baixo nível do LangChain4j. |
| `chat_with_memory` | Integração usando `AiServices`, a interface de alto nível do LangChain4j, com memória de conversa. |
| `chat_with_response_stream` | Integração usando `AiServices`, memória de conversa e stream de respostas token por token com Spring WebFlux. |
| `dto` | DTOs de entrada e saída usados pelos controllers. |
| `shared` | Serviços compartilhados entre os exemplos, como o store de memória. |
| `structured_output` | Exemplo de saída estruturada usando LangChain4j. |

Também existe a pasta `infra`, que concentra configurações de infraestrutura, como a criação dos beans `ChatModel` e `StreamingChatModel` para o AWS Bedrock.

## Pré-requisitos

Antes de executar o projeto, você precisa ter:

- JDK 25 instalado;
- acesso ao AWS Bedrock;
- um modelo liberado no AWS Bedrock para a região configurada;
- credenciais AWS válidas no ambiente;
- PostgreSQL em execução;
- Maven não é obrigatório, pois o projeto inclui o Maven Wrapper (`mvnw` e `mvnw.cmd`).

## Configuração

As configurações principais ficam em `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: backend-management
  datasource:
    url: jdbc:postgresql://localhost/onagenticdb
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update

aws:
  region: ${AWS_REGION:us-west-2}
  bedrock:
    model-id: ${AWS_BEDROCK_MODEL_ID}
```

### Banco de dados

Por padrão, a aplicação espera um PostgreSQL local com:

- host: `localhost`
- database: `onagenticdb`
- usuário: `postgres`
- senha: `postgres`

Você pode usar esses valores locais ou sobrescrever as propriedades do Spring Boot por variável de ambiente.

Exemplo no PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost/onagenticdb"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
```

Exemplo no Linux/macOS:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost/onagenticdb"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"
```

Se quiser subir um PostgreSQL rapidamente com Docker:

```bash
docker run --name langchain4j-bedrock-postgres \
  -e POSTGRES_DB=onagenticdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:latest
```

### AWS Bedrock

A classe `BedrockConfig` cria os clientes `BedrockRuntimeClient` e `BedrockRuntimeAsyncClient` usando a cadeia padrão de credenciais do AWS SDK. Isso significa que você pode autenticar usando variáveis de ambiente, profile local da AWS CLI, credenciais de uma role ou qualquer outro provider suportado pelo SDK.

As variáveis mais importantes são:

| Variável | Obrigatória | Descrição |
| --- | --- | --- |
| `AWS_REGION` | Não | Região AWS usada pelo Bedrock. O valor padrão é `us-west-2`. |
| `AWS_BEDROCK_MODEL_ID` | Sim | ID do modelo Bedrock usado pelo LangChain4j. |
| `AWS_ACCESS_KEY_ID` | Depende do ambiente | Access key usada pelo provider padrão do AWS SDK. |
| `AWS_SECRET_ACCESS_KEY` | Depende do ambiente | Secret key usada pelo provider padrão do AWS SDK. |
| `AWS_SESSION_TOKEN` | Depende do ambiente | Token de sessão, quando estiver usando credenciais temporárias. |
| `AWS_PROFILE` | Depende do ambiente | Nome do profile local da AWS CLI, se você preferir autenticar por profile. |

Exemplo no PowerShell:

```powershell
$env:AWS_REGION="us-west-2"
$env:AWS_BEDROCK_MODEL_ID="seu-model-id-do-bedrock"
$env:AWS_ACCESS_KEY_ID="sua-access-key"
$env:AWS_SECRET_ACCESS_KEY="sua-secret-key"
```

Exemplo no Linux/macOS:

```bash
export AWS_REGION="us-west-2"
export AWS_BEDROCK_MODEL_ID="seu-model-id-do-bedrock"
export AWS_ACCESS_KEY_ID="sua-access-key"
export AWS_SECRET_ACCESS_KEY="sua-secret-key"
```

Se você usa AWS CLI com profiles:

```bash
export AWS_PROFILE="seu-profile"
export AWS_REGION="us-west-2"
export AWS_BEDROCK_MODEL_ID="seu-model-id-do-bedrock"
```

## Executando o projeto

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

Por padrão, a API sobe em:

```text
http://localhost:8080
```

## Build e testes

Para compilar:

```bash
./mvnw clean package
```

No Windows:

```powershell
.\mvnw.cmd clean package
```

Para executar os testes:

```bash
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

## Endpoints

Todos os exemplos recebem um JSON com o formato abaixo:

```json
{
  "text": "Explique o que é LangChain4j em poucas palavras.",
  "sessionId": "11111111-1111-1111-1111-111111111111"
}
```

O campo `text` é obrigatório. O campo `sessionId` também é obrigatório no DTO atual e é usado principalmente nos exemplos com memória.

### Chat simples

Endpoint:

```http
POST /chat
```

Esse exemplo usa diretamente o bean `ChatModel`, que é a interface de baixo nível do LangChain4j.

```bash
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Olá! Responda em uma frase.",
    "sessionId": "11111111-1111-1111-1111-111111111111"
  }'
```

Resposta esperada:

```json
{
  "message": "..."
}
```

### Chat com memória

Endpoint:

```http
POST /chat-with-memory
```

Esse exemplo usa `AiServices`, a interface de alto nível do LangChain4j, com `MessageWindowChatMemory`. A memória é separada por `sessionId`.

```bash
curl -X POST http://localhost:8080/chat-with-memory \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Meu nome é Ana. Guarde essa informação para a conversa.",
    "sessionId": "22222222-2222-2222-2222-222222222222"
  }'
```

Em seguida, usando o mesmo `sessionId`:

```bash
curl -X POST http://localhost:8080/chat-with-memory \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Qual é o meu nome?",
    "sessionId": "22222222-2222-2222-2222-222222222222"
  }'
```

### Chat com stream de resposta

Endpoint:

```http
POST /chat-with-response-stream
```

Esse exemplo usa `AiServices`, memória de conversa e `StreamingChatModel` para retornar tokens via Server-Sent Events.

```bash
curl -N -X POST http://localhost:8080/chat-with-response-stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "text": "Explique AWS Bedrock em três tópicos.",
    "sessionId": "33333333-3333-3333-3333-333333333333"
  }'
```

Durante o stream, a API envia eventos do tipo:

```text
event: token
data: ...

event: complete
data: ...
```

### Saída estruturada

Endpoint:

```http
POST /structured-output
```

Esse exemplo usa `AiServices` para converter a resposta do modelo em um tipo estruturado. No código atual, o serviço demonstra uma análise de sentimento que retorna um booleano.

```bash
curl -X POST http://localhost:8080/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Esse projeto é muito útil.",
    "sessionId": "44444444-4444-4444-4444-444444444444"
  }'
```

Resposta esperada:

```json
true
```

## Memória das conversas

Os exemplos com memória usam `PersistentMemoryStore`, que implementa `ChatMemoryStore`.

Apesar do nome, a implementação atual armazena o histórico em um `ConcurrentHashMap`. Ou seja, a memória fica disponível enquanto a aplicação estiver em execução, mas é perdida quando o processo reinicia.

Para persistência real, substitua essa implementação por um store baseado em banco de dados, Redis ou outro mecanismo externo.

## Observações sobre o Bedrock

O bean `ChatModel` está configurado com:

- timeout de 30 segundos;
- até 3 retries;
- temperatura `0.3`;
- limite de `800` tokens de saída;
- prompt caching após a mensagem de sistema.

O bean `StreamingChatModel` usa configuração semelhante e adiciona o campo `reasoningConfig` em `additionalModelRequestField`.

Verifique se o modelo escolhido em `AWS_BEDROCK_MODEL_ID` aceita os parâmetros configurados. Alguns modelos podem exigir ajustes nos campos enviados ao Bedrock.
