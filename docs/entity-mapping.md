# Mappatura delle entità

Il riferimento è `piattaforma-web/ER_DIAGRAM.drawio`, con le correzioni concordate il 25 ottobre 2026.
Sono presenti 23 entity JPA: le cinque di sicurezza e le diciotto elencate sotto.

| Entity | Package sotto `it.sectec.platform` |
| --- | --- |
| Categoria | catalog.entity |
| TipoArticolo | catalog.entity |
| FileAsset | storage.entity |
| ImmagineArticolo | catalog.entity |
| DocumentoArticolo | catalog.entity |
| TipoIntervento | support.entity |
| TipoImpianto | installation.entity |
| TipoAssistenza | support.entity |
| Fornitore | inventory.entity |
| Cliente | customer.entity |
| Impianto | installation.entity |
| ClienteImpianto | installation.entity |
| Articolo | inventory.entity |
| Assistenza | support.entity |
| Intervento | support.entity |
| UserIntervento | support.entity |
| Bolla | inventory.entity |
| BollaTipoArticolo | inventory.entity |

Le associazioni sono mappate sul lato che possiede la FK, con caricamento LAZY.
Le tabelle associative hanno una propria entity e un UUID; non sono ManyToMany implicite.
Non sono presenti cascade REMOVE o orphanRemoval. Gli UUID sono generati da JPA.
Le entity espongono getter e setter dei campi modificabili; l'ID non è modificabile.

Correzioni concordate:
- USER_INTERVENTO contiene id_intervento e id_user; sostituisce INTERVENTO.id_user.
- CLIENTE_IMPIANTO sostituisce IMPIANTO.id_cliente.
- Le coppie delle due associazioni sono univoche.
- IMPIANTO, INTERVENTO e ASSISTENZA hanno ciascuno una FK obbligatoria alla propria tipologia.
- CLIENTE.iban e BOLLA.id_fornitore ammettono NULL.
- CLIENTE.user_id è una relazione OneToOne facoltativa e univoca verso USER.

V1 mantiene le tabelle di sicurezza. V2 aggiunge catalogo, file, fornitori e tipologie.
V3 aggiunge clienti, impianti, articoli, assistenze, interventi, bolle e associazioni.
Le migrazioni si applicano all'avvio; Hibernate verifica lo schema con ddl-auto: validate.

BigDecimal rappresenta importi e coordinate, LocalDate le date, Instant i timestamptz.
Gli stati non ancora definiti restano stringhe. Per direzione della bolla, backend storage
e visibilità sono applicati i valori già elencati nel diagramma.
I vincoli quantitativi, le coppie univoche e le FK sono applicati anche da PostgreSQL.

L'unicità (tipo articolo, matricola) resta una proposta nel diagramma e non viene imposta.
La coerenza fra impianto dell'intervento e impianto dell'articolo coperto dall'assistenza
deve essere verificata nel futuro servizio applicativo; non è garantita dalle sole FK.
Le [API CRUD](crud-api.md) espongono DTO dedicati e associazioni per ID, con permessi
di lettura e scrittura per risorsa. Non sono introdotte ulteriori regole commerciali.

I test verificano tutte le 23 entity, il salvataggio e la rilettura del grafo delle relazioni,
la precisione numerica, le relazioni facoltative, i collegamenti multipli, l'unicità
cliente-impianto, la direzione delle bolle e il divieto di cancellare categorie referenziate.

