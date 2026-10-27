# Migrazioni

Aggiungere le migrazioni SQL con nomi `V1__descrizione.sql`, `V2__descrizione.sql`, ecc.
Le migrazioni applicate non devono essere modificate: aggiungere una nuova versione.

`V1__create_identity_tables.sql` introduce le cinque tabelle di sicurezza del
diagramma ER: app_user, role, permission, user_role e role_permission.
Flyway gestisce lo schema; Hibernate verifica le entity con `ddl-auto: validate`
senza creare o aggiornare tabelle. Nessun account viene inserito automaticamente.

V2 aggiunge catalogo, file, fornitori e tipologie. V3 aggiunge clienti, impianti,
articoli, assistenze, interventi, bolle e relative associazioni.

V4 inserisce i codici dei permessi CRUD per le 23 risorse. Non assegna permessi a
ruoli o utenti e non crea account predefiniti.

V5 aggiunge i token interni per verifica email/reset password e la versione delle
credenziali per revocare le sessioni. I token sono conservati soltanto come hash.

V6 garantisce la presenza di BOLLA_READ/WRITE e BOLLA_TIPO_ARTICOLO_READ/WRITE,
gia previsti da V4, senza duplicazioni e senza assegnarli a ruoli.

V7 crea i ruoli `Admin`, `Tecnico` e `Cliente`. Assegna a `Tecnico` soltanto
CLIENTE_READ/WRITE, IMPIANTO_READ/WRITE, INTERVENTO_READ/WRITE e
ASSISTENZA_READ/WRITE. WRITE include creazione, modifica ed eliminazione.
Questi permessi hanno ambito globale, non limitato agli interventi assegnati.
V8 assegna ad Admin tutti i permessi presenti al momento della migrazione,
inclusi quelli di bolle e righe delle bolle. I permessi introdotti in futuro
richiederanno assegnazioni nelle relative migrazioni.
Cliente riceve soltanto CATEGORIA_READ, TIPO_ARTICOLO_READ,
IMMAGINE_ARTICOLO_READ, TIPO_INTERVENTO_READ, TIPO_IMPIANTO_READ e
TIPO_ASSISTENZA_READ. Nessun accesso globale ad anagrafiche, impianti,
interventi, assistenze o bolle viene aggiunto a Cliente.
I ruoli esistenti e le eventuali assegnazioni personalizzate sono conservati.
Nessun ruolo viene assegnato automaticamente agli utenti, nemmeno alla registrazione.
Le migrazioni vengono applicate da Flyway al successivo avvio del backend.
Gli utenti gia autenticati devono ripetere il login per caricare permessi modificati.
