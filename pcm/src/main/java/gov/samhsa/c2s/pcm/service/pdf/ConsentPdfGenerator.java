package gov.samhsa.c2s.pcm.service.pdf;


import gov.samhsa.c2s.pcm.domain.Consent;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.infrastructure.dto.UserDto;
import gov.samhsa.c2s.pcm.service.pdf.hexPdf.HexPDF;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

public interface ConsentPdfGenerator {

    String getConsentTitle(String pdfType);

    void drawConsentTitle(HexPDF document, String consentTitle);

    void setPageFooter(HexPDF document, String consentTitle);

    void drawPatientInformationSection(HexPDF document, Consent consent, PatientDto patientProfile);

    void addConsentSigningDetails(HexPDF document, PatientDto patient, Optional<UserDto> signedByUserDto, Date signedOnDateTime, Optional<Boolean> signedByPatient) throws IOException;

    byte[] generateConsentPdf(Consent consent, PatientDto patientDto, Date operatedOnDateTime, String consentTerms, Optional<UserDto> operatedByUserDto, Optional<Boolean> operatedByPatient) throws IOException;
}
