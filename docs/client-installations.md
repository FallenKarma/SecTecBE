# Impianti associati a un cliente

`GET /api/v1/clienti/{clienteId}/impianti`

`clienteId` e l'UUID del cliente. Non richiede body o parametri di paginazione.
Restituisce tutti gli impianti collegati tramite `cliente_impianto`, ordinati per
ID crescente, nel formato `ImpiantoDto.Response` gia usato da `/api/v1/impianti`.
Un impianto condiviso viene incluso per ciascuno dei clienti a cui e associato.
La ricerca e eseguita nel database, senza caricare gli impianti di altri clienti.

Richiede una sessione autenticata e entrambe le authority `PERM_CLIENTE_READ`
e `PERM_IMPIANTO_READ`, presenti nei ruoli standard Admin e Tecnico.
Non concede al ruolo Cliente accesso ai dati degli altri clienti.

- `200`: array di impianti, oppure `[]` se il cliente non ha impianti associati.
- `400 INVALID_REQUEST`: UUID non valido.
- `404 NOT_FOUND`: cliente inesistente.
- `401`: sessione non autenticata.
- `403`: permessi insufficienti.

La risposta contiene i campi dell'impianto, incluso `tipoImpiantoId`, senza
entita annidate. Nessuna modifica al database o migrazione Flyway e necessaria.
