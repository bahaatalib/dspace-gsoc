ALTER TABLE resourcepolicy ADD uuid VARCHAR(36);

ALTER TABLE resourcepolicy ADD resource_uuid VARCHAR(36);

ALTER TABLE eperson ADD uuid VARCHAR(36);

ALTER TABLE epersongroup ADD uuid VARCHAR(36);

ALTER TABLE item ADD uuid VARCHAR(36);

ALTER TABLE bitstream ADD uuid VARCHAR(36);

ALTER TABLE bundle ADD uuid VARCHAR(36);

ALTER TABLE collection ADD uuid VARCHAR(36);

ALTER TABLE community ADD uuid VARCHAR(36);

CREATE TABLE uuid (
    uuid VARCHAR(36) not null,
    resource_type integer,
    resource_id integer
);

CREATE INDEX uuid_idx ON uuid (uuid);

CREATE TABLE externalidentifier (
    namespace varchar(20),
    identifier text,
    resource_type_id integer,
    resource_id integer,
    tombstone integer
);

CREATE INDEX externalidentifier_idx ON externalidentifier(namespace, identifier);

-- We also need to do the following at some point during the upgrade.  Before the upgrade has
-- finished but after the update script has been run
--
-- DROP TABLE handle;
-- DROP INDEX handle_handle_idx;
-- DROP INDEX handle_resource_id_and_type_idx;