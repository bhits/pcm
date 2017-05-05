
USE `pcm`;


CREATE TABLE `Message` (
    `basename` VARCHAR( 31 ) NOT NULL ,
    `language` VARCHAR( 7 ) NULL ,
    `country` VARCHAR( 7 ) NULL ,
    `variant` VARCHAR( 7 ) NULL ,
    `key` VARCHAR( 255 ) NULL ,
    `message` TEXT NULL
);

-- ENGLISH

-- PURPOSR OF USE

INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('en','en_US','US','','PURPOSE.TREAT.DISPLAYNAME','Treatment');
INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('en','en_US','US','','PURPOSE.TREAT.DESCRIPTION','To perform one or more operations on information for the provision of health care.');

INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('en','en_US','US','','PURPOSE.HPAYMT.DISPLAYNAME','Healthcare Payment');
INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('en','en_US','US','','PURPOSE.HPAYMT.DESCRIPTION','To perform one or more operations on information for conducting financial or contractual activities related to payment for the provision of health care.');


INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('en','en_US','US','','PURPOSE.HRESCH.DISPLAYNAME','Healthcare Researc');
INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('en','en_US','US','','PURPOSE.HRESCH.DESCRIPTION','To perform one or more operations on information for conducting scientific investigations to obtain health care knowledge.');


-- SPANISH

-- PURPOSE OF USE

INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('es','es_US','US','','PURPOSE.TREAT.DISPLAYNAME','Tratamiento');
INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('es','es_US','US','','PURPOSE.TREAT.DESCRIPTION','Realizar una o más operaciones de información para la provisión de servicios de salud.');

INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('es','es_US','US','','PURPOSE.HPAYMT.DISPLAYNAME','Pago de atención médica');
INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('es','es_US','US','','PURPOSE.HPAYMT.DESCRIPTION','Realizar una o más operaciones sobre información para la realización de actividades financieras o contractuales relacionadas con el pago de la prestación de atención de salud.');


INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('es','es_US','US','','PURPOSE.HRESCH.DISPLAYNAME','Investigación de la salud');
INSERT INTO `message` (basename, language, country, variant, `key`, message) values ('es','es_US','US','','PURPOSE.HRESCH.DESCRIPTION','Realizar una o más operaciones sobre información para realizar investigaciones científicas para obtener conocimiento sobre el cuidado de la salud.');


