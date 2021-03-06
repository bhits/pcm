package gov.samhsa.c2s.pcm.infrastructure.jdbcpaging.sql;

public interface SqlScriptProvider {

    /**
     * Gets the sql script.
     *
     * @return the sql script
     */
    String getSqlScriptByPath(String sqlFilePath);
}