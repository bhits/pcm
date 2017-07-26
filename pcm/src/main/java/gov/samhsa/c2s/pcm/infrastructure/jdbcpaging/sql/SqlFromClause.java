package gov.samhsa.c2s.pcm.infrastructure.jdbcpaging.sql;

import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

@Data
public class SqlFromClause {

    private final String tableName;
    private final String fromClause;
    private final List<String> idColumns;

    public SqlFromClause(String tableName, String fromClause, List<String> idColumns) {
        Assert.notNull(tableName);
        Assert.notNull(idColumns);
        Assert.isTrue(idColumns.size() > 0, "At least one primary key column must be provided");

        this.tableName = tableName;
        this.fromClause = StringUtils.hasText(fromClause) ? fromClause : tableName;
        this.idColumns = idColumns;
    }
}
