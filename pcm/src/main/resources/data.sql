-- FIXME: remove this file after initial implementation is done
INSERT INTO sensitivity_category VALUES (1,
                                         'Drug abuse or substance abuse is the use of mood-altering substances that interfere with or have a negative effect on a person’s life. These include negative effects on a person’s physical, psychological, social, emotional, occupational, and educational well-being. Drug abuse is characterized by dysfunction and negative consequences. Most drugs of abuse are mood altering (they change a person’s mood or feeling), and fall in three categories: stimulants, depressants, and hallucinogens.',
                                         'Drug use information',
                                         'http://hl7.org/fhir/v3/ActCode',
                                         'ETH');
INSERT INTO sensitivity_category VALUES (2,
                                        'Human immunodeficiency virus (HIV) is a virus that weakens a person’s immune system by destroying important cells that fight disease and infection. HIV infection typically begins with flu-like symptoms followed by a long symptom-free period. HIV can be controlled with antiretroviral therapy. Untreated, HIV can advance to acquire immunodeficiency syndrome (AIDS), the most severe phase of HIV infection. People with AIDS have such badly damaged immune systems that they get an increasing number of severe illnesses, which can lead to death.',
                                        'HIV/AIDS information',
                                        'http://hl7.org/fhir/v3/ActCode',
                                        'HIV');
INSERT INTO sensitivity_category VALUES (3,
                                        'Mental illness or a psychiatric disorder is a condition that affects a person’s thinking, feeling, or mood, and may affect his or her ability to relate to others and function well on a daily basis. Mental illnesses are medical conditions that often cause a diminished ability to cope with the ordinary demands of life. Like other medical disorders, mental illness ranges from mild to severe. There is a wide variety of treatments for mental illnesses.',
                                        'Mental health information',
                                        'http://hl7.org/fhir/v3/ActCode',
                                        'PSY');
INSERT INTO sensitivity_category VALUES (4,
                                        'Communicable diseases, also known as infectious diseases are illnesses that result from the infection, presence, and growth of organisms and microorganisms such as bacteria, viruses, fungi, and parasites. They can be spread, directly or indirectly, from one person to another.',
                                        'Communicable disease information',
                                        'http://hl7.org/fhir/v3/ActCode',
                                        'COM');
INSERT INTO sensitivity_category VALUES (5,
                                        'Good sexual and reproductive health is a state of complete physical, mental, and social well-being in all matters relating to the reproductive system, at all stages of life. It implies that people are able to have a satisfying and safe sex life, the capacity to reproduce, and the freedom to decide if, when, and how often to do so. Similarly, sexual health is a state of physical, emotional, and social well-being in relation to sexuality. It is not simply the absence of disease, dysfunction, or infirmity.',
                                        'Sexuality and reproductive health information',
                                        'http://hl7.org/fhir/v3/ActCode',
                                        'SEX');
INSERT INTO sensitivity_category VALUES (6,
                                        'Alcohol abuse is the use of alcohol in such a way that it interferes with or has a negative effect on a person’s life. These include negative effects on a person’s physical, psychological, social, emotional, occupational, and educational well-being. Alcoholism or alcohol addiction is a primary, chronic, and disabling disorder that involves compulsion, loss of control, and continued use despite negative consequences. Genetic, psychosocial, and environmental factors influence its development and outcome.',
                                        'Alcohol use and Alcoholism Information',
                                        'http://hl7.org/fhir/v3/ActCode',
                                        'ALC');

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