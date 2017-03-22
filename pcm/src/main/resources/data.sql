-- FIXME: remove this file after initial implementation is done
INSERT INTO sensitivity_category VALUES (1, 'http://hl7.org/fhir/v3/ActCode', 'ETH');
INSERT INTO sensitivity_category VALUES (2, 'http://hl7.org/fhir/v3/ActCode', 'HIV');
INSERT INTO sensitivity_category VALUES (3, 'http://hl7.org/fhir/v3/ActCode', 'PSY');
INSERT INTO sensitivity_category VALUES (4, 'http://hl7.org/fhir/v3/ActCode', 'COM');
INSERT INTO sensitivity_category VALUES (5, 'http://hl7.org/fhir/v3/ActCode', 'SEX');
INSERT INTO sensitivity_category VALUES (6, 'http://hl7.org/fhir/v3/ActCode', 'ALC');

INSERT INTO purpose VALUES (1,
                            'To perform one or more operations on information for the provision of health care.',
                            'Healthcare Treatment',
                            'http://hl7.org/fhir/v3/ActReason', 'TREAT');
INSERT INTO purpose VALUES (2,
                            'To perform one or more operations on information for conducting financial or contractual activities related to payment for the provision of health care.',
                            'Payment', 'http://hl7.org/fhir/v3/ActReason', 'HPAYMT');
INSERT INTO purpose VALUES (3,
                            'To perform one or more operations on information for conducting scientific investigations to obtain health care knowledge.',
                            'Research', 'http://hl7.org/fhir/v3/ActReason', 'HRESCH');

INSERT INTO consent_attestation_term VALUES (1,
                                             'I, ${ATTESTER_FULL_NAME}, understand that my records are protected under the federal regulations governing Confidentiality of Alcohol and Drug Abuse Patient Records, 42 CFR part 2, and cannot be disclosed without my written permission or as otherwise permitted by 42 CFR part 2. I also understand that I may revoke this consent at any time except to the extent that action has been taken in reliance on it, and that any event this consent expires automatically as follows:');
INSERT INTO consent_revocation_term VALUES (1,
                                            'I have previously signed a patient consent form allowing my providers to access my electronic health records through the Consent2Share system and now want to withdraw that consent. If I sign this form as the Patient\'s Legal Representative, I understand that all references in this form to \"me\" or \"my\" refer to the Patient.\n\nBy withdrawing my Consent, I understand that:\n\n	1. I Deny Consent for all Participants to access my electronic health information through Consent2Share for any purpose, EXCEPT in a medical emergency.\n	2. Health care provider and health insurers that I am enrolled with will no longer be able to access health information about me through Consent2Share, except in an emergency.\n	3. The Withdrawal of Consent will not affect the exchange of my health information while my Consent was in effect.\n	4. No Consent2Share participating provider will deny me medical care and my insurance eligibility will not be affected based on my Withdrawal of Consent.\n	5. If I wish to reinstate Consent, I may do so by signing and completing a new Patient Consent form and returning it to a participating provider or payer.\n	6. Withdrawing my consent does not prevent my health care provider from submitting claims to my health insurer for reimbursement for services rendered to me.\n	7. I understand that I will get a copy of this form after I sign it.');