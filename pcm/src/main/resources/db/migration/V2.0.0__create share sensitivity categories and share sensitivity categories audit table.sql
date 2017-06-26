
create table share_sensitivity_categories (id bigint not null auto_increment, share_sensitivity_categories_enabled bit not null, primary key (id)) ENGINE=InnoDB;
create table share_sensitivity_categories_aud (id bigint not null, rev integer not null, revtype tinyint, share_sensitivity_categories_enabled bit, primary key (id, rev)) ENGINE=InnoDB;

ALTER TABLE consent ADD share_sensitivity_categories_id bigint;
ALTER TABLE consent_aud ADD share_sensitivity_categories_id bigint;


alter table consent add constraint FKp4i88yloelurx8mi5vrwhh18p foreign key (share_sensitivity_categories_id) references share_sensitivity_categories (id);
alter table share_sensitivity_categories_aud add constraint FK8qs3kma2wwkp8sjub3gyuep57 foreign key (rev) references revinfo (rev);