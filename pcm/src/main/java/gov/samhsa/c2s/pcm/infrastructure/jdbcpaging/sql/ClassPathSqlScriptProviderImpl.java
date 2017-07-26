package gov.samhsa.c2s.pcm.infrastructure.jdbcpaging.sql;

import gov.samhsa.c2s.pcm.infrastructure.exception.SqlScriptFileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClassPathSqlScriptProviderImpl implements SqlScriptProvider {

    @Override
    public String getSqlScriptByPath(String sqlFilePath) {
        try {
            byte[] fileBytes = IOUtils.toByteArray(new ClassPathResource(sqlFilePath).getInputStream());
            String sqlString = new String(fileBytes);
            log.debug("Sql Script: " + sqlString);
            return sqlString;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SqlScriptFileNotFoundException("SQL script file cannot be found:" + e);
        }
    }
}
