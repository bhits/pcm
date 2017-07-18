-- Pcm Sample Data
-- ------------------------------------------------------

USE `pcm`;

--
-- Insert data for table `consent_attestation_term`
--
/* Patient signs the consent */
INSERT INTO consent_attestation_term VALUES (1,
                                             'I, ${ATTESTER_FULL_NAME}, understand that my records are protected under the federal regulations governing Confidentiality of Alcohol and Drug Abuse Patient Records, 42 CFR part 2, and cannot be disclosed without my written permission or as otherwise permitted by 42 CFR part 2. I also understand that I may revoke this consent at any time except to the extent that action has been taken in reliance on it, and that any event this consent expires automatically as follows:');
/* Provider signs the consent (PDF version) */											 
INSERT INTO consent_attestation_term VALUES (2,
                                             'I, ${ATTESTER_FULL_NAME}, understand that my records are protected under the federal regulations governing Confidentiality of Alcohol and Drug Abuse Patient Records, 42 CFR part 2, and cannot be disclosed without my written permission or as otherwise permitted by 42 CFR part 2. I also understand that I may revoke this consent at any time except to the extent that action has been taken in reliance on it, and that any event this consent expires automatically as set forth below.\n \n	By signning this form below, I acknowledge that I have reviewed all of the information on this consent, confirm that such information is accurate, and accept and understand the terms of this consent.');		
											 
/* Provider signs the consent (Provider UI version) */	
INSERT INTO consent_attestation_term VALUES (3,
                                             'I, ${PROVIDER_FULL_NAME}, attest that I completed this consent form granting permission to disclose ${ATTESTER_FULL_NAME}\’s records governed by 42 CFR Part 2\’s regulations protecting the Confidentiality of Alcohol and Drug Abuse Patient Records in the presence of the patient and in accordance with their preferences, and that the patient acknowledges that he or she reviewed all of the information on this consent, confirmed that such information is accurate, and has accepted and understood the terms of this consents as evidenced by his or her signature (or the signature of his or her personal representative).');												 