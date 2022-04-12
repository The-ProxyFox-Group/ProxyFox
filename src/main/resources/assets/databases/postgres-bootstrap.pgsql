-- Bootstrap script for Postgres

CREATE TYPE autoproxymode AS ENUM ('off', 'latch', 'front', 'member', 'fallback');

CREATE TABLE IF NOT EXISTS systems (
    /** The ID of the system. Must be unique. A hint maybe provided. */
    id          SERIAL PRIMARY KEY NOT NULL UNIQUE,
    /** The name of the system. Maybe null */
    name        TEXT NULL,
    /** The description of the system. */
    description TEXT NULL,
    /** The global tag of the system. Maybe appended to the member's name. */
    tag         TEXT NULL,
    /** The avatar of the system. */
    avatarUrl   TEXT NULL,
    /** The timezone the system's in. */
    timezone    TEXT NULL,
    /** When the system was created. */
    created     TIMESTAMPTZ NULL,
    autoProxy   INT REFERENCES members(id) NULL,
    autoProxyMode autoproxymode NOT NULL DEFAULT 'off',
    autoProxyTimeout INT8 NULL
);

CREATE TABLE IF NOT EXISTS members (
    /** The ID of the member. This is *not* globally unique, but maybe incremented as if it was. */
    id              SERIAL NOT NULL NOT UNIQUE,
    /** The ID of the system the member belongs to. */
    systemId        INT REFERENCES systems(id) NOT NULL,
    /** The name of the member. Must not be null */
    name            TEXT NOT NULL,
    /** A nickname for the member. If present, will replace `name` when proxied. */
    displayName     TEXT NULL,
    /** A description of the member */
    description     TEXT NULL,
    /** The member's pronouns. */
    pronouns        TEXT NULL,
    /** The member's color. Only shown in info commands due to limitations. */
    color           INT  NOT NULL DEFAULT -1,
    /** The member's avatar URL. Will be used when proxied. */
    avatarUrl       TEXT NULL,
    /** Whether the proxy tags should be preserved. */
    keepProxyTags   BOOLEAN NOT NULL DEFAULT FALSE,
    /** How many messages the member has sent. */
    messageCount    INT8 NOT NULL DEFAULT 0,
    /** When the member was created. */
    created         TIMESTAMPZ NULL
);

CREATE TABLE IF NOT EXISTS systemServerPreferences (
    /** The ID of the server. */
    serverId            INT8 NOT NULL NOT UNIQUE,
    /** The ID of the system. */
    systemId            INT REFERENCES systems(id) NOT NULL,
    /** Whether the system wants proxying enabled in the server. */
    proxyEnabled        BOOLEAN DEFAULT TRUE
    autoProxy           INT REFERENCES members(id) NULL,
    autoProxyMode       autoproxymode NOT NULL DEFAULT 'fallback',
    autoProxyTimeout    INT8 NULL
);

CREATE TABLE IF NOT EXISTS memberServerPreferences (
    /** The ID of the server. */
    serverId    INT8 NOT NULL NOT UNIQUE,
    /** The ID of the system. */
    systemId    INT REFERENCES systems(id) NOT NULL,
    /** The ID of the member. */
    memberId    INT REFERENCES members(id) NOT NULL,
    /** Per-server avatar URL. If present, will override the global avatar. */
    avatarUrl   TEXT NULL,
    /** Nickname for within the server. If present, will override the set display name. */
    nickname    TEXT NULL,
    /** Whether the member's proxy is enabled in the server. */
    proxyEnabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS hosts(
    /** The ID of the Discord user. Must be unique. */
    discordId   INT8 PRIMARY KEY NOT NULL UNIQUE,
    /** The ID of the system. */
    systemId    INT REFERENCES systems(id) NOT NULL
);

CREATE TABLE IF NOT EXISTS switches(
    /** Implementation detail of the database; global ID is only for one to many. */
    globalId    SERIAL8 PRIMARY KEY NOT NULL UNIQUE,
    /** The per-system ID of the switch. */
    id          SERIAL NOT NULL NOT UNIQUE,
    /** The ID of the system. */
    systemId    INT REFERENCES systems(id) NOT NULL,
    /** The timestamp of the switch. */
    timestamp   TIMESTAMPTZ NOT NULL
);

/** This is a one-to-many relationship, you'll probably want to collate this into an array. */
CREATE TABLE IF NOT EXISTS switchMembers (
    /** The global ID for the switch. */
    globalId    INT8 REFERENCES switches(globalId) NOT NULL,
    /** One of the members fronting. */
    memberId    INT REFERENCES members(id) NOT NULL
);

CREATE TABLE IF NOT EXISTS subSystems (
    /** The global ID for the subsystem */
    globalId    SERIAL8 PRIMARY KEY NOT NULL UNIQUE,
    /** The per-system ID of the subsystem */
    id          SERIAL NOT NULL NOT UNIQUE,
    /** The ID of the system */
    systemId    INT REFERENCES systems(id) NOT NULL,
    /** The tag of the subsystem */
    tag         TEXT NULL
);

CREATE TABLE IF NOT EXISTS subSystemMembers (
    /** The global ID for the subsystem */
    globalId    INT8 REFERENCES subSystems(globalId) NOT NULL,
    /** The ID of the member apart of the subsystem */
    memberId    INT8 REFERENCES members(id) NOT NULL
);

CREATE TABLE IF NOT EXISTS groups (
    /** The global ID for the group */
    globalId    SERIAL8 PRIMARY KEY NOT NULL UNIQUE,
    /** The per-system ID of the group */
    id          SERIAL NOT NULL NOT UNIQUE,
    /** The ID of the system */
    systemId    INT REFERENCES systems(id) NOT NULL
);

CREATE TABLE IF NOT EXISTS groupMembers (
    /** The global ID for the group */
    globalId    INT8 REFERENCES groups(globalId) NOT NULL,
    /** The ID of the member apart of the group */
    memberId    INT8 REFERENCES members(id) NOT NULL
);

-- Creates 4 indexes to intertwine stuff into a hopefully quick lookup.
CREATE INDEX IF NOT EXISTS members_index ON members (id, systemId, name);
CREATE INDEX IF NOT EXISTS systemServerPreferences_index ON systemServerPreferences (serverId, systemId);
CREATE INDEX IF NOT EXISTS memberServerPreferences_index ON memberServerPreferences (serverId, systemId, memberId);
CREATE INDEX IF NOT EXISTS hosts_index ON hosts (systemId);
CREATE INDEX IF NOT EXISTS switches_index ON switches (id, systemId);