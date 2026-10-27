-- Grants confirmed for the roles created in V7. No user assignments.
-- Admin receives all permissions currently present, including delivery permissions.
-- Future permissions must be granted in their own migrations.
INSERT INTO role_permission (id, id_role, id_permission)
SELECT gen_random_uuid(), r.id, p.id
FROM role r CROSS JOIN permission p
WHERE r.name = 'Admin'
ON CONFLICT (id_role, id_permission) DO NOTHING;

-- Customer access is limited to catalog and type lookups.
INSERT INTO role_permission (id, id_role, id_permission)
SELECT gen_random_uuid(), r.id, p.id
FROM role r CROSS JOIN permission p
WHERE r.name = 'Cliente' AND p.name IN (
    'CATEGORIA_READ', 'TIPO_ARTICOLO_READ', 'IMMAGINE_ARTICOLO_READ',
    'TIPO_INTERVENTO_READ', 'TIPO_IMPIANTO_READ', 'TIPO_ASSISTENZA_READ'
)
ON CONFLICT (id_role, id_permission) DO NOTHING;

-- Replace only the placeholder descriptions introduced by V7.
UPDATE role SET description = 'Amministrazione completa della piattaforma'
WHERE name = 'Admin' AND description = 'Amministratore: assegnazioni da configurare';
UPDATE role SET description = 'Consultazione del catalogo e delle tipologie'
WHERE name = 'Cliente' AND description = 'Cliente: assegnazioni da configurare';
