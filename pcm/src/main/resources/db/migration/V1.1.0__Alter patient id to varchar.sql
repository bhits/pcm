LOCK TABLES
patient WRITE,
patient_providers WRITE,
patient_aud WRITE,
consent WRITE,
consent_aud WRITE;

ALTER TABLE patient_providers
  DROP FOREIGN KEY FK9yk8mkbajtynqjk49xe1abqm2,
  MODIFY patient_id varchar(255) NOT NULL;

ALTER TABLE consent
  DROP FOREIGN KEY FK975ajb9rne9852qoi9c2rq2pg,
  MODIFY patient_id varchar(255);

ALTER TABLE consent_aud MODIFY patient_id varchar(255);

ALTER TABLE patient MODIFY id varchar(255) NOT NULL;

ALTER TABLE patient_aud MODIFY id varchar(255);

ALTER TABLE patient_providers
  ADD CONSTRAINT FK9yk8mkbajtynqjk49xe1abqm2
  FOREIGN KEY (patient_id)
  REFERENCES patient (id);

ALTER TABLE consent
  ADD CONSTRAINT  FK975ajb9rne9852qoi9c2rq2pg
  FOREIGN KEY (patient_id)
  REFERENCES patient (id);

UNLOCK TABLES;