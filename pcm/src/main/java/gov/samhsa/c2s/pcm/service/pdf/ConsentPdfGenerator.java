package gov.samhsa.c2s.pcm.service.pdf;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

public interface ConsentPdfGenerator {
    void addConsentTitle(String pdfType, float startYCoordinate, PDPage page, PDPageContentStream contentStream) throws IOException;

    void addConsentReferenceNumberAndPatientInfo(Consent consent, PatientDto patientProfile, float startYCoordinate, PDPageContentStream contentStream) throws IOException;

    byte[] generateConsentPdf(Consent consent, PatientDto patientDto, Date operatedOnDateTime, String consentTerms, Optional<UserDto> operatedByUserDto, Optional<Boolean> operatedByPatient) throws IOException;
}
