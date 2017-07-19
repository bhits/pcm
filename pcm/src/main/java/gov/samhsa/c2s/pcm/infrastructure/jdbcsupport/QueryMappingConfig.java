package gov.samhsa.c2s.pcm.infrastructure.jdbcsupport;

import org.springframework.jdbc.core.RowMapper;

public interface QueryMappingConfig {
    RowMapper getRowMapper();

    String getTableName();

    String getIdColumn();
}
