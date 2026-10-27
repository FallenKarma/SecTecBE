-- PostgreSQL / Flyway. Preserve existing roles and custom grants.
-- No accounts or user_role assignments are created.
INSERT INTO role (id, name, description) VALUES
    (gen_random_uuid(), 'Admin', 'Amministratore: assegnazioni da configurare'),
    (gen_random_uuid(), 'Tecnico', 'Lettura e scrittura di clienti, impianti, interventi e assistenze'),
    (gen_random_uuid(), 'Cliente', 'Cliente: assegnazioni da configurare')
ON CONFLICT (name) DO NOTHING;

-- Only the explicitly agreed technician grants are seeded.
INSERT INTO role_permission (id, id_role, id_permission)
SELECT gen_random_uuid(), r.id, p.id
FROM role r CROSS JOIN permission p
WHERE r.name = 'Tecnico' AND p.name IN (
    'CLIENTE_READ', 'CLIENTE_WRITE',
    'IMPIANTO_READ', 'IMPIANTO_WRITE',
    'INTERVENTO_READ', 'INTERVENTO_WRITE',
    'ASSISTENZA_READ', 'ASSISTENZA_WRITE'
)
ON CONFLICT (id_role, id_permission) DO NOTHING;
