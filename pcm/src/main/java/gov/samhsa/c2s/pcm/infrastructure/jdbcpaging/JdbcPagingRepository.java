package gov.samhsa.c2s.pcm.infrastructure.jdbcpaging;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JdbcPagingRepository {

    <T> Page<T> findAll(String sqlFilePath, Pageable pageable);

    <T> Page<T> findAllByArgs(String sqlFilePath, Pageable pageable, Object... args);
}
