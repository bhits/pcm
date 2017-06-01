ALTER TABLE consent ADD created_by VARCHAR(255), ADD created_date datetime,  ADD last_updated_by VARCHAR(255),  ADD last_updated_date datetime ;
ALTER TABLE consent_aud ADD created_by VARCHAR(255), ADD created_date datetime,  ADD last_updated_by VARCHAR(255),  ADD last_updated_date datetime ;

ALTER TABLE consent_attestation ADD attested_date datetime, ADD attested_by  VARCHAR(255), ADD attested_by_patient boolean;
ALTER TABLE consent_attestation_aud ADD attested_date datetime, ADD attested_by  VARCHAR(255), ADD attested_by_patient boolean;