package gov.samhsa.c2s.pcm.infrastructure.jdbcsupport.sql;

import gov.samhsa.c2s.pcm.config.ActivityProperties;
import gov.samhsa.c2s.pcm.infrastructure.exception.SqlScriptFileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class ClassPathSqlScriptProviderImpl implements SqlScriptProvider {

    @Autowired
    private ActivityProperties activityProperties;

    @Override
    public String getSqlScriptByPath(String sqlFilePath) {
        try {
            byte[] fileBytes = IOUtils.toByteArray(getSqlResourceByPath(sqlFilePath));
            String sqlString = new String(fileBytes);
            log.debug("Sql Script: " + sqlString);
            return sqlString;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new SqlScriptFileNotFoundException("SQL script file cannot be found:" + e);
        }
    }

    private InputStream getSqlResourceByPath(String sqlPath) throws IOException {
        String sqlFilePath = activityProperties
                .getActivity()
                .getSqls()
                .stream()
                .filter(sql -> sql.getFilePath().equalsIgnoreCase(sqlPath))
                .map(ActivityProperties.Activity.Sql::getFilePath)
                .findAny().orElseThrow(SqlScriptFileNotFoundException::new);
        return new ClassPathResource(sqlFilePath).getInputStream();
    }
}
