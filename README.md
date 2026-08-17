# BackEnd-FantAsta-PlayerService 🚀

Microservizio backend per la gestione dei giocatori del progetto **FantAsta**, sviluppato con **Spring Boot** e architettura a microservizi.

## 📋 Indice

- [Descrizione](#-descrizione)
- [Tecnologie](#-tecnologie)
- [Struttura del Progetto](#-struttura-del-progetto)
- [Prerequisiti](#-prerequisiti)
- [Installazione](#-installazione)
- [Configurazione](#-configurazione)
- [Esecuzione](#-esecuzione)
- [API Endpoints](#-api-endpoints)
- [Docker](#-docker)
- [Test](#-test)
- [Contributing](#-contributing)
- [License](#-license)

## 📖 Descrizione

Questo servizio gestisce tutte le operazioni relative ai giocatori del fantacalcio **FantAsta**, tra cui:

- CRUD completo dei giocatori
- Gestione statistiche e prestazioni
- Integrazione con altri microservizi del ecosistema FantAsta
- API RESTful per il frontend e altri servizi

## 🛠️ Tecnologie

| Tecnologia | Versione | Descrizione |
|------------|----------|-------------|
| **Java** | 17+ | Linguaggio di programmazione |
| **Spring Boot** | 3.x | Framework applicativo |
| **Spring Data JPA** | - | Persistenza dati |
| **Spring Web** | - | API REST |
| **Maven** | 3.8+ | Dependency management |
| **Docker** | - | Containerizzazione |
| **PostgreSQL/MySQL** | - | Database (configurabile) |

### Dipendenze Principali

- `spring-boot-starter-web` - Web application e API REST
- `spring-boot-starter-data-jpa` - ORM e gestione database
- `spring-boot-starter-validation` - Validazione input
- `lombok` - Riduzione boilerplate code
- `spring-boot-devtools` - Hot reload in sviluppo

## 📁 Struttura del Progetto

```
BackEnd-FantAsta-PlayerService/
├── src/main/java/com/example/fantasta/player_service/
│   ├── Client/              # REST client per comunicazione con altri microservizi
│   ├── Config/              # Configurazioni (CORS, Security, ecc.)
│   ├── Controller/          # REST API endpoints
│   ├── DTO/                 # Data Transfer Objects
│   ├── Entity/              # Entity JPA/Database models
│   ├── Exceptions/          # Gestione eccezioni personalizzate
│   ├── Repository/          # Data access layer
│   ├── Service/             # Business logic layer
│   └── PlayerServiceApplication.java  # Main class
├── src/main/resources/
│   ├── application.properties  # Configurazione applicativa
│   └── application.yml         # Configurazione YAML (opzionale)
├── Dockerfile               # Configurazione Docker
├── pom.xml                  # Maven dependencies
└── mvnw                     # Maven wrapper
```

## ⚙️ Prerequisiti

Assicurati di avere installato:

- **Java JDK 17** o superiore
- **Maven 3.8+** (o usa il Maven wrapper incluso)
- **Docker** (opzionale, per containerizzazione)
- **Database** (PostgreSQL/MySQL - configurabile in `application.properties`)

## 📥 Installazione

### 1. Clona il Repository

```bash
git clone https://github.com/francesc05Cervera/BackEnd-FantAsta-PlayerService.git
cd BackEnd-FantAsta-PlayerService
```

### 2. Installa le Dipendenze

```bash
# Usando Maven wrapper (consigliato)
./mvnw clean install

# Oppure con Maven globale
mvn clean install
```

## ⚙️ Configurazione

### Database

Configura il database in `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/fantasta_players
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Server Port

```properties
server.port=8080
```

### Altri Microservizi

Configura gli URL degli altri servizi FantAsta se necessario:

```properties
# Esempio: URL servizio aste
fantasta.auction-service.url=http://localhost:8081
```

## 🚀 Esecuzione

### Sviluppo Locale

```bash
# Usando Maven wrapper
./mvnw spring-boot:run

# Oppure con Maven globale
mvn spring-boot:run

# Oppure esegui il JAR
./mvnw clean package
java -jar target/player-service-0.0.1-SNAPSHOT.jar
```

L'applicazione sarà disponibile su: `http://localhost:8080`

## 🔌 API Endpoints

### Player Controller

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/players` | Lista tutti i giocatori |
| `GET` | `/api/players/{id}` | Dettaglio giocatore per ID |
| `POST` | `/api/players` | Crea nuovo giocatore |
| `PUT` | `/api/players/{id}` | Aggiorna giocatore esistente |
| `DELETE` | `/api/players/{id}` | Elimina giocatore |
| `GET` | `/api/players/search` | Cerca giocatori (con filtri) |

### Esempio Richieste

#### Ottenere tutti i giocatori

```bash
curl -X GET http://localhost:8080/api/players
```

#### Creare un nuovo giocatore

```bash
curl -X POST http://localhost:8080/api/players \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nome Giocatore",
    "team": "Serie A Team",
    "role": "ATT",
    "price": 15.5
  }'
```

## 🐳 Docker

### Build dell'Immagine

```bash
docker build -t fantasta-player-service .
```

### Esecuzione Container

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/fantasta_players \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  fantasta-player-service
```

### Docker Compose (consigliato)

Crea un file `docker-compose.yml`:

```yaml
version: '3.8'
services:
  player-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/fantasta_players
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    depends_on:
      - db

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=fantasta_players
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Esegui con:

```bash
docker-compose up -d
```

## 🧪 Test

### Esegui tutti i test

```bash
./mvnw test
```

### Test con Coverage

```bash
./mvnw clean test jacoco:report
```

Il report sarà disponibile in `target/site/jacoco/index.html`

## 🤝 Contributing

1. Forka il progetto
2. Crea un branch per la tua feature (`git checkout -b feature/AmazingFeature`)
3. Commit delle modifiche (`git commit -m 'Add some AmazingFeature'`)
4. Push sul branch (`git push origin feature/AmazingFeature`)
5. Apri una Pull Request

### Linee Guida

- Segui le convenzioni di codice Java/Spring Boot
- Scrivi test per le nuove funzionalità
- Documenta le API nel README o con OpenAPI/Swagger
- Mantieni i commit atomici e descrittivi

## 📄 License

Questo progetto è parte del progetto **FantAsta** e viene distribuito sotto licenza MIT.

## 👨‍💻 Autore

**Francesco Cervera** - [francesc05Cervera](https://github.com/francesc05Cervera)

## 🔗 Link Utili

- [Progetto FantAsta](https://github.com/francesc05Cervera/FantAsta)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Documentation](https://maven.apache.org/guides/index.html)

---

**Built with ❤️ using Spring Boot**
