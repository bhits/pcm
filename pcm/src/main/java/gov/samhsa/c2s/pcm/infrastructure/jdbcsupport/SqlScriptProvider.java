package gov.samhsa.c2s.pcm.infrastructure.jdbcsupport;

public interface SqlScriptProvider {

    /**
     * Gets the sql script.
     *
     * @return the sql script
     */
    String getSqlScriptByIndex(int indexOfSqls);
}