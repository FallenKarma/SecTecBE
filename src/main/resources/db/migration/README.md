# Migrazioni

Aggiungere le migrazioni SQL con nomi `V1__descrizione.sql`, `V2__descrizione.sql`, ecc.
Le migrazioni applicate non devono essere modificate: aggiungere una nuova versione.

`V1__create_identity_tables.sql` introduce le cinque tabelle di sicurezza del
diagramma ER: app_user, role, permission, user_role e role_permission.
Flyway gestisce lo schema; Hibernate verifica le entity con `ddl-auto: validate`
senza creare o aggiornare tabelle. Nessun account o permesso è inserito automaticamente.

V2 aggiunge catalogo, file, fornitori e tipologie. V3 aggiunge clienti, impianti,
articoli, assistenze, interventi, bolle e relative associazioni.

V4 inserisce i codici dei permessi CRUD per le 23 risorse. Non assegna permessi a
ruoli o utenti e non crea account predefiniti.

V5 aggiunge i token interni per verifica email/reset password e la versione delle
credenziali per revocare le sessioni. I token sono conservati soltanto come hash.
