ALTER TABLE organization DROP FOREIGN KEY FK9jug41w1xluibkpq2a5m4pc8v;
ALTER TABLE practitioner DROP FOREIGN KEY FKseu8ofhvw5hvnf4wh6nubvnk9;


ALTER TABLE organization ADD consent_attestation_from_id bigint, ADD consent_attestation_to_id bigint, DROP COLUMN consent_attestation_id;
ALTER TABLE organization_aud ADD consent_attestation_from_id bigint, ADD consent_attestation_to_id bigint, DROP COLUMN consent_attestation_id;

ALTER TABLE practitioner ADD consent_attestation_from_id bigint, ADD consent_attestation_to_id bigint, DROP COLUMN consent_attestation_id;
ALTER TABLE practitioner_aud ADD consent_attestation_from_id bigint, ADD consent_attestation_to_id bigint, DROP COLUMN consent_attestation_id;

ALTER TABLE organization ADD CONSTRAINT FK35k7d5ia1kby6dhw9mr7l7cql FOREIGN KEY (consent_attestation_to_id) REFERENCES consent_attestation (id);
ALTER TABLE organization ADD CONSTRAINT FKigguob0rewgro7959ode7b9rm FOREIGN KEY (consent_attestation_from_id) REFERENCES consent_attestation (id);

ALTER TABLE practitioner ADD CONSTRAINT FKsktn0qh9ws339ij68sxj06i2y FOREIGN KEY (consent_attestation_to_id) REFERENCES consent_attestation (id);
ALTER TABLE practitioner ADD CONSTRAINT FKnah53x9ynkmnuttouiitxhq05 FOREIGN KEY (consent_attestation_from_id) REFERENCES consent_attestation (id);