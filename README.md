# 📊 BDReplicável - Replicação de Banco de Dados com Separação de Leitura/Escrita

## 📝 Descrição

Este projeto implementa um padrão de **replicação de banco de dados com separação de leitura e escrita** (Read/Write Splitting), desenvolvido como trabalho acadêmico para demonstrar os conceitos de escalabilidade e performance em aplicações Java com Spring Boot.

O sistema utiliza uma arquitetura master-slave onde:
- **Primário (Master)**: Recebe todas as operações de **escrita** (INSERT, UPDATE, DELETE)
- **Réplicas (Slaves)**: Processam todas as operações de **leitura** (SELECT)

Esta arquitetura melhora a performance e a escalabilidade através da distribuição de carga entre múltiplas instâncias do banco de dados.

---

## 🎯 Objetivos do Projeto

✅ Implementar roteamento automático de queries para primário/réplicas  
✅ Demonstrar conceitos de replicação de dados  
✅ Validar performance com separação de leitura/escrita  
✅ Usar AOP (Aspect-Oriented Programming) para interceptar transações  
✅ Gerar dados de teste automaticamente

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    Aplicação Spring Boot                    │
│  (ReplicacaoApplication + EnableScheduling)                 │
└────────────┬──────────────────────────┬──────────────────────┘
             │                          │
             ▼                          ▼
    ┌────────────────────┐    ┌─────────────────────┐
    │  DataSourceAspect  │    │   DataSourceConfig  │
    │  (AOP Interceptor) │    │  (Configuração)     │
    └────────────┬───────┘    └──────────┬──────────┘
                 │                       │
         ┌───────▼───────────────────────▼──────────┐
         │     RoutingDataSource                     │
         │  (Inteligência de Roteamento)             │
         │  - Load Balance entre Réplicas            │
         └───────┬──────────────────┬────────────────┘
                 │                  │
         ┌───────▼────────┐  ┌──────▼────────────┐
         │  BD Primário   │  │  BD Réplicas      │
         │   (ESCRITA)    │  │  (LEITURA)        │
         │   Master       │  │  Slave 1, 2, ... │
         └────────────────┘  └───────────────────┘
```

---

## 📦 Estrutura de Pacotes

```
com.grupo5.replicacaobd/
├── ReplicacaoApplication.java          # Classe principal da aplicação
├── config/
│   ├── DataSourceConfig.java            # Configuração dos DataSources
│   ├── DataSourceType.java              # Enum: PRIMARY, REPLICA
│   ├── DataSourceContextHolder.java     # ThreadLocal para contexto
│   ├── DataSourceAspect.java            # AOP para roteamento automático
│   └── RoutingDataSource.java           # AbstractRoutingDataSource
├── controller/
│   └── ApiController.java               # REST endpoints para consultas
├── model/
│   ├── Cliente.java                     # Entidade Cliente
│   ├── Produto.java                     # Entidade Produto
│   ├── Pedido.java                      # Entidade Pedido
│   └── PedidoItem.java                  # Entidade Item do Pedido
├── repository/
│   ├── ClienteRepository.java           # DAO para Cliente
│   ├── ProdutoRepository.java           # DAO para Produto
│   ├── PedidoRepository.java            # DAO para Pedido
│   └── PedidoItemRepository.java        # DAO para PedidoItem
└── service/
    ├── QueryService.java                # Serviço de consultas (Leitura)
    └── DataGeneratorService.java        # Gerador de dados de teste
```

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão | Propósito |
|-----------|--------|----------|
| Java | 17 | Linguagem de programação |
| Spring Boot | 3.2.5 | Framework principal |
| Spring Data JPA | - | ORM e persistência |
| Spring AOP | - | Aspect-Oriented Programming |
| MySQL | 8.0+ | Banco de dados |
| Lombok | - | Redução de boilerplate |
| Maven | 3.6+ | Gerenciador de dependências |
| dotenv-java | 3.0.0 | Carregamento de variáveis de ambiente |

---

## ⚙️ Configuração e Instalação

### 1. Pré-requisitos

- ✅ Java 17 ou superior
- ✅ Maven 3.6+
- ✅ MySQL 8.0+
- ✅ Mínimo 3 instâncias MySQL (1 primária + 2+ réplicas)

### 2. Configurar Replicação MySQL

#### Na máquina do BD Primário:

```sql
-- Editar /etc/mysql/mysql.conf.d/mysqld.cnf
[mysqld]
server-id = 1
log_bin = mysql-bin
binlog_format = ROW

