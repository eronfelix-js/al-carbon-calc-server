# 🌱 Calculadora de Carbono - Backend

Sistema backend para cálculo de pegada de carbono desenvolvido com **Java**, **Spring Boot** e **MongoDB**.

## 📋 Sobre o Projeto

Esta aplicação permite que usuários calculem sua pegada de carbono mensal baseada em:
- **Consumo de energia elétrica** (por estado brasileiro)
- **Tipo de transporte** e distância percorrida
- **Produção de resíduos sólidos** (considerando taxa de reciclagem)

---

## 🚀 Tecnologias Utilizadas

- **Java 11+**
- **Spring Boot 2.7+**
- **MongoDB** (via Docker)
- **Lombok** (redução de boilerplate)
- **Swagger/OpenAPI** (documentação de API)
- **JUnit 5 + Mockito** (testes unitários)
- **SLF4J** (logging)
- **Gradle** (build tool)

---

## 📦 Estrutura do Projeto

```
src/main/java/br/com/actionlabs/carboncalc/
├── config/              # Configurações (Swagger, Security, etc)
├── dto/                 # Data Transfer Objects
├── enums/               # Enumerações
├── exception/           # Exceções customizadas e handlers
│   ├── CalculationNotFoundException.java
│   └── GlobalExceptionHandler.java
├── model/               # Entidades/Modelos
│   ├── Calculation.java                    ⭐ NOVO
│   ├── EnergyEmissionFactor.java
│   ├── TransportationEmissionFactor.java
│   └── SolidWasteEmissionFactor.java
├── repository/          # Camada de acesso a dados
│   ├── CalculationRepository.java          ⭐ NOVO
│   ├── EnergyEmissionFactorRepository.java
│   ├── TransportationEmissionFactorRepository.java
│   └── SolidWasteEmissionFactorRepository.java
├── rest/                # Controllers REST
│   └── OpenRestController.java             ⭐ IMPLEMENTADO
├── service/             # Lógica de negócio
│   └── CarbonCalculatorService.java        ⭐ NOVO
└── CarbonCalculatorApplication.java

src/test/java/br/com/actionlabs/carboncalc/
└── service/
    └── CarbonCalculatorServiceTest.java    ⭐ NOVO
```

---

## 🔧 Como Executar

### 1. Pré-requisitos

- Java 11 ou superior
- Docker e Docker Compose
- Gradle (ou use o wrapper incluído)

### 2. Iniciar o MongoDB

```bash
docker compose up -d
```

O banco será populado automaticamente com os fatores de emissão (script `init-mongo.js`).

### 3. Executar a aplicação

**Via Gradle:**
```bash
./gradlew bootRun
```

**Via IDE:**
Execute a classe `CarbonCalculatorApplication`

A aplicação estará disponível em: **http://localhost:8085**

### 4. Acessar documentação Swagger

**URL:** http://localhost:8085/swagger-ui.html

---

## 📡 API Endpoints

### 1️⃣ POST /open/start-calc

Inicia um novo cálculo de carbono.

**Request:**
```json
{
  "name": "João Silva",
  "email": "joao@example.com",
  "phoneNumber": "11999999999",
  "uf": "SP"
}
```

**Response:** `201 Created`
```json
{
  "id": "63f1a2b3c4d5e6f7g8h9i0j1"
}
```

---

### 2️⃣ PUT /open/info

Atualiza informações de consumo do cálculo.

**Request:**
```json
{
  "id": "63f1a2b3c4d5e6f7g8h9i0j1",
  "energyConsumption": 250.0,
  "transportation": "car",
  "monthlyDistance": 500.0,
  "solidWasteProduction": 100.0,
  "recyclePercentage": 0.3
}
```

**Response:** `200 OK`

**Notas:**
- `recyclePercentage`: valor entre 0.0 e 1.0 (ex: 0.3 = 30% reciclável)
- Chamar múltiplas vezes sobrescreve os valores anteriores

---

### 3️⃣ GET /open/result/{id}

Retorna a pegada de carbono calculada.

**Response:** `200 OK`
```json
{
  "id": "63f1a2b3c4d5e6f7g8h9i0j1",
  "energy": 20.425,
  "transportation": 60.0,
  "solidWaste": 12.0,
  "total": 92.425
}
```

**Comportamento:**
- Se `/info` não foi chamado, retorna zeros
- Valores em kg CO₂

---

## 🧮 Lógica de Cálculo

### Energia
```
Emissão = consumo_energia × fator_emissão(UF)
```

**Exemplo:**
- Consumo: 250 kWh
- UF: SP (fator: 0.0817)
- Emissão: 250 × 0.0817 = **20.425 kg CO₂**

---

### Transporte
```
Emissão = distância_mensal × fator_emissão(tipo_transporte)
```

**Exemplo:**
- Distância: 500 km
- Tipo: car (fator: 0.12)
- Emissão: 500 × 0.12 = **60 kg CO₂**

---

### Resíduos Sólidos
```
Emissão_reciclável = resíduos × %_reciclável × fator_reciclável
Emissão_não_reciclável = resíduos × (1 - %_reciclável) × fator_não_reciclável
Emissão_total = Emissão_reciclável + Emissão_não_reciclável
```

**Exemplo:**
- Resíduos: 100 kg
- Reciclável: 30% (0.3)
- Fator reciclável: 0.05
- Fator não-reciclável: 0.15

Cálculo:
- Reciclável: 100 × 0.3 × 0.05 = 1.5 kg CO₂
- Não-reciclável: 100 × 0.7 × 0.15 = 10.5 kg CO₂
- **Total: 12 kg CO₂**

---

## 🧪 Testes

### Executar todos os testes:
```bash
./gradlew test
```

### Coverage:
Os testes cobrem:
- ✅ Criação de cálculo
- ✅ Atualização de informações
- ✅ Cálculo de emissão de energia
- ✅ Cálculo de emissão de transporte
- ✅ Cálculo de emissão de resíduos sólidos
- ✅ Cálculo de emissão total
- ✅ Tratamento de exceções
- ✅ Cenário de informações incompletas

---

## 📊 Logs

A aplicação possui logs estruturados em diferentes níveis:

- **INFO**: Operações principais (criação, atualização, resultados)
- **DEBUG**: Detalhes dos cálculos (valores intermediários)
- **WARN**: Situações anormais (fatores não encontrados, dados incompletos)
- **ERROR**: Erros críticos (cálculo não encontrado, exceções)

**Exemplo de log:**
```
INFO  - Starting new calculation for user: João Silva (joao@example.com)
INFO  - Calculation created successfully with id: 63f1a2b3
DEBUG - Energy emission: 250.0 kWh × 0.0817 = 20.425 kg CO2
INFO  - Carbon footprint calculated - Energy: 20.425, Transport: 60.0, Waste: 12.0, Total: 92.425
```

---

## ⚠️ Tratamento de Erros

### 400 Bad Request
Validação de dados falhou (campos obrigatórios, formatos inválidos).

```json
{
  "timestamp": "2024-03-09T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "must be a well-formed email address",
    "uf": "must not be blank"
  }
}
```

### 404 Not Found
Cálculo não encontrado.

```json
{
  "timestamp": "2024-03-09T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Calculation not found with id: invalid123"
}
```

---

## 🔄 Resetar Banco de Dados

Para restaurar o MongoDB ao estado inicial:

```bash
docker compose down -v
docker compose up -d
```

---

## 💡 Boas Práticas Implementadas

### Arquitetura
- ✅ Separação em camadas (Controller → Service → Repository)
- ✅ Injeção de dependências com `@RequiredArgsConstructor`
- ✅ DTOs para transferência de dados

### Código Limpo
- ✅ Uso de Lombok (`@Data`, `@Builder`, `@Slf4j`)
- ✅ Javadoc em classes e métodos principais
- ✅ Nomes descritivos e auto-explicativos
- ✅ Métodos pequenos e focados (SRP)

### Segurança e Robustez
- ✅ Validação com Bean Validation
- ✅ Exception handling global
- ✅ Exceptions customizadas
- ✅ Logs estruturados

### Qualidade
- ✅ Testes unitários com JUnit 5 + Mockito
- ✅ Documentação Swagger/OpenAPI
- ✅ Tratamento de casos extremos (dados nulos, fatores não encontrados)

---

## 🎯 Exemplo de Fluxo Completo

```bash
# 1. Iniciar cálculo
curl -X POST http://localhost:8085/open/start-calc \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Santos",
    "email": "maria@email.com",
    "phoneNumber": "11988887777",
    "uf": "RJ"
  }'
# Resposta: {"id": "abc123"}

# 2. Adicionar informações de consumo
curl -X PUT http://localhost:8085/open/info \
  -H "Content-Type: application/json" \
  -d '{
    "id": "abc123",
    "energyConsumption": 300.0,
    "transportation": "bus",
    "monthlyDistance": 400.0,
    "solidWasteProduction": 80.0,
    "recyclePercentage": 0.5
  }'

# 3. Obter resultado
curl http://localhost:8085/open/result/abc123
# Resposta: {"id":"abc123","energy":24.51,"transportation":48.0,"solidWaste":8.0,"total":80.51}
```

---

## 📝 Notas

- Os fatores de emissão no banco são **valores de teste**, não representam dados reais
- A aplicação roda na porta **8085** (não 8080)
- Todos os parâmetros em `/start-calc` são obrigatórios
- `recyclePercentage` deve estar entre 0.0 e 1.0

---

## 👨‍💻 Desenvolvedor

Implementado como teste técnico para Action Labs.

**Destaques da implementação:**
- Arquitetura limpa e extensível
- Código bem documentado
- Testes unitários abrangentes
- Logs para rastreabilidade
- Tratamento robusto de erros

---

## 📄 Licença

Projeto desenvolvido para fins de avaliação técnica.
