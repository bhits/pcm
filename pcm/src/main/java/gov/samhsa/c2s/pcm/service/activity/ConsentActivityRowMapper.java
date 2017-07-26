package gov.samhsa.c2s.pcm.service.activity;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsentActivityRowMapper implements RowMapper<ConsentActivityQueryResult> {

    private static final String CONSENT_REFERENCE_ID = "consent_reference_id";
    private static final String CONSENT_STAGE = "consent_stage";
    private static final String REV_TYPE = "revtype";
    private static final String LAST_UPDATED_BY = "last_updated_by";
    private static final String LAST_UPDATED_DATE = "last_updated_date";

    @Override
    public ConsentActivityQueryResult mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ConsentActivityQueryResult.builder()
                .consentReferenceId(rs.getString(CONSENT_REFERENCE_ID))
                .consentStage(rs.getString(CONSENT_STAGE))
                .revType(rs.getString(REV_TYPE))
                .lastUpdatedBy(rs.getString(LAST_UPDATED_BY))
                .lastUpdatedDateTime(rs.getString(LAST_UPDATED_DATE))
                .build();
    }
}
