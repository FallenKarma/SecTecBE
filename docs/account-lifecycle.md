# Registrazione, verifica email e recupero password

## Flusso e API

Tutti gli endpoint sono POST sotto `/api/v1/auth` e richiedono CSRF, anche senza login.
Prima ottenere token e cookie con `GET /api/v1/auth/csrf`.

| Endpoint | JSON | Risposta |
| --- | --- | --- |
| `/register` | `email`, `password`, `nome`, `cognome` | 202 `REQUEST_ACCEPTED` |
| `/resend-verification` | `email` | 202 `REQUEST_ACCEPTED` |
| `/verify-email` | `token` | 204 |
| `/forgot-password` | `email` | 202 `REQUEST_ACCEPTED` |
| `/reset-password` | `token`, `password` | 204 |

La registrazione pubblica crea un utente `PENDING_VERIFICATION`, senza ruoli, permessi CRUD
o anagrafica Cliente. Verificare l'email imposta `email_verified_at` e attiva l'utente.
Il frontend non può scegliere stato o ruoli. Un account esistente non viene sovrascritto
da una nuova registrazione: usare reinvio verifica o recupero password.

Registrazione duplicata e richieste per indirizzi sconosciuti restituiscono lo stesso
codice generico. Non sono restituiti ID, token o informazioni sull'esistenza dell'account.
Il reset è disponibile soltanto per account ACTIVE con email verificata; non sblocca
account DISABLED o LOCKED. Il CRUD amministrativo resta un canale fidato per il provisioning:
gli account ACTIVE già esistenti mantengono la compatibilità con il login precedente.
Per verificarne l'email si può usare `/resend-verification`.

Le password devono avere almeno 12 caratteri e al massimo 72 byte UTF-8. Hash BCrypt
tramite PasswordEncoder, mai password in chiaro nel database. Dopo il reset bisogna
effettuare nuovamente il login: non viene creata automaticamente una sessione.

## Token e sessioni

V5 aggiunge `account_token` e `app_user.credentials_version`. I token sono 32 byte
casuali codificati Base64 URL-safe; nel database si conserva esclusivamente SHA-256.
Scadenza predefinita: 24 ore per verifica email, 30 minuti per reset password.
Ogni token è legato a utente, indirizzo, versione delle credenziali e scopo.
I token sono utilizzabili una volta sola. Le operazioni bloccano la riga utente e
consumano il token nella stessa transazione dell'aggiornamento: anche due richieste
simultanee non possono completare due reset con lo stesso link.

Reinviare un link invalida quelli precedenti dello stesso tipo. Entro 60 secondi
dall'emissione, ulteriori richieste restituiscono comunque 202 senza inviare altre email.
Il reset invalida tutti i token residui e incrementa la versione delle credenziali.
Le sessioni precedenti vengono respinte con `401 SESSION_REVOKED` alla successiva
richiesta. Anche modifica amministrativa di password, email o stato revoca le sessioni.
La cancellazione/disabilitazione dell'utente viene verificata a ogni richiesta autenticata.
I permessi rimangono una fotografia al login: la loro modifica richiede ancora un nuovo login.

Gli errori per link scaduti, errati, consumati o di scopo diverso sono tutti
`400 INVALID_TOKEN`. Altri errori: `400 INVALID_REQUEST`, `400 INVALID_PASSWORD`,
`403 INVALID_CSRF_TOKEN`, `429 TOO_MANY_REQUESTS`.
Il limite è 10 richieste di ciclo account al minuto per indirizzo IP, condiviso fra
i cinque endpoint. Il contatore è in memoria, limitato a 10.000 indirizzi e valido
per una singola istanza. Non si interpretano header X-Forwarded-For non fidati;
in produzione configurare proxy fidati e un limite condiviso al gateway se necessario.

## Prova locale

Con il profilo dev attuale il backend usa la porta 8081:

```powershell
docker compose up -d --wait
./mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev'
```

- API backend: `http://localhost:8081/api/v1/auth`
- Frontend: `http://localhost:5173` (progetto `piattaforma-web`)
- Casella Mailpit: `http://localhost:8025`

Mailpit cattura le email sul laptop; non le recapita a destinatari esterni. Le porte
1025 e 8025 sono esposte solo su loopback. Il servizio usa l'immagine `axllent/mailpit:v1`.
Il backend espone esclusivamente API JSON e non serve pagine HTML o script per gli account.
Le schermate di registrazione, verifica e recupero appartengono al frontend React e sono
da integrare in `piattaforma-web`. Il contratto dei link email usa la navigazione hash:

- `FRONTEND_BASE_URL/#verify-email?token=...`
- `FRONTEND_BASE_URL/#reset-password?token=...`

Il frontend deve leggere il token dal fragment, conservarlo solo in memoria e rimuoverlo
dalla barra degli indirizzi; quindi ottenere CSRF e inviare la POST all'API corrispondente
solo su conferma dell'utente. Aprire il link non modifica lo stato sul backend.
Password e token non devono essere salvati in localStorage. Le chiamate `/api` passano
attraverso il proxy del frontend, conservando cookie di sessione e protezione CSRF.

## Configurazione SMTP

| Variabile | Default locale | Significato |
| --- | --- | --- |
| `MAIL_HOST` | localhost | Server SMTP |
| `MAIL_PORT` | 1025 | Porta SMTP |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | vuoti | Credenziali SMTP |
| `MAIL_AUTH` | false | Abilita autenticazione SMTP |
| `MAIL_STARTTLS` | false | Abilita e richiede STARTTLS |
| `MAIL_FROM` | noreply@sectec.invalid | Mittente autorizzato dal provider |
| `FRONTEND_BASE_URL` | http://localhost:5173 | URL pubblico del frontend per i link |

Per invio reale configurare SMTP, mittente e URL pubblico HTTPS. I link non vengono
costruiti a partire dall'header Host ricevuto dal client. I timeout SMTP sono 5 secondi.
Le durate sono configurabili con `app.account.verification-ttl` e `app.account.reset-ttl`.

L'invio avviene in un executor separato, solo dopo commit della transazione. Errori SMTP
vengono registrati senza indirizzi, contenuto del messaggio o token. La risposta 202
indica accettazione della richiesta, non consegna dell'email. Non è ancora presente
un outbox persistente: se il processo si arresta prima dell'invio o SMTP non è disponibile,
ripristinare il servizio e richiedere un nuovo link dopo il cooldown.

## Test

AccountLifecycleTests usa PostgreSQL e un mail sender simulato: verifica registrazione,
blocco del login prima della verifica, invio dopo commit, hash dei token, scadenza,
reinvio, scopi distinti, reset concorrenti, mancata enumerazione nelle risposte,
CSRF, limiti di frequenza, account disabilitati e revoca delle sessioni.
