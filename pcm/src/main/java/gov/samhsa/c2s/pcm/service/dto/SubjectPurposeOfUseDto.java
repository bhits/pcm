package gov.samhsa.c2s.pcm.service.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum SubjectPurposeOfUseDto {
    @XmlEnumValue("TREATMENT")
    HEALTHCARE_TREATMENT("TREATMENT", "TREAT"),
    @XmlEnumValue("PAYMENT")
    PAYMENT("PAYMENT", "HPAYMT"),
    @XmlEnumValue("RESEARCH")
    RESEARCH("RESEARCH", "HRESCH");

    private final String purpose;
    private final String purposeFhir;

    SubjectPurposeOfUseDto(String p, String purposeFhir) {
        this.purpose = p;
        this.purposeFhir = purposeFhir;
    }

    public static SubjectPurposeOfUseDto fromValue(String v) {
        return valueOf(v);
    }

    public static SubjectPurposeOfUseDto fromPurpose(String purposeOfUse) {
        for (SubjectPurposeOfUseDto p : SubjectPurposeOfUseDto.values()) {
            if (p.getPurpose().equals(purposeOfUse)) {
                return p;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("The abbreviation '");
        builder.append(purposeOfUse);
        builder.append("' is not defined in this enum.");
        throw new IllegalArgumentException(builder.toString());
    }

    public static SubjectPurposeOfUseDto fromPurposeFhir(String purposeOfUse) {
        for (SubjectPurposeOfUseDto p : SubjectPurposeOfUseDto.values()) {
            if (p.getPurposeFhir().equals(purposeOfUse)) {
                return p;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("The abbreviation '");
        builder.append(purposeOfUse);
        builder.append("' is not defined in this enum.");
        throw new IllegalArgumentException(builder.toString());
    }


    public String getPurpose() {
        return purpose;
    }

    public String getPurposeFhir() {
        return purposeFhir;
    }
}
