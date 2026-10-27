CREATE TABLE app_user (
    id uuid PRIMARY KEY,
    email varchar(254) NOT NULL CHECK (btrim(email) <> ''),
    hashed_password varchar(255) NOT NULL CHECK (btrim(hashed_password) <> ''),
    status varchar(32) NOT NULL CHECK (btrim(status) <> ''),
    email_verified_at timestamptz,
    nome varchar(100) NOT NULL CHECK (btrim(nome) <> ''),
    cognome varchar(100) NOT NULL CHECK (btrim(cognome) <> '')
);

CREATE UNIQUE INDEX uq_app_user_email ON app_user (lower(btrim(email)));

CREATE TABLE role (
    id uuid PRIMARY KEY,
    name varchar(64) NOT NULL UNIQUE CHECK (btrim(name) <> ''),
    description text
);

CREATE TABLE permission (
    id uuid PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE CHECK (btrim(name) <> ''),
    description text
);

CREATE TABLE user_role (
    id uuid PRIMARY KEY,
    id_user uuid NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    id_role uuid NOT NULL REFERENCES role(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    assigned_at timestamptz NOT NULL,
    CONSTRAINT uq_user_role UNIQUE (id_user, id_role)
);

CREATE INDEX ix_user_role_id_role ON user_role(id_role);

CREATE TABLE role_permission (
    id uuid PRIMARY KEY,
    id_role uuid NOT NULL REFERENCES role(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    id_permission uuid NOT NULL REFERENCES permission(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT uq_role_permission UNIQUE (id_role, id_permission)
);

CREATE INDEX ix_role_permission_id_permission ON role_permission(id_permission);
