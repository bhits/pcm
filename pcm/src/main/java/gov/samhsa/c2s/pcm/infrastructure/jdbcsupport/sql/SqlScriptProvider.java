package gov.samhsa.c2s.pcm.infrastructure.jdbcsupport.sql;

public interface SqlScriptProvider {

    /**
     * Gets the sql script.
     *
     * @return the sql script
     */
    String getSqlScriptByIndex(int indexOfSqls);
}