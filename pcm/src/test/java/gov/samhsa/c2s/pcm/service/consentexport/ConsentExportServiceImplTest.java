package gov.samhsa.c2s.pcm.service.consentexport;


import gov.samhsa.c2s.common.consentgen.ConsentBuilderImpl;
import gov.samhsa.c2s.common.consentgen.pg.XacmlXslUrlProviderImpl;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.c2s.common.document.transformer.XmlTransformerImpl;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.c2s.pcm.config.FhirProperties;
import gov.samhsa.c2s.pcm.infrastructure.UmsService;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.service.ConsentService;
import gov.samhsa.c2s.pcm.service.dto.AbstractProviderDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentXacmlDto;
import gov.samhsa.c2s.pcm.service.dto.DetailedConsentDto;
import gov.samhsa.c2s.pcm.service.dto.IdentifierDto;
import gov.samhsa.c2s.pcm.service.dto.OrganizationDto;
import gov.samhsa.c2s.pcm.service.dto.PatientIdDto;
import gov.samhsa.c2s.pcm.service.dto.PractitionerDto;
import gov.samhsa.c2s.pcm.service.dto.PurposeDto;
import gov.samhsa.c2s.pcm.service.dto.SensitivityCategoryDto;
import gov.samhsa.c2s.pcm.service.dto.XacmlRequestDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentExportServiceImplTest {
    private static final String XACML_ATTRIBUTE_VALUE_XPATH_TEMPLATE = "/Policy/Rule/Condition//Apply[SubjectAttributeDesignator[@AttributeId='%1']]/following-sibling::AttributeValue/text()";
    private static final String XACML_OBLIGATION_ATTRIBUTE_ASSIGNMENT_XPATH_TEMPLATE = "/Policy/Obligations/Obligation[@ObligationId='urn:samhsa:names:tc:consent2share:1.0:obligation:share-sensitivity-policy-code']/AttributeAssignment/text()";

    private static final String FROM_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject";
    private static final String TO_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject";
    private static final String PURPOSE_OF_USE_ATTRIBUTE_ID = "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse";

    private static final String FHIR_NPI_SYSTEM = "http://hl7.org/fhir/sid/us-npi";
    private static final String FHIR_V3_ACT_REASON_SYSTEM = "http://hl7.org/fhir/v3/ActReason";
    private static final String PCM_ORG = "urn:oid:1.3.6.1.4.1.21367.13.20.200";

    private static final String TREATMENT = "TREAT";
    private static final String PAYMENT = "HPAYMT";
    private static final String RESEARCH = "HRESCH";

    private static final String COM = "COM";
    private static final String ETH = "ETH";
    private static final String ALC = "ALC";
    private static final String HIV = "HIV";
    private static final String PSY = "PSY";
    private static final String SEX = "SEX";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // mocks
    @Mock
    private UmsService umsService;
    @Mock
    private FhirProperties fhirProperties;
    @Mock
    private FhirProperties.Npi npi;
    @Mock
    private ConsentService consentService;

    // real instances
    private ConsentDtoFactoryImpl consentDtoFactory;
    private ConsentExportMapperImpl consentExportMapper;
    private ConsentBuilderImpl consentBuilder;
    private XacmlXslUrlProviderImpl xacmlXslUrlProvider;
    private SimpleMarshallerImpl simpleMarshaller;
    private XmlTransformerImpl xmlTransformer;

    // utils
    private DocumentAccessor documentAccessor;
    private DocumentXmlConverter documentXmlConverter;

    // sut
    private ConsentExportServiceImpl sut;

    @Before
    public void setUp() throws Exception {
        documentAccessor = new DocumentAccessorImpl();
        documentXmlConverter = new DocumentXmlConverterImpl();
        when(fhirProperties.getNpi()).thenReturn(npi);
        when(npi.getSystem()).thenReturn(FHIR_NPI_SYSTEM);

        simpleMarshaller = new SimpleMarshallerImpl();
        xmlTransformer = new XmlTransformerImpl(simpleMarshaller);
        xacmlXslUrlProvider = new XacmlXslUrlProviderImpl();
        consentExportMapper = new ConsentExportMapperImpl(fhirProperties);
        consentDtoFactory = new ConsentDtoFactoryImpl(consentExportMapper);
        consentBuilder = new ConsentBuilderImpl(PCM_ORG,
                xacmlXslUrlProvider,
                consentDtoFactory,
                xmlTransformer);
        sut = new ConsentExportServiceImpl(consentService, umsService, consentBuilder);
    }

    @Test
    public void exportConsent2XACML_Many_To_Many_Support() throws Exception {
        final String identifierValue = "identifierValue";
        final String identifierSystem = "identifierSystem";
        final XacmlRequestDto xacmlRequestDto = XacmlRequestDto.builder()
                .patientId(PatientIdDto.builder().root(identifierSystem).extension(identifierValue).build())
                .build();
        final String mrn = "mrn";
        final String lastName = "lastName";
        final String firstName = "firstName";
        final PatientDto patientDto = PatientDto.builder()
                .mrn(mrn)
                .lastName(lastName)
                .firstName(firstName)
                .build();
        when(umsService.getPatientByIdentifierValueAndIdentifierSystem(identifierValue, identifierSystem)).thenReturn(patientDto);
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = startDate.plusYears(1);
        final String consentReferenceId = "consentReferenceId";
        final String fromNpi1 = "1111111111";
        final String fromNpi2 = "2222222222";
        final String fromNpi3 = "3333333333";
        final String toNpi1 = "4444444444";
        final String toNpi2 = "5555555555";
        final String toNpi3 = "6666666666";
        final DetailedConsentDto detailedConsentDto = DetailedConsentDto.builder()
                .consentReferenceId(consentReferenceId)
                .fromProviders(providers(practitioner(fromNpi1), organization(fromNpi2), practitioner(fromNpi3)))
                .toProviders((providers(organization(toNpi1), practitioner(toNpi2), organization(toNpi3))))
                .sharePurposes(purposes(
                        purpose(TREATMENT, "Treatment", "To perform one or more operations on information for the provision of health care."),
                        purpose(PAYMENT, "Healthcare Payment", "To perform one or more operations on information for conducting financial or contractual activities related to payment for the provision of health care."),
                        purpose(RESEARCH, "Healthcare Research", "To perform one or more operations on information for conducting scientific investigations to obtain health care knowledge.")
                ))
                .shareSensitivityCategories(sensitivities(
                        sensitivity(COM, "Communicable disease information", "Communicable diseases, also known as infectious diseases are illnesses that result from the infection, presence, and growth of organisms and microorganisms such as bacteria, viruses, fungi, and parasites. They can be spread, directly or indirectly, from one person to another."),
                        sensitivity(ETH, "Drug use information", "Drug abuse or substance abuse is the use of mood-altering substances that interfere with or have a negative effect on a person’s life. These include negative effects on a person’s physical, psychological, social, emotional, occupational, and educational well-being. Drug abuse is characterized by dysfunction and negative consequences. Most drugs of abuse are mood altering (they change a person’s mood or feeling), and fall in three categories: stimulants, depressants, and hallucinogens."),
                        sensitivity(ALC, "Alcohol use and Alcoholism Information", "Alcohol abuse is the use of alcohol in such a way that it interferes with or has a negative effect on a person’s life. These include negative effects on a person’s physical, psychological, social, emotional, occupational, and educational well-being. Alcoholism or alcohol addiction is a primary, chronic, and disabling disorder that involves compulsion, loss of control, and continued use despite negative consequences. Genetic, psychosocial, and environmental factors influence its development and outcome."),
                        sensitivity(HIV, "HIV/AIDS information", "Human immunodeficiency virus (HIV) is a virus that weakens a person’s immune system by destroying important cells that fight disease and infection. HIV infection typically begins with flu-like symptoms followed by a long symptom-free period. HIV can be controlled with antiretroviral therapy. Untreated, HIV can advance to acquire immunodeficiency syndrome (AIDS), the most severe phase of HIV infection. People with AIDS have such badly damaged immune systems that they get an increasing number of severe illnesses, which can lead to death."),
                        sensitivity(PSY, "Mental health information", "Mental illness or a psychiatric disorder is a condition that affects a person’s thinking, feeling, or mood, and may affect his or her ability to relate to others and function well on a daily basis. Mental illnesses are medical conditions that often cause a diminished ability to cope with the ordinary demands of life. Like other medical disorders, mental illness ranges from mild to severe. There is a wide variety of treatments for mental illnesses."),
                        sensitivity(SEX, "Sexuality and reproductive health information", "Good sexual and reproductive health is a state of complete physical, mental, and social well-being in all matters relating to the reproductive system, at all stages of life. It implies that people are able to have a satisfying and safe sex life, the capacity to reproduce, and the freedom to decide if, when, and how often to do so. Similarly, sexual health is a state of physical, emotional, and social well-being in relation to sexuality. It is not simply the absence of disease, dysfunction, or infirmity.")
                ))
                .startDate(startDate)
                .endDate(endDate)
                .build();
        when(consentService.searchConsent(xacmlRequestDto)).thenReturn(detailedConsentDto);

        // Act
        final ConsentXacmlDto consentXacmlDto = sut.exportConsent2XACML(xacmlRequestDto);
        final String actualXacml = new String(consentXacmlDto.getConsentXacml(), consentXacmlDto.getConsentXacmlEncoding());
        final String actualXacmlNoNamespace = actualXacml.replace("xmlns=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\"", "");
        final Document actualXacmlNoNamespaceDocument = documentXmlConverter.loadDocument(actualXacmlNoNamespace);
        // lookup values from xacml
        final List<String> froms = documentAccessor
                .getNodeListAsStream(actualXacmlNoNamespaceDocument, XACML_ATTRIBUTE_VALUE_XPATH_TEMPLATE, FROM_ATTRIBUTE_ID)
                .map(Node::getNodeValue)
                .collect(toList());
        final List<String> tos = documentAccessor
                .getNodeListAsStream(actualXacmlNoNamespaceDocument, XACML_ATTRIBUTE_VALUE_XPATH_TEMPLATE, TO_ATTRIBUTE_ID)
                .map(Node::getNodeValue)
                .collect(toList());
        final List<String> pous = documentAccessor
                .getNodeListAsStream(actualXacmlNoNamespaceDocument, XACML_ATTRIBUTE_VALUE_XPATH_TEMPLATE, PURPOSE_OF_USE_ATTRIBUTE_ID)
                .map(Node::getNodeValue)
                .collect(toList());
        final List<String> sensitivities = documentAccessor
                .getNodeListAsStream(actualXacmlNoNamespaceDocument, XACML_OBLIGATION_ATTRIBUTE_ASSIGNMENT_XPATH_TEMPLATE)
                .map(Node::getNodeValue)
                .collect(toList());

        // Assert
        assertTrue("Missing at least one of FROM NPIs", froms.containsAll(Arrays.asList(fromNpi1, fromNpi2, fromNpi3)));
        assertTrue("Missing at least one of TO NPIs", tos.containsAll(Arrays.asList(toNpi1, toNpi2, toNpi3)));
        assertTrue("Missing at least one of Purpose of Use", pous.containsAll(Arrays.asList(TREATMENT, PAYMENT, RESEARCH)));
        assertTrue("Missing at least one of Sensitivities", sensitivities.containsAll(Arrays.asList(COM, ETH, ALC, HIV, PSY, SEX)));
    }

    private List<SensitivityCategoryDto> sensitivities(SensitivityCategoryDto... sensitivityCategoryDtos) {
        return Arrays.asList(sensitivityCategoryDtos);
    }

    private SensitivityCategoryDto sensitivity(String code, String display, String description) {
        return SensitivityCategoryDto.builder().identifier(IdentifierDto.of(FHIR_V3_ACT_REASON_SYSTEM, code)).display(display).description(description).build();
    }

    private List<PurposeDto> purposes(PurposeDto... purposeDtos) {
        return Arrays.asList(purposeDtos);
    }

    private PurposeDto purpose(String identifierValue, String display, String description) {
        return PurposeDto.builder()
                .display(display)
                .description(description)
                .identifier(IdentifierDto.of(FHIR_V3_ACT_REASON_SYSTEM, identifierValue))
                .build();
    }

    private List<AbstractProviderDto> providers(AbstractProviderDto... providers) {
        return Arrays.asList(providers);
    }

    private PractitionerDto practitioner(String npi) {
        return PractitionerDto.builder().identifiers(Stream.of(IdentifierDto.of(FHIR_NPI_SYSTEM, npi)).collect(toSet())).build();
    }

    private OrganizationDto organization(String npi) {
        return OrganizationDto.builder().identifiers(Stream.of(IdentifierDto.of(FHIR_NPI_SYSTEM, npi)).collect(toSet())).build();
    }
}