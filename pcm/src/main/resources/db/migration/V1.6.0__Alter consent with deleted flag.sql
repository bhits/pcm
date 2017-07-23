ALTER TABLE consent
  ADD deleted BIT NOT NULL;
ALTER TABLE consent_aud
  ADD deleted BIT;