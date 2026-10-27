# Log applicativi

I log sono emessi sulla console. Il livello del package `it.sectec.platform` e
`INFO` per impostazione predefinita e `DEBUG` con il profilo `dev`.
La variabile `APP_LOG_LEVEL` consente di sovrascriverlo, ad esempio:

```powershell
$env:APP_LOG_LEVEL = 'DEBUG'
./mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev'
```

Se il checkout non contiene `.mvn/wrapper/maven-wrapper.properties`, usare
`mvn` al posto di `./mvnw.cmd`, con Maven installato e `JAVA_HOME` sul JDK 25.

Riavviare il backend dopo la modifica del codice o della configurazione.
Per scrivere anche su file si puo impostare `LOGGING_FILE_NAME`, ad esempio
`logs/sectec.log` (configurazione standard Spring Boot).

## Livelli e correlazione

- `DEBUG`: esito e durata HTTP, letture CRUD, inizio operazioni, cause delle
  validazioni, account assente/non attivo, richieste ignorate, cooldown email,
  token non valido e operazioni annullate dalla transazione.
- `INFO`: login/logout, revoca sessione, scritture CRUD e cambiamenti account
  dopo commit, accettazione dell'email da parte del server SMTP.
- `WARN`: login rifiutato, rate limit, conflitti di integrita e risposte HTTP 4xx.
- `ERROR`: invio SMTP fallito, eccezioni non gestite e risposte HTTP 5xx.

Ogni richiesta riceve un UUID generato dal server: compare come `requestId`
nei log e nell'header di risposta `X-Request-ID`. Non viene usato un ID fornito
dal client. Lo stesso contesto accompagna l'invio asincrono delle email.
Per trovare una sequenza, cercare nei log il valore restituito nell'header.
Le richieste bloccate prima del controller hanno route `<unmapped-or-filtered>`;
quelle gestite da MVC riportano il template della route, senza valori dei parametri.

## Diagnosi del login e delle email

Un login prima della verifica produce a livello DEBUG
`Login account is not active: pendingVerification=true locked=false`, seguito
da `Login rejected: reason=DisabledException`. La risposta pubblica rimane
`401 INVALID_CREDENTIALS`.

`Registration committed: status=PENDING_VERIFICATION` indica che serve la
verifica email. Una registrazione duplicata non cambia le credenziali e non
invia email. Il reinvio puo essere ignorato entro il cooldown di 60 secondi.
`Account email accepted by SMTP server` non garantisce la consegna finale:
in sviluppo il messaggio va cercato in Mailpit. In caso di errore SMTP,
controllare il servizio/configurazione e richiedere un nuovo link.

## Dati esclusi

I nuovi log non registrano body, email, nomi, password, hash, token, cookie,
session ID, indirizzi IP, URL grezzi o query string. Per le eccezioni inattese
sono registrati il tipo e i frame applicativi, senza messaggi e cause annidate
che potrebbero contenere dati SQL o credenziali. Le validazioni riportano il
codice applicativo o il tipo di errore senza il valore rifiutato.
Questa scelta riguarda i log aggiunti qui: non abilitare indiscriminatamente
TRACE di Spring Security, binding SQL o debug SMTP, che possono esporre dati.
