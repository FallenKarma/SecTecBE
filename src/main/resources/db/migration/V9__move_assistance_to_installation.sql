-- Preserve existing assistance IDs and intervention links. Do not invent an
-- installation for assistance covering an article that is still in stock.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM assistenza a JOIN articolo ar ON ar.id = a.id_articolo
        WHERE ar.id_impianto IS NULL
    ) THEN
        RAISE EXCEPTION 'Cannot migrate assistance: assign an installation to every covered article before retrying V9';
    END IF;
END $$;

ALTER TABLE assistenza ADD COLUMN id_impianto uuid;
UPDATE assistenza a SET id_impianto = ar.id_impianto
FROM articolo ar WHERE ar.id = a.id_articolo;
ALTER TABLE assistenza ALTER COLUMN id_impianto SET NOT NULL;
ALTER TABLE assistenza ADD CONSTRAINT assistenza_id_impianto_fkey
    FOREIGN KEY (id_impianto) REFERENCES impianto(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
CREATE INDEX ix_assistenza_id_impianto ON assistenza(id_impianto);
ALTER TABLE assistenza DROP COLUMN id_articolo;
