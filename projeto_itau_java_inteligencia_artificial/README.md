# Assistente Budgeting — Projeto de Finalização Banco Itaú

> Java + Spring Boot + Spring AI | Transformando Voz em Dados Financeiros

## Visão Geral

O **Assistente Budgeting** é uma API de orçamento pessoal que transforma relatos de voz em transações financeiras estruturadas e persistidas automaticamente.

**Exemplo central do desafio:**
> *"Gastei 50 reais no Starbucks agora"*

O sistema processa essa fala e persiste:

| Campo      | Dado Extraído     |
|------------|-------------------|
| Valor      | R$ 50,00          |
| Local      | Starbucks         |
| Data/Hora  | Instante da fala  |
| Categoria  | FOOD *(automático)* |

Sem que o usuário precise informar a categoria — o sistema infere automaticamente pelo nome do estabelecimento.

---

## Stack Tecnológica

- **Java 21** + **Spring Boot 3.3.x**
- **Spring AI 1.0.0** — Tool Calling com ChatClient (GPT-4o)
- **Spring Cloud OpenFeign** — cliente HTTP para Whisper (transcrição) e TTS (resposta em voz)
- **Spring Data JPA** + **MySQL 8**
- **Lombok**, **Maven**

---

## Fluxo do Sistema

```
Áudio do usuário
     │
     ▼
POST /api/voice
     │
     ▼
OpenAiAudioClient (Feign) → Whisper → Texto transcrito
     │
     ▼
ChatClient (Spring AI) + BudgetingAssistant (@Tool)
     │
     ▼
PersistTransactionUseCase
  ├── occurredAt == null? → Instant.now()
  └── category == null?  → MerchantCategoryClassifier → Strategy Rules
     │
     ▼
TransactionRepository → MySQL
     │
     ▼
OpenAiAudioClient (Feign) → TTS → Áudio de resposta (audio/mpeg)
```

---

## Estrutura de Pacotes

```
src/main/java/dio/budgeting/
├── BudgetingApplication.java
├── assistant/
│   ├── BudgetingAssistant.java       ← @Tool methods
│   ├── OpenAiAudioClient.java        ← Feign client (Whisper + TTS)
│   ├── TranscriptionResponse.java
│   └── SpeechRequest.java
├── config/
│   └── BudgetingAssistantConfig.java ← ChatClient bean
├── controller/
│   ├── BudgetingController.java      ← REST + /api/voice
│   └── HealthController.java
├── domain/
│   ├── Category.java                 ← FOOD, GROCERIES, PHARMA, AUTO, TRANSPORT, LEISURE, OTHER
│   └── Transaction.java              ← @Entity JPA
├── repository/
│   └── TransactionRepository.java
├── strategy/
│   ├── MerchantCategoryRule.java     ← interface Strategy
│   ├── MerchantCategoryClassifier.java
│   └── impl/
│       ├── CoffeeShopRule.java       → FOOD
│       ├── GroceryRule.java          → GROCERIES
│       ├── PharmaRule.java           → PHARMA
│       ├── AutoRule.java             → AUTO
│       └── TransportRule.java        → TRANSPORT
└── usecase/
    ├── PersistTransactionUseCase.java
    ├── ListTransactionsByCategoryUseCase.java
    ├── ListTransactionsByEstablishmentUseCase.java
    ├── ListAllTransactionsUseCase.java
    ├── SummarizeMonthlyByCategoryUseCase.java
    └── DeleteTransactionUseCase.java
```

---

## Como Configurar e Rodar

### Pré-requisitos

- Java 21+
- Maven 3.9+
- MySQL 8 rodando (ou Docker)

### Subir MySQL com Docker

```bash
docker run -d \
  --name budgeting-db \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=budgeting \
  -p 3306:3306 \
  mysql:8
```

### Configurar variáveis

Edite `src/main/resources/application.yml` ou exporte as variáveis de ambiente:

```bash
export OPENAI_API_KEY=sua-chave-aqui
export DB_URL=jdbc:mysql://localhost:3306/budgeting
export DB_USER=root
export DB_PASS=root
```

### Compilar e rodar

```bash
mvn clean package
java -jar target/assistente-budgeting-0.0.1-SNAPSHOT.jar
```

---

## Endpoints REST

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/health` | Status da aplicação |
| `POST` | `/api/voice` | Enviar áudio (multipart) e receber resposta em áudio |
| `POST` | `/api/chat` | Enviar texto e receber resposta em texto |
| `POST` | `/api/transactions` | Registrar transação via JSON params |
| `GET` | `/api/transactions` | Listar todas ou filtrar por `?category=FOOD` |
| `GET` | `/api/transactions/by-establishment?name=` | Listar por estabelecimento |
| `DELETE` | `/api/transactions/{id}` | Excluir transação por UUID |

---

## Regras de Categorização Automática

O sistema usa o **Strategy Pattern**: cada regra é uma classe independente que verifica palavras-chave no nome do estabelecimento (case-insensitive).

| Classe | Palavras-chave | Categoria |
|--------|---------------|-----------|
| `CoffeeShopRule` | starbucks, café, cafeteria, coffee, espresso | `FOOD` |
| `GroceryRule` | mercado, supermercado, atacadão, carrefour, extra | `GROCERIES` |
| `PharmaRule` | farmácia, drogaria, ultrafarma, droga, panvel | `PHARMA` |
| `AutoRule` | posto, oficina, shell, ipiranga, auto peças | `AUTO` |
| `TransportRule` | uber, 99, táxi, metro, metrô, ônibus | `TRANSPORT` |

### Como adicionar uma nova regra

1. Crie uma classe em `strategy/impl/` implementando `MerchantCategoryRule`.
2. Anote com `@Component`.
3. O `MerchantCategoryClassifier` a detecta automaticamente via injeção de `List<MerchantCategoryRule>`.

---

## Ferramentas de IA Disponíveis (@Tool)

| Tool name | Descrição |
|-----------|-----------|
| `registrar-transacao` | Registra um gasto com valor, local e data/hora |
| `listar-por-categoria` | Lista transações de uma categoria |
| `listar-por-estabelecimento` | Lista transações por nome do local |
| `resumo-mensal-por-categoria` | Total gasto em uma categoria no mês informado |
| `excluir-transacao` | Exclui uma transação pelo UUID |

---

## Como o Sistema Extrai Valor, Local e Data/Hora

1. **Áudio** → transcrito pelo Whisper (via Feign) em texto.
2. **Texto** → enviado ao GPT-4o com o system prompt que instrui a extração das 3 entidades.
3. **GPT-4o** → chama a tool `registrar-transacao` com os parâmetros extraídos.
4. **PersistTransactionUseCase** aplica os fallbacks:
   - `occurredAt = null` → `Instant.now()`
   - `category = null` → `MerchantCategoryClassifier.classify(establishment)`
5. Transação salva no MySQL com todos os campos preenchidos.

---

## Nota sobre Migração de Schema em Produção

Em desenvolvimento, `spring.jpa.hibernate.ddl-auto: update` cria e atualiza as colunas automaticamente.

**Em produção**, use `validate` ou `none` e execute os scripts de migração manualmente (ou via Flyway/Liquibase):

```sql
CREATE TABLE transactions (
    id          CHAR(36)       NOT NULL PRIMARY KEY,
    description VARCHAR(255)   NOT NULL,
    amount      DECIMAL(10,2)  NOT NULL,
    establishment VARCHAR(255),
    occurred_at DATETIME(6)    NOT NULL,
    category    VARCHAR(50)    NOT NULL,
    created_at  DATETIME(6)
);
```

---

## Testes

```bash
mvn test
```

Cobertura dos testes:
- `MerchantCategoryClassifierTest` — 8 cenários (5 regras + null + empty + sem match)
- `PersistTransactionUseCaseTest` — fallback de `occurredAt` e `category`
- `BudgetingControllerTest` — endpoints REST com MockMvc

---

*Projeto de Finalização — Banco Itaú | Java com Inteligência Artificial*
