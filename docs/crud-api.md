# API CRUD

Tutte le API richiedono una sessione autenticata. Sono API di gestione, con accesso
all'intera risorsa per chi possiede il relativo permesso, non API self-service del cliente.
I permessi si applicano sui servizi tramite Spring Method Security.

## Operazioni comuni

| Metodo | Percorso | Risultato |
| --- | --- | --- |
| GET | /api/v1/{risorsa}?page=0&size=20 | 200, elenco paginato |
| GET | /api/v1/{risorsa}/{id} | 200, dettaglio |
| POST | /api/v1/{risorsa} | 201, DTO creato e header Location |
| PUT | /api/v1/{risorsa}/{id} | 200, DTO aggiornato |
| DELETE | /api/v1/{risorsa}/{id} | 204, nessun corpo |

Gli ID sono UUID generati dal server. La pagina parte da zero; size va da 1 a 100.
L'ordinamento è stabile per ID. La risposta dell'elenco contiene content, page, size,
totalElements e totalPages. PUT sostituisce i valori dei campi: i campi obbligatori
devono essere inviati; quelli facoltativi omessi diventano null. PATCH non è esposto.

POST, PUT e DELETE richiedono l'header CSRF ottenuto da /api/v1/auth/csrf.
Le relazioni sono indicate con UUID nei campi *Id, non con oggetti annidati.
Le risposte non espongono entity JPA o collezioni ricorsive.

## Risorse e permessi

I codici nella tabella sono salvati in permission.name. Spring li espone come authorities
con prefisso PERM_. READ abilita GET; WRITE abilita POST, PUT e DELETE.
WRITE non implica automaticamente READ.

| Risorsa | Entity | Permesso lettura | Permesso scrittura |
| --- | --- | --- | --- |
| /api/v1/users | User | USER_READ | USER_WRITE |
| /api/v1/roles | Role | ROLE_READ | ROLE_WRITE |
| /api/v1/permissions | Permission | PERMISSION_READ | PERMISSION_WRITE |
| /api/v1/user-roles | UserRole | USER_ROLE_READ | USER_ROLE_WRITE |
| /api/v1/role-permissions | RolePermission | ROLE_PERMISSION_READ | ROLE_PERMISSION_WRITE |
| /api/v1/categorie | Categoria | CATEGORIA_READ | CATEGORIA_WRITE |
| /api/v1/tipi-articolo | TipoArticolo | TIPO_ARTICOLO_READ | TIPO_ARTICOLO_WRITE |
| /api/v1/file-assets | FileAsset | FILE_ASSET_READ | FILE_ASSET_WRITE |
| /api/v1/immagini-articolo | ImmagineArticolo | IMMAGINE_ARTICOLO_READ | IMMAGINE_ARTICOLO_WRITE |
| /api/v1/documenti-articolo | DocumentoArticolo | DOCUMENTO_ARTICOLO_READ | DOCUMENTO_ARTICOLO_WRITE |
| /api/v1/tipi-intervento | TipoIntervento | TIPO_INTERVENTO_READ | TIPO_INTERVENTO_WRITE |
| /api/v1/tipi-impianto | TipoImpianto | TIPO_IMPIANTO_READ | TIPO_IMPIANTO_WRITE |
| /api/v1/tipi-assistenza | TipoAssistenza | TIPO_ASSISTENZA_READ | TIPO_ASSISTENZA_WRITE |
| /api/v1/fornitori | Fornitore | FORNITORE_READ | FORNITORE_WRITE |
| /api/v1/clienti | Cliente | CLIENTE_READ | CLIENTE_WRITE |
| /api/v1/impianti | Impianto | IMPIANTO_READ | IMPIANTO_WRITE |
| /api/v1/clienti-impianti | ClienteImpianto | CLIENTE_IMPIANTO_READ | CLIENTE_IMPIANTO_WRITE |
| /api/v1/articoli | Articolo | ARTICOLO_READ | ARTICOLO_WRITE |
| /api/v1/assistenze | Assistenza | ASSISTENZA_READ | ASSISTENZA_WRITE |
| /api/v1/interventi | Intervento | INTERVENTO_READ | INTERVENTO_WRITE |
| /api/v1/user-interventi | UserIntervento | USER_INTERVENTO_READ | USER_INTERVENTO_WRITE |
| /api/v1/bolle | Bolla | BOLLA_READ | BOLLA_WRITE |
| /api/v1/bolle-tipi-articolo | BollaTipoArticolo | BOLLA_TIPO_ARTICOLO_READ | BOLLA_TIPO_ARTICOLO_WRITE |

V4 inserisce i 46 codici, senza assegnazioni automatiche o account predefiniti.
Per abilitare un operatore, collegare i permessi al suo ruolo tramite role_permission
e il ruolo all'utente tramite user_role. L'accesso iniziale deve essere predisposto
da un amministratore nel database; non esiste un endpoint pubblico di bootstrap.
Una volta configurati i permessi di gestione, le associazioni sono gestibili tramite API.

I permessi USER_WRITE, ROLE_WRITE, PERMISSION_WRITE, USER_ROLE_WRITE e
ROLE_PERMISSION_WRITE sono privilegi amministrativi: consentono di modificare account
o autorizzazioni e devono essere assegnati soltanto ad amministratori fidati.
I permessi della sessione restano quelli del login: modifiche alle assegnazioni richiedono
un nuovo accesso. Stato e versione credenziali sono verificati a ogni richiesta: reset
password e modifiche amministrative a password, email o stato revocano le sessioni.

## Esempi

POST /api/v1/categorie:

```json
{"nome":"Sensori","descrizione":"Sensori di apertura"}
```

POST /api/v1/tipi-articolo:

```json
{
  "categoriaId":"00000000-0000-4000-8000-000000000001",
  "codiceArticolo":"SENS-01",
  "descrizione":"Sensore di apertura",
  "marca":"Marca",
  "modello":"Modello",
  "costo":19.95
}
```

POST /api/v1/clienti-impianti:

```json
{
  "clienteId":"00000000-0000-4000-8000-000000000002",
  "impiantoId":"00000000-0000-4000-8000-000000000003"
}
```

Gli UUID degli esempi sono illustrativi e vanno sostituiti con record esistenti.
I campi completi sono definiti nei record Request e Response di ciascun package dto.

## Utenti e password

User Request contiene email, password, status, emailVerifiedAt, nome e cognome.
POST richiede password; PUT con password null o omessa conserva l'hash esistente.
Una password fornita deve contenere almeno 12 caratteri e al massimo 72 byte UTF-8
(limite BCrypt). Il server genera l'hash tramite PasswordEncoder; non accetta hash
come credenziale da salvare direttamente. Le risposte non contengono password o hash.
status accetta ACTIVE, DISABLED e LOCKED. Queste API sono amministrative, non registrazione pubblica.

## Validazioni ed errori

- 400 INVALID_REQUEST: campi mancanti/non validi, JSON errato o UUID non valido.
- 400 INVALID_PAGINATION: page negativa o size fuori intervallo.
- 400 INVALID_REFERENCE: un ID associato non esiste.
- 400 INVALID_PASSWORD: password mancante in creazione o fuori dai limiti.
- 400 INVALID_DATE_RANGE: assistenza con dataFine non successiva a dataInizio.
- 400 INCONSISTENT_ASSISTANCE: intervento associato a una copertura di un altro impianto.
- 401 UNAUTHENTICATED: sessione non autenticata.
- 403 FORBIDDEN: permesso insufficiente.
- 403 INVALID_CSRF_TOKEN: token CSRF mancante o non valido.
- 404 NOT_FOUND: risorsa richiesta inesistente.
- 409 DATA_CONFLICT: violazione di un vincolo database, ad esempio duplicato o cancellazione
  di un record ancora referenziato.

Le cancellazioni non si propagano alle relazioni: occorre gestire esplicitamente i record
dipendenti. Gli errori non riportano SQL, hash o contenuti delle credenziali.
I controlli di coerenza dell'assistenza sono applicati quando si crea o aggiorna un intervento;
spostamenti successivi degli articoli e regole sullo storico richiedono un flusso applicativo dedicato.
Le API file-assets gestiscono soltanto metadati; non effettuano upload o cancellazioni di file fisici.
Non sono introdotte regole automatiche di giacenza, pagamenti o fatturazione.

## Verifica

CrudApiTests esercita POST, GET elenco/dettaglio, PUT e DELETE per tutte le 23 risorse,
includendo associazioni reali in PostgreSQL. Controlla inoltre permessi, CSRF,
password, validazione, paginazione, riferimenti inesistenti e conflitti.

