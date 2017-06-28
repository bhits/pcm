

RENAME TABLE consent_share_sensitivity_categories to consent_sensitivity_categories;
ALTER TABLE consent_sensitivity_categories CHANGE `share_sensitivity_categories_id` `sensitivity_categories_id` BIGINT(20);

RENAME TABLE consent_share_sensitivity_categories_aud to consent_sensitivity_categories_aud;
ALTER TABLE consent_sensitivity_categories_aud CHANGE `share_sensitivity_categories_id` `sensitivity_categories_id` BIGINT(20);


RENAME TABLE consent_share_purposes to consent_purposes;
ALTER TABLE consent_purposes CHANGE `share_purposes_id` `purposes_id` BIGINT(20);

RENAME TABLE consent_share_purposes_aud to consent_purposes_aud;
ALTER TABLE consent_purposes_aud CHANGE `share_purposes_id` `purposes_id` BIGINT(20);