-- Reiniciar MySQL
sudo systemctl restart mysql

-- Criar usuário replicação
CREATE USER 'replication_user'@'%' IDENTIFIED BY 'password';
GRANT REPLICATION SLAVE ON *.* TO 'replication_user'@'%';
FLUSH PRIVILEGES;

SHOW MASTER STATUS;
```

#### Na máquina do BD Réplica:

```sql
-- Editar /etc/mysql/mysql.conf.d/mysqld.cnf
[mysqld]
server-id = 2
relay-log = mysql-relay-bin
read_only = ON

-- Reiniciar MySQL
sudo systemctl restart mysql

CHANGE MASTER TO
  MASTER_HOST='192.168.1.100',
  MASTER_USER='replication_user',
  MASTER_PASSWORD='password',
  MASTER_LOG_FILE='mysql-bin.000001',
  MASTER_LOG_POS=1234;

START SLAVE;
SHOW SLAVE STATUS\G
```

### 3. Executar Script de Schema

```bash
mysql -u root -p < schema.sql
```

Este script cria:
- Banco de dados `aula-db`
- Tabelas: `cliente`, `produto`, `pedido`, `pedido_item`

### 4. Configurar Variáveis de Ambiente

Criar arquivo `.env` na raiz do projeto:

```env
# Banco Primário (Escrita)
DB_PRIMARY_HOST=localhost:3306

# Bancos Réplica (Leitura) - separados por vírgula
DB_REPLICA_HOSTS=localhost:3307,localhost:3308

# Credenciais
DB_USER=root
DB_PASS=password
DB_NAME=aula-db

# Servidor
SERVER_PORT=8080
```

### 5. Compilar e Executar

```bash
# Compilar projeto
mvn clean install

# Executar aplicação
mvn spring-boot:run

# Ou executar o JAR
java -jar target/replicacao-bd-1.0-SNAPSHOT.jar
```

---

## 🔑 Componentes Principais

### 1. **DataSourceType** (Enum)
Define os tipos de datasource:
```java
public enum DataSourceType {
    PRIMARY,   // Para operações de ESCRITA
    REPLICA    // Para operações de LEITURA
}
```

### 2. **DataSourceContextHolder** (ThreadLocal)
Armazena o tipo de datasource por thread:
```java
public class DataSourceContextHolder {
    private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();
    
    public static void set(DataSourceType dataSourceType) { ... }
    public static DataSourceType get() { ... }
    public static void clear() { ... }
}
```

### 3. **DataSourceAspect** (AOP)
Intercepta transações e define o datasource automaticamente:
```java
@Aspect
@Component
@Order(0)
public class DataSourceAspect {
    @Around("@annotation(transactional)")
    public Object proceed(ProceedingJoinPoint proceedingJoinPoint, 
                         Transactional transactional) throws Throwable {
        if (transactional.readOnly()) {
            DataSourceContextHolder.set(DataSourceType.REPLICA);
        } else {
            DataSourceContextHolder.set(DataSourceType.PRIMARY);
        }
        // ...
    }
}
```

**Funcionamento:**
- Se `@Transactional(readOnly = true)` → Usa RÉPLICA
- Se `@Transactional` (padrão) → Usa PRIMÁRIO

### 4. **RoutingDataSource** (AbstractRoutingDataSource)
Roteia queries para o datasource correto:
```java
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = DataSourceContextHolder.get();
        if (type == DataSourceType.REPLICA && replicaCount > 0) {
            int index = counter.getAndIncrement() % replicaCount;
            return "REPLICA_" + index;
        }
        return "PRIMARY";
    }
}
```

**Estratégia:** Round-robin entre réplicas para load balancing.

### 5. **DataSourceConfig** (Configuration)
Configura múltiplos datasources:
```java
@Bean
@Primary
public DataSource dataSource() {
    // Cria datasources primário + réplicas
    // Define RoutingDataSource como padrão
}
```

---

## 📊 Modelo de Dados

```sql
-- CLIENTE
┌─────────────────────────────────┐
│ cliente                         │
├─────────────────────────────────┤
│ id (PK)                         │
│ nome                            │
│ email (UNIQUE)                  │
│ criado_em (DATETIME)            │
│ criado_por (VARCHAR)            │
└─────────────────────────────────┘

-- PRODUTO
┌─────────────────────────────────┐
│ produto                         │
├─────────────────────────────────┤
│ id (PK)                         │
│ descricao                       │
│ categoria                       │
│ valor (NUMERIC)                 │
│ estoque (INT)                   │
│ criado_em (DATETIME)            │
│ criado_por (VARCHAR)            │
└─────────────────────────────────┘

-- PEDIDO
┌─────────────────────────────────┐
│ pedido                          │
├─────────────────────────────────┤
│ id (PK)                         │
│ cliente_id (FK)                 │
│ valor_total (NUMERIC)           │
│ status (VARCHAR)                │
│ criado_em (DATETIME)            │
│ criado_por (VARCHAR)            │
└─────────────────────────────────┘
       │
       │ 1:N
       ▼
┌─────────────────────────────────┐
│ pedido_item                     │
├─────────────────────────────────┤
│ id (PK)                         │
│ pedido_id (FK)                  │
│ produto_id (FK)                 │
│ quantidade (INT)                │
│ valor_unitario (NUMERIC)        │
└─────────────────────────────────┘
```

---

## 🔄 Fluxo de Funcionamento

### Fluxo de Escrita (PRIMÁRIO)

```
┌───────────────────────────────────────────────────────────┐
│ 1. DataGeneratorService executa @Scheduled(fixedDelay)   │
│    a cada 15 segundos                                     │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
┌───────────────────────────────────────────────────────────┐
│ 2. Cria CLIENTE novo                                      │
│    @Transactional (readOnly = false, implícito)          │
│    → DataSourceContextHolder.set(PRIMARY)                │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
┌───────────────────────────────────────────────────────────┐
│ 3. Cria PRODUTO novo                                      │
│    @Transactional (readOnly = false, implícito)          │
│    → Usa PRIMARY                                          │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
┌───────────────────────────────────────────────────────────┐
│ 4. Cria PEDIDO com ITENS                                 │
│    @Transactional (readOnly = false, implícito)          │
│    → Usa PRIMARY                                          │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
┌───────────────────────────────────────────────────────────┐
│ 5. Aguarda 1 segundo para replicação                     │
│    (sincronização do MySQL binlog)                        │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
    ✅ DADOS INSERIDOS NO PRIMÁRIO
    ✅ REPLICADOS PARA TODAS AS RÉPLICAS
```

### Fluxo de Leitura (RÉPLICAS)

```
┌───────────────────────────────────────────────────────────┐
│ 1. QueryService.getPedidoById()                           │
│    @Transactional(readOnly = true)                       │
│    → DataSourceContextHolder.set(REPLICA)                │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
┌───────────────────────────────────────────────────────────┐
│ 2. RoutingDataSource.determineCurrentLookupKey()         │
│    → Round-robin entre REPLICA_0, REPLICA_1, ...         │
│    (AtomicInteger counter++ % replicaCount)              │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
┌───────────────────────────────────────────────────────────┐
│ 3. SELECT executado em uma das RÉPLICAS                  │
│    (load balancing automático)                            │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
    ✅ DADOS RETORNADOS DA RÉPLICA
    ✅ SEM SOBRECARREGAR O PRIMÁRIO
```

---

## 🔌 Endpoints REST

### 1. Consultar Pedido por ID

```http
GET /pedidos/{id}
```

**Exemplo:**
```bash
curl http://localhost:8080/pedidos/1
```

**Resposta:**
```json
{
  "id": 1,
  "cliente": {
    "id": 1,
    "nome": "João Silva",
    "email": "joao.silva@grupo5.com"
  },
  "valorTotal": 2999.99,
  "status": "FINALIZADO",
  "itens": [
    {
      "id": 1,
      "produto": {
        "id": 1,
        "descricao": "Notebook Dell",
        "categoria": "Informática",
        "valor": 2999.99,
        "estoque": 15
      },
      "quantidade": 1,
      "valorUnitario": 2999.99
    }
  ]
}
```

### 2. Histórico de Pedidos por Cliente

```http
GET /clientes/{id}/pedidos
```

**Exemplo:**
```bash
curl http://localhost:8080/clientes/1/pedidos
```

### 3. Produtos com Baixo Estoque

```http
GET /produtos/baixo-estoque
```

Retorna produtos com estoque < 10 unidades.

### 4. Relatório de Vendas

```http
GET /relatorios/vendas
```

**Resposta:**
```json
{
  "total_pedidos": 42,
  "valor_total_vendas": 125780.50
}
```

---

## 📈 Monitoramento e Logs

A aplicação gera logs detalhados do processo:

```
--- [FLUXO DE ESCRITA - PRIMÁRIO] ---
Cliente Inserido: [ID: 5, Nome: Maria Oliveira, Email: maria.oliveira234@grupo5.com]
Produto Inserido: [ID: 12, Descrição: Mouse Gamer, Valor: R$ 89.99, Estoque: 23]
Pedido Gravado: [ID: 8, Cliente: Maria Oliveira, Valor Total: 89.99, Qtd Itens: 1]

--- [FLUXO DE LEITURA - RÉPLICAS] ---
Consulta Pedido: ID 8, Cliente: Maria Oliveira, Valor: 89.99, Status: FINALIZADO
  > Item: Mouse Gamer, Quantidade: 1
Histórico do Cliente (ID 5):
  - Pedido 8 - R$ 89.99
Relatório Agregado:
  - Quantidade total de pedidos: 42
  - Valor total vendido: R$ 125780.50
-------------------------------------
```

---

## 🧪 Testes e Validação

### Verificar Replicação no MySQL

**Primário:**
```bash
mysql> SHOW MASTER STATUS;
```

**Réplica:**
```bash
mysql> SHOW SLAVE STATUS\G
```

Procure por:
- `Seconds_Behind_Master: 0` (em sincronia)
- `Last_Errno: 0` (sem erros)

### Teste de Load Balancing

Os logs mostram o round-robin entre réplicas:

```
Consulta 1: REPLICA_0
Consulta 2: REPLICA_1
Consulta 3: REPLICA_0
Consulta 4: REPLICA_1
...
```

---

## 🎓 Conceitos Demonstrados

| Conceito | Implementação |
|----------|---------------|
| **Read/Write Splitting** | RoutingDataSource + DataSourceAspect |
| **Load Balancing** | Round-robin em RoutingDataSource |
| **AOP (Aspect-Oriented Programming)** | DataSourceAspect intercepta @Transactional |
| **ThreadLocal** | DataSourceContextHolder isola contexto por thread |
| **AbstractRoutingDataSource** | Roteia queries baseado em contexto |
| **Spring Data JPA** | Repositories para acesso a dados |
| **Replicação MySQL** | Master-Slave com binlog |
| **Scheduled Tasks** | @Scheduled para geração automática de dados |

---

## 📚 Referências

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [MySQL Replication](https://dev.mysql.com/doc/refman/8.0/en/replication.html)
- [Spring AOP](https://spring.io/projects/spring-framework)
- [AbstractRoutingDataSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/lookup/AbstractRoutingDataSource.html)

---

## 👥 Integrantes do Grupo 5

| Nome | Matrícula |
|------|-----------|
| Dan Benedetti | - |
| - | - |
| - | - |

---

## 📅 Informações do Projeto

- **Disciplina:** Banco de Dados
- **Professor:** Fábio
- **Data de Criação:** Junho 2026
- **Status:** ✅ Completo

---

## 📄 Licença

Este projeto é fornecido como material educacional para fins acadêmicos.

---

## ❓ Dúvidas e Suporte

Para dúvidas sobre o projeto, consulte:
1. Documentação inline no código
2. Logs da aplicação
3. Documentação MySQL Replication oficial

---

**Última Atualização:** Junho 2026
