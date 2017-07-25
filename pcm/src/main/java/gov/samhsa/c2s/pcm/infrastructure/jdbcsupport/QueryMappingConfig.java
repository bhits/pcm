package gov.samhsa.c2s.pcm.infrastructure.jdbcsupport;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public interface QueryMappingConfig {
    RowMapper getRowMapper();

    String getTableName();

    List<String> getIdColumn();
}
