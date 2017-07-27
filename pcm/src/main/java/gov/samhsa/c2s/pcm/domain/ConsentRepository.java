package gov.samhsa.c2s.pcm.domain;

import gov.samhsa.c2s.pcm.domain.valueobject.ConsentStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConsentRepository extends JpaRepository<Consent, Long> {

    String NOT_ALLOWED_TO_DELETE_CONSENT_EXCEPTION_MESSAGE = "Not allowed to delete consent due to audit purpose";

    Optional<Consent> findOneByIdAndPatientIdAndConsentAttestationIsNullAndConsentRevocationIsNull(Long consentId,
                                                                                                   String patientId);

    Optional<Consent> findOneByIdAndPatientIdAndConsentAttestationIsNotNullAndConsentRevocationIsNull(Long consentId,
                                                                                                      String patientId);

    Optional<Consent> findOneByIdAndPatientIdAndConsentAttestationIsNotNullAndConsentRevocationIsNotNull(Long consentId, String patientId);

    Optional<Consent> findOneByIdAndPatientId(Long consentId, String patientId);

    Page<Consent> findAllByPatientIdOrderByLastUpdatedDateDesc(String patientId, Pageable pageable);

    Optional<Consent>
    findOneByPatientIdAndFromProvidersIdentifierValueAndToProvidersIdentifierValueAndSharePurposesIdentifierValueAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndConsentAttestationNotNullAndConsentRevocationIsNullAndConsentStage(String patientId, String fromoProviderNpi, String toProviderNpi, String pouValue, LocalDateTime startDateBefore, LocalDateTime endDateAfter, ConsentStage consentStage);

    @Override
    default void deleteInBatch(Iterable<Consent> entities) {
        throw new UnsupportedOperationException(NOT_ALLOWED_TO_DELETE_CONSENT_EXCEPTION_MESSAGE);
    }

    @Override
    default void deleteAllInBatch() {
        throw new UnsupportedOperationException(NOT_ALLOWED_TO_DELETE_CONSENT_EXCEPTION_MESSAGE);
    }

    @Override
    default void delete(Long aLong) {
        throw new UnsupportedOperationException(NOT_ALLOWED_TO_DELETE_CONSENT_EXCEPTION_MESSAGE);
    }

    @Override
    default void delete(Consent entity) {
        throw new UnsupportedOperationException(NOT_ALLOWED_TO_DELETE_CONSENT_EXCEPTION_MESSAGE);
    }

    @Override
    default void delete(Iterable<? extends Consent> entities) {
        throw new UnsupportedOperationException(NOT_ALLOWED_TO_DELETE_CONSENT_EXCEPTION_MESSAGE);
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException(NOT_ALLOWED_TO_DELETE_CONSENT_EXCEPTION_MESSAGE);
    }
}
