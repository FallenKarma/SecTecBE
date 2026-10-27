-- PostgreSQL: ensure the permission codes already enforced by the delivery APIs.
-- These permissions were originally seeded in V4. No grants are made here.
INSERT INTO permission (id, name, description) VALUES
    (gen_random_uuid(), 'BOLLA_READ', 'Lettura bolle'),
    (gen_random_uuid(), 'BOLLA_WRITE', 'Creazione, modifica ed eliminazione bolle'),
    (gen_random_uuid(), 'BOLLA_TIPO_ARTICOLO_READ', 'Lettura righe delle bolle'),
    (gen_random_uuid(), 'BOLLA_TIPO_ARTICOLO_WRITE', 'Creazione, modifica ed eliminazione righe delle bolle')
ON CONFLICT (name) DO NOTHING;
