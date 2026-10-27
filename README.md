# SecTec API

Backend Spring Boot della piattaforma. Java 25, Spring Boot 4.1.2, Maven 3.10.0
tramite Wrapper e PostgreSQL 17.11. Packaging JAR.

## Prerequisiti

- JDK 25 selezionato tramite `JAVA_HOME` e `PATH`.
- Docker Desktop avviato con container Linux, per database locale e test.
- Accesso alla rete per scaricare dipendenze e immagini al primo avvio.

## Avvio locale (PowerShell)

Dalla cartella `piattaforma-api`:

```powershell
# Percorso presente su questo laptop; adattarlo alle altre macchine.
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-25.0.2'
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
docker compose up -d --wait
./mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev'
```

Con il profilo dev attuale, API sulla porta 8081; `GET http://localhost:8081/actuator/health` restituisce
`{"status":"UP"}` quando database e SMTP sono raggiungibili. Nessun dettaglio interno
è esposto. Sono disponibili anche le API di autenticazione descritte sotto.
Le [API CRUD](docs/crud-api.md) gestiscono tutte le 23 entità e richiedono permessi espliciti.
La schermata di login React non è ancora implementata.

Il database è esposto solo su `127.0.0.1:5433`, con nome e utente `sectec` e password
locale `sectec-local`. Il profilo `dev` contiene questi default esclusivamente per
sviluppo. Per cambiarne la password, impostare `DB_PASSWORD` nel terminale prima di
avviare sia Compose sia Spring Boot. PostgreSQL applica le variabili iniziali solo
alla creazione del volume: cambiarle non aggiorna la password di un database esistente.

Senza profilo `dev`, impostare `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` nell'ambiente.
`SERVER_PORT` è opzionale (default 8080). Nessun profilo di sviluppo è attivo per default.

Per fermare l'applicazione usare Ctrl+C; `docker compose down` ferma il database
conservando i dati nel volume.

## Build e test

Con Java 25 e Docker attivi:

```powershell
./mvnw.cmd verify
```

I test avviano un PostgreSQL temporaneo con Testcontainers e porte dinamiche,
indipendente dal database di sviluppo. Verificano il contesto completo, lo stato
di salute, Flyway, il blocco delle rotte, la persistenza delle entità di sicurezza,
le associazioni e i vincoli PostgreSQL. Verificano anche login, rotazione della sessione,
CSRF, logout, account non attivi e autorizzazione sui metodi. Il container di test viene
rimosso automaticamente.

Il JAR eseguibile viene prodotto in `target/piattaforma-api-0.0.1-SNAPSHOT.jar`.

## Organizzazione

Package base: `it.sectec.platform`. `SecTecApplication` rimane alla radice per
consentire la scansione dei componenti. Il package `security` contiene la policy HTTP.

`identity.entity` e `identity.repository` contengono le entità di sicurezza e i
repository JPA. Le altre funzionalità verranno aggiunte nei package `customer`,
`catalog`, `cart`, `order`, `inventory`, `payment`, `fulfillment` e `support`.
Ogni funzionalità potrà contenere `controller`, `dto`, `service`, `repository`,
`entity` e `mapper`, quando necessari. Configurazioni trasversali in `config`;
gestione errori condivisa in `shared.error`.

I controller espongono DTO; i service gestiscono logica e transazioni; i repository
restano interni alla rispettiva funzionalità. L'applicazione è un unico modulo Maven.

## Persistenza

Sono mappate tutte le 23 entità del diagramma ER aggiornato. La [mappatura completa](docs/entity-mapping.md)
descrive package, associazioni e correzioni concordate. Le migrazioni V2 e V3 aggiungono
le tabelle di dominio alle cinque tabelle di sicurezza della V1.

Flyway cerca le migrazioni in `src/main/resources/db/migration`. La migrazione
`V1__create_identity_tables.sql` crea `app_user`, `role`, `permission`, `user_role`
e `role_permission`, seguendo il diagramma aggiornato `piattaforma-web/ER_DIAGRAM.drawio`
e le note `docs/diagram-review/schema-notes.md` (percorsi dalla radice del repository).
Hibernate usa `ddl-auto: validate` e `open-in-view: false` e non modifica lo schema.

Le cinque entità sono `User`, `Role`, `Permission`, `UserRole` e `RolePermission`.
Le associazioni hanno UUID propri, coppie univoche, riferimenti LAZY e nessuna
cancellazione a cascata. Gli UUID sono generati dall'applicazione tramite JPA.
Le FK impediscono cancellazioni e modifiche di chiavi ancora referenziate.

`UserRepository.findByEmail` confronta email senza distinzione di maiuscole e
ignorando gli spazi esterni, coerentemente con l'indice univoco del database.
I nomi di ruoli e permessi sono codici univoci case-sensitive; non viene imposto
un elenco fisso. `status` rimane una stringa obbligatoria non vuota; per il login
è consentito soltanto `ACTIVE`. `DISABLED`, `LOCKED` e valori sconosciuti negano l'accesso.

`hashed_password` deve ricevere esclusivamente un hash generato con il bean
`PasswordEncoder` (`encode`), che usa il formato Spring con prefisso, ad esempio
`{bcrypt}...`. Non inserire password in chiaro o hash privi del prefisso. Le entity non sono DTO
da esporre nelle API. Non vengono inseriti utenti o ruoli predefiniti. La migrazione V4
inserisce i 46 permessi CRUD senza assegnarli automaticamente.
Il caricamento degli utenti, dei ruoli e dei permessi per Spring Security avviene nel database.

## API di autenticazione

Sono disponibili anche [registrazione, verifica email e reset password](docs/account-lifecycle.md),
tramite API JSON e Mailpit per leggere le email di sviluppo. Le schermate appartengono
a `piattaforma-web`; i link email puntano al frontend configurato con `FRONTEND_BASE_URL`.

| Metodo | Endpoint | Risposta |
| --- | --- | --- |
| GET | `/api/v1/auth/csrf` | 200 con `headerName` e `token`; crea una sessione anonima se necessario |
| POST | `/api/v1/auth/login` | JSON con `email` e `password`; 200 con il profilo dopo autenticazione |
| GET | `/api/v1/auth/me` | 200 con `id`, `email`, `nome`, `cognome`, `roles`, `permissions` |
| POST | `/api/v1/auth/logout` | 204; invalida la sessione e cancella il cookie |

Login e logout richiedono il token CSRF nell'header restituito da `/csrf`
(`X-CSRF-TOKEN`). La risposta del login non include password o hash.
Il login cambia l'ID di sessione e invalida il token CSRF precedente: recuperare
un nuovo token dopo login e logout. GET non effettua logout.

Errori JSON: `401 INVALID_CREDENTIALS` per credenziali errate o account non attivo,
`401 UNAUTHENTICATED` per accesso anonimo a risorse protette,
`403 INVALID_CSRF_TOKEN` per token mancante/non valido,
`403 FORBIDDEN` per permessi insufficienti e `400 INVALID_REQUEST` per JSON non valido
o campi obbligatori mancanti. Il filtro CSRF precede l'autenticazione: una POST senza
token può rispondere 403 anche quando la sessione è scaduta.

La sessione risiede nella memoria del backend e scade dopo 30 minuti di inattività.
Riavviare il backend richiede un nuovo login. Il cookie `JSESSIONID` è HttpOnly,
SameSite=Lax e Secure; soltanto il profilo `dev` disabilita Secure per HTTP locale.
In produzione usare HTTPS e un reverse proxy che serva frontend e `/api` dalla stessa origine.
Il proxy Vite inoltra `/api` al backend sulla porta 8080 durante `npm run dev`;
non configura il container Nginx della demo statica. Non è abilitato CORS cross-origin.

Esempio da React (nessuna credenziale in localStorage):

```typescript
const csrf = await fetch('/api/v1/auth/csrf').then(r => r.json());
const response = await fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
  body: JSON.stringify({ email, password }),
});
if (!response.ok) throw new Error('Accesso non riuscito');
const profile = await response.json();
const authenticatedCsrf = await fetch('/api/v1/auth/csrf').then(r => r.json());
// Usare authenticatedCsrf per le successive richieste di modifica e logout.
```

Ruoli e permessi sono una fotografia al login: modifiche alle assegnazioni richiedono un
nuovo accesso. Reset password e modifiche amministrative alle credenziali revocano le
sessioni alla richiesta successiva; lo stato account è verificato a ogni richiesta.
La limitazione dei tentativi di login non è ancora inclusa; gli endpoint di registrazione
e recupero hanno invece un limite dedicato. Non vengono creati account predefiniti.

## Autorizzazione

I ruoli diventano authorities `ROLE_<nome>`, i permessi `PERM_<nome>`, in namespace distinti.
I controlli sui metodi sono abilitati con `@EnableMethodSecurity`.
Per un servizio protetto usare ad esempio `@PreAuthorize("hasAuthority('PERM_SUPPORT_READ')")`;
per un ruolo usare `@PreAuthorize("hasRole('ADMIN')")`.
Le nuove API devono essere abilitate esplicitamente nella policy HTTP e protette secondo
i permessi richiesti: le rotte non dichiarate sono negate. La visibilità dei pulsanti React
non sostituisce questi controlli sul backend.
