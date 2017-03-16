-- FIXME: remove this file after initial implementation is done
insert into sensitivity_category values (1, 'http://hl7.org/fhir/v3/ActCode', 'ETH');
insert into sensitivity_category values (2, 'http://hl7.org/fhir/v3/ActCode', 'HIV');
insert into sensitivity_category values (3, 'http://hl7.org/fhir/v3/ActCode', 'PSY');
insert into sensitivity_category values (4, 'http://hl7.org/fhir/v3/ActCode', 'COM');
insert into sensitivity_category values (5, 'http://hl7.org/fhir/v3/ActCode', 'SEX');
insert into sensitivity_category values (6, 'http://hl7.org/fhir/v3/ActCode', 'ALC');

insert into purpose values (1, null, 'http://hl7.org/fhir/v3/ActReason', 'TREAT');
insert into purpose values (2, null, 'http://hl7.org/fhir/v3/ActReason', 'HRESCH');
insert into purpose values (3, null, 'http://hl7.org/fhir/v3/ActReason', 'HPAYMT');