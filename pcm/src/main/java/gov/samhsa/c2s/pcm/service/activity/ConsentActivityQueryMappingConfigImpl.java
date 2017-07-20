package gov.samhsa.c2s.pcm.service.activity;

import gov.samhsa.c2s.pcm.infrastructure.jdbcsupport.QueryMappingConfig;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ConsentActivityQueryMappingConfigImpl implements QueryMappingConfig {

    private static final String WHERE_CONDITION = "patient_id";
    private static final String CONDITION_BELONG_TO_TABLE = "consent_aud";

    @Override
    public RowMapper getRowMapper() {
        return new ConsentActivityRowMapper();
    }

    @Override
    public String getTableName() {
        return CONDITION_BELONG_TO_TABLE;
    }

    @Override
    public String getIdColumn() {
        return WHERE_CONDITION;
    }
}
