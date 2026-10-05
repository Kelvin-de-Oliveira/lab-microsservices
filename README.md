# Laboratório de Microsserviços: Pedido, Estoque e Pagamento

Plataforma de comércio eletrônico composta por três microsserviços independentes, cada um com seu próprio banco de dados. A comunicação síncrona (Pedido para Estoque) usa REST, e a assíncrona (Pedido para Pagamento) usa RabbitMQ.

## 1. Estado atual do projeto

Implementado até a **Etapa 6**.

| Etapa | Descrição | Situação |
|---|---|---|
| 1 | Estoque Service | Implementada |
| 2 | Pedido Service | Implementada |
| 3 | Integração REST (Pedido chama Estoque) e experimento de consistência | Implementada |
| 4 | Docker Compose | Implementado |
| 5 | RabbitMQ (exchange, fila e publicação do evento) | Implementada |
| 6 | Pagamento Service (consumo do evento) | Implementada |
| 7 a 10 | Testes funcionais, falha, recuperação e escalabilidade | Execuções pendentes  |
| 11 | Observabilidade (`correlationId` nos logs de todos os serviços) | **Não implementada** |
| 12 | Atualização assíncrona do pedido (`pagamento.processado`) | **Não implementada** |

Consequências do estado atual, que são esperadas nas execuções:

* O pedido permanece em `AGUARDANDO_PAGAMENTO` mesmo depois de o pagamento ser processado, pois o Pagamento ainda não publica o resultado.
* O `correlationId` é gerado pelo Pedido e viaja apenas dentro do evento `pedido.criado`. O Estoque não o recebe, e o Pedido ainda não registra os logs de negócio da Etapa 11. O único log com `correlationId` hoje é o do Pagamento.
* A fila `pagamento.processado` não existe.

## 2. Arquitetura

```
Cliente  ──REST──>  pedido-service  ──REST──>  estoque-service
                         │                          │
                         │ pedidos.exchange         estoque-db
                         │ routing key: pedido.criado
                         ▼
                      RabbitMQ (fila pedido.criado)
                         │
                         ▼
                   pagamento-service
                         │
                    pagamento-db
```

Fluxo de `POST /pedidos`:

1. O Pedido gera um `correlationId`.
2. O Pedido chama `PUT /produtos/{id}/reservar` no Estoque.
3. Se a reserva der certo, o Pedido salva o pedido com status `AGUARDANDO_PAGAMENTO`.
4. O Pedido publica o evento `pedido.criado` em `pedidos.exchange`.
5. O Pagamento consome o evento, sorteia o resultado (cerca de 80% aprovado e 20% rejeitado), grava no seu banco e registra no log.

Se o Estoque recusar a reserva (estoque insuficiente ou produto inexistente), o pedido não é criado e nenhum evento é publicado.

Regras da arquitetura: cada serviço tem seu próprio banco, e nenhum serviço consulta o banco de outro.

### Serviços e portas

| Serviço | Porta no host | Porta no container | Banco |
|---|---|---|---|
| pedido-service | 8080 | 8080 | pedido-db |
| estoque-service | 8081 | 8080 | estoque-db |
| pagamento-service | nenhuma (apenas `expose`) | 8080 | pagamento-db |
| rabbitmq | 5672 (AMQP) e 15672 (interface web) | 5672 e 15672 | não se aplica |

Os bancos PostgreSQL não publicam porta no host. Para consultá-los, use `docker compose exec` (seção 8).

## 3. Tecnologias

* Spring Boot 3.3.5, Spring Web, Spring Data JPA e Spring AMQP
* PostgreSQL 16 e RabbitMQ 3.13 (com interface de gerenciamento)
* Maven, Docker e Docker Compose
* Java 21 no Estoque e no Pagamento, e Java 17 no Pedido (cada serviço tem seu Dockerfile compatível)
* Lombok nos serviços que o utilizam

## 4. Pré-requisitos

* Docker Desktop com Docker Compose v2
* PowerShell (os comandos deste guia são para Windows)
* Portas livres no host: 8080, 8081, 5672 e 15672
* Opcional, para rodar fora do Docker: JDK 21 e Maven

## 5. Estrutura do repositório

```
lab-prova-01/
├── docs/                       (diagrama de arquitetura)
├── docker-compose.yml          (todos os serviços, bancos e RabbitMQ)
├── docker-compose.dev.yml      (apenas os bancos; uso opcional de desenvolvimento, não é necessário para as execuções)
├── estoque-service/
├── pedido-service/
└── pagamento-service/
```

## 6. Como executar (Docker Compose)

Na raiz do projeto:

```
docker compose up --build
```

Aguarde os logs `Started ...Application` dos três serviços. Em outro terminal, confira os containers:

```
docker compose ps
```

Para acompanhar os logs de um serviço:

```
docker compose logs pedido-service
docker compose logs estoque-service
docker compose logs pagamento-service
```

Para encerrar:

```
docker compose down        # mantém os dados dos bancos
docker compose down -v     # apaga os dados (o estoque volta ao valor inicial)
```

Observações:

* Logo depois do `docker compose up`, um `POST /pedidos` pode responder 503, porque o Estoque ainda pode estar iniciando. Aguarde alguns segundos e repita.
* Os três serviços são reconstruídos com `--build`. Sempre use essa opção depois de alterar o código.
* Para as execuções, use apenas o `docker-compose.yml`. Ele não deve rodar junto com o `docker-compose.dev.yml`, pois compartilham o mesmo nome de projeto e os mesmos nomes de serviço. Se o dev estiver no ar, rode `docker compose -f docker-compose.dev.yml down` antes.

## 7. Endpoints

### Estoque (`http://localhost:8081`)

| Método e rota | Descrição | Respostas |
|---|---|---|
| `GET /produtos` | Lista os produtos | 200 |
| `GET /produtos/{id}` | Consulta um produto | 200 ou 404 |
| `PUT /produtos/{id}/reservar` | Reserva estoque. Body: `{"quantidade": 2}` | 200, 409 (`Estoque insuficiente!`), 404 (`Produto não encontrado!`) ou 400 (quantidade inválida) |

Dados iniciais:

| id | nome | quantidade |
|---|---|---|
| 1 | Notebook | 10 |
| 2 | Mouse | 50 |
| 3 | Teclado | 20 |

### Pedido (`http://localhost:8080`)

| Método e rota | Descrição | Respostas |
|---|---|---|
| `POST /pedidos` | Cria um pedido. Body: `{"produtoId": 1, "quantidade": 2}` | 201, 409 ou 404 (repassados do Estoque, com o mesmo corpo), 503 (`Estoque indisponível`) |
| `GET /pedidos` | Lista os pedidos | 200 |
| `GET /pedidos/{id}` | Consulta um pedido | 200 ou 404 (sem corpo) |

Status possíveis do pedido: `AGUARDANDO_PAGAMENTO`, `PAGO` e `REJEITADO`. No estado atual, apenas `AGUARDANDO_PAGAMENTO` é usado. 

### Pagamento

Não possui endpoints. Ele só consome eventos do RabbitMQ.

## 8. Comandos úteis

### Chamadas HTTP

**PowerShell (Windows).** Use `curl.exe` (no PowerShell, `curl` é um alias de `Invoke-WebRequest`) e escape as aspas do JSON:

```powershell
# Listar produtos e pedidos
Invoke-RestMethod http://localhost:8081/produtos
Invoke-RestMethod http://localhost:8080/pedidos

# Criar pedido
curl.exe -i -X POST http://localhost:8080/pedidos -H "Content-Type: application/json" -d '{\"produtoId\":2,\"quantidade\":1}'
```

**Terminal (macOS ou Linux):**

```bash
# Listar produtos e pedidos
curl -s http://localhost:8081/produtos
curl -s http://localhost:8080/pedidos

# Criar pedido
curl -i -X POST http://localhost:8080/pedidos -H "Content-Type: application/json" -d '{"produtoId":2,"quantidade":1}'
```

Bodies do `POST /pedidos` para os casos de negócio:

| Caso | Body (macOS ou Linux) | Body (PowerShell) | Resposta |
|---|---|---|---|
| Com estoque | `{"produtoId":2,"quantidade":1}` | `'{\"produtoId\":2,\"quantidade\":1}'` | 201 |
| Estoque insuficiente | `{"produtoId":1,"quantidade":999}` | `'{\"produtoId\":1,\"quantidade\":999}'` | 409 |
| Produto inexistente | `{"produtoId":99,"quantidade":1}` | `'{\"produtoId\":99,\"quantidade\":1}'` | 404 |

Consulta de um produto ou de um pedido por id, nos dois sistemas:

```
# PowerShell
Invoke-RestMethod http://localhost:8081/produtos/1
curl.exe -i http://localhost:8080/pedidos/1

# macOS ou Linux
curl -s http://localhost:8081/produtos/1
curl -i http://localhost:8080/pedidos/1
```

### Bancos de dados 

```
docker compose exec estoque-db psql -U estoque -d estoque -c "SELECT * FROM produto ORDER BY id;"
docker compose exec pedido-db psql -U pedido -d pedido -c "SELECT * FROM pedido;"
docker compose exec pagamento-db psql -U pagamento -d pagamento -c "SELECT * FROM pagamento;"
```

### RabbitMQ

* Interface web: `http://localhost:15672` (usuário `guest` e senha `guest`).
* Exchange `pedidos.exchange` (tipo direct) e fila durável `pedido.criado`, ligadas pela routing key `pedido.criado`.
* Pela linha de comando (igual nos dois sistemas):

```
docker compose exec rabbitmq rabbitmqctl list_exchanges name type
docker compose exec rabbitmq rabbitmqctl list_queues name messages consumers
```

Formato do evento `pedido.criado`:

```json
{
  "pedidoId": 10,
  "produtoId": 1,
  "quantidade": 2,
  "correlationId": "b1f6c1a2-5c5e-4c39-9f52-0d3b0d2c9d10"
}
```


## 9. Experimento de consistência (Etapa 3)

O Pedido possui uma chave que simula uma falha logo depois da reserva do estoque e antes da criação do pedido. Ela é controlada pela variável de ambiente `SIMULAR_FALHA_APOS_RESERVA`. Sem a variável, o valor padrão é `false` e o Pedido funciona normalmente. Com o valor `true`, o `POST /pedidos` responde 500 depois de reservar o estoque, sem criar o pedido.

No Docker Compose, a variável é definida na seção `environment` do `pedido-service`, em `docker-compose.yml`.

## 10. Limitações conhecidas

* O status do pedido não é atualizado após o pagamento (Etapa 12 pendente).
* O `correlationId` não é propagado ao Estoque, e os logs da Etapa 11 ainda não existem.
* Não há compensação automática: se o pedido falhar depois da reserva, o estoque fica reduzido sem pedido.
* Um pagamento rejeitado não devolve o estoque reservado.
* A reserva do Estoque não usa bloqueio, então requisições simultâneas ao mesmo produto podem ler o mesmo saldo.

## 11. Solução de problemas

| Problema | Causa provável |
|---|---|
| `port is already allocated` | Outro processo usa 8080, 8081, 5672 ou 15672 |
| 503 logo após subir | O Estoque ainda está iniciando |
| Código alterado sem efeito | A imagem não foi reconstruída: use `--build` |
| Estoque ou pedidos com dados antigos | Os volumes dos bancos persistem: `docker compose down -v` os apaga |
| Erro de YAML no compose | `docker compose config` mostra a linha com problema |