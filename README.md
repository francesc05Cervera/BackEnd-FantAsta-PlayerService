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

Per l'integrazione reale configura `AUTH_SERVICE_URL` con l'URL pubblico di AuthService e `APP_CORS_ALLOWEDORIGINS` con le origini Angular, per esempio `http://localhost:4200,https://app.example.com`. Le operazioni di creazione, modifica, importazione ed eliminazione richiedono un utente con ruolo `ADMIN`, assegnato solo da `AUTH_ADMINEMAILS` in AuthService. Gli endpoint di lettura richiedono un token Bearer valido.

### Sincronizzazione Serie A con API-Football

La sincronizzazione e' disattivata per impostazione predefinita e non inserisce mai chiavi nel repository. Crea un account su [API-Football](https://dashboard.api-football.com/register), genera una chiave nel dashboard e configura Render o l'ambiente locale:

```bash
API_FOOTBALL_ENABLED=true
API_FOOTBALL_KEY=la-tua-chiave
API_FOOTBALL_LEAGUE_ID=135
API_FOOTBALL_SEASON=2026
# opzionali
API_FOOTBALL_CRON="0 0 3 * * MON"
API_FOOTBALL_CONNECT_TIMEOUT_MS=3000
API_FOOTBALL_READ_TIMEOUT_MS=10000
API_FOOTBALL_MIN_REQUEST_INTERVAL_MS=6500
```

`API_FOOTBALL_LEAGUE_ID=135` identifica la Serie A. La stagione deve essere verificata nel dashboard/API per il piano associato alla chiave: il piano Free fornisce 100 richieste al giorno, ha un limite di 10 richieste al minuto e puo' limitare le stagioni disponibili. La sincronizzazione settimanale usa `/teams?league=&season=` e poi `/players/squads?team=`: con 20 squadre esegue circa 21 richieste e l'intervallo predefinito di 6,5 secondi resta sotto il limite al minuto.

L'endpoint manuale e':

```text
POST /api/players/sync
Authorization: Bearer <JWT ADMIN>
```

Restituisce `{ "inserted", "updated", "unchanged", "total" }`. Solo `ADMIN` puo' avviarlo; gli utenti `USER` restano in sola lettura. La sincronizzazione usa l'identificativo API-Football come chiave esterna, aggiorna i trasferimenti senza cambiare l'ID FantAsta, non elimina giocatori assenti dalla rosa corrente e non avvia due esecuzioni insieme. Un errore o una risposta incompleta dell'API interrompe la transazione e mantiene i dati esistenti.

I ruoli API sono normalizzati in modo compatibile con AuctionService: `Goalkeeper -> P`, `Defender -> D`, `Midfielder -> C`, `Attacker -> A`. Gli input manuali/CSV accettano anche i nomi italiani precedenti e vengono normalizzati ai quattro codici. Per i record CSV precedenti, l'ID esterno viene collegato soltanto se nome e cognome normalizzati identificano un singolo candidato; una corrispondenza ambigua interrompe la sincronizzazione senza scrivere dati.

La migrazione Flyway `V1__add_api_football_player_id.sql` e' additiva: aggiunge `api_football_player_id` e il relativo indice univoco, lasciando nullo il campo per i dati preesistenti. Non modifica gli ID interni usati da AuctionService e non elimina dati.

### PostgreSQL condiviso su Render

Se i servizi condividono un PostgreSQL con PgBouncer in `session mode`, configura pool piccoli per non superare il limite di connessioni del piano:

```text
DB_POOL_MAX_SIZE=3
DB_POOL_MIN_IDLE=0
DB_CONNECTION_TIMEOUT_MS=10000
```

I valori sono gia' i default del servizio; le variabili servono solo per personalizzarli. Ogni microservizio deve avere un budget di connessioni coerente con il limite complessivo del database.

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
