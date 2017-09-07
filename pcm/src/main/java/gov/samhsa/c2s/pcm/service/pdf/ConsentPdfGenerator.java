package gov.samhsa.c2s.pcm.service.pdf;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

public interface ConsentPdfGenerator {
    void addConsentTitle(String pdfType, float startYCoordinate, PDPage page, PDPageContentStream contentStream) throws IOException;

    void addConsentReferenceNumberAndPatientInfo(Consent consent, PatientDto patientProfile, float startYCoordinate, PDFont defaultFont, PDPageContentStream contentStream) throws IOException;

    void addConsentSigningDetails(PatientDto patient, Optional<UserDto> signedByUserDto, Date signedOnDateTime, Optional<Boolean> signedByPatient, float startYCoordinate, PDFont defaultFont, PDPageContentStream contentStream) throws IOException;

    byte[] generateConsentPdf(Consent consent, PatientDto patientDto, Date operatedOnDateTime, String consentTerms, Optional<UserDto> operatedByUserDto, Optional<Boolean> operatedByPatient) throws IOException;
}
