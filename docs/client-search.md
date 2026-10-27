# Ricerca clienti

`GET /api/v1/clienti/search` restituisce lo stesso oggetto paginato di
`GET /api/v1/clienti`. Richiede una sessione autenticata e il permesso
`CLIENTE_READ` (authority `PERM_CLIENTE_READ`). Non richiede body.

| Parametro | Comportamento |
| --- | --- |
| `nome` | Corrispondenza parziale, massimo 100 caratteri |
| `cognome` | Corrispondenza parziale, massimo 100 caratteri |
| `codiceFiscale` | Corrispondenza esatta, massimo 16 caratteri |
| `page` | Indice da 0, default 0 |
| `size` | Da 1 a 100, default 20 |

I filtri sono opzionali, combinati in AND e ignorano maiuscole/minuscole e
spazi iniziali/finali. Valori vuoti o composti solo da spazi non applicano
filtri. Senza filtri si ottiene l'elenco completo paginato. Nome e cognome
sono parametri distinti: per cercare una persona passarli entrambi.
I caratteri `%`, `_` e `!` sono cercati letteralmente, non come wildcard.
Il codice fiscale nullo non corrisponde a un filtro valorizzato.

```http
GET /api/v1/clienti/search?nome=Mario&cognome=Rossi&page=0&size=20
GET /api/v1/clienti/search?codiceFiscale=RSSMRA80A01H501U
GET /api/v1/clienti/search?nome=Mario&cognome=Rossi&codiceFiscale=RSSMRA80A01H501U
```

La risposta 200 contiene `content`, `page`, `size`, `totalElements` e
`totalPages`; ogni elemento di `content` usa il DTO cliente esistente.
L'ordinamento e per `id`, come nell'elenco non filtrato. Se non ci sono
corrispondenze, `content` e vuoto e `totalElements` e 0. Una pagina oltre
l'ultima restituisce contenuto vuoto ma conserva il totale delle corrispondenze.

Errori: `400 INVALID_REQUEST` per filtri troppo lunghi (dopo trim) o parametri
numerici malformati; `400 INVALID_PAGINATION` per page/size fuori intervallo;
`401` senza autenticazione e `403` senza permesso di lettura.
I valori dei filtri non vengono scritti nei log applicativi.
L'endpoint CRUD `GET /api/v1/clienti` rimane invariato.
