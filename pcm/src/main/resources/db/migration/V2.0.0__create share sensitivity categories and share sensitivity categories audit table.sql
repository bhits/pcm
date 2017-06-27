
create table consent_type_configuration (id bigint not null auto_increment, share_consent_type_configured bit not null, primary key (id)) ENGINE=InnoDB;
create table consent_type_configuration_aud (id bigint not null, rev integer not null, revtype tinyint, share_consent_type_configured bit, primary key (id, rev)) ENGINE=InnoDB;

ALTER TABLE consent ADD consent_type_configuration_id bigint;
ALTER TABLE consent_aud ADD consent_type_configuration_id bigint;


alter table consent add constraint FKp4i88yloelurx8mi5vrwhh18p foreign key (consent_type_configuration_id) references consent_type_configuration (id);
alter table consent_type_configuration_aud add constraint FK8qs3kma2wwkp8sjub3gyuep57 foreign key (rev) references revinfo (rev);