package gov.samhsa.c2s.pcm.infrastructure.jdbcsupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JdbcPagingRepository {

    <T> Page<T> findAll(int indexOfSqls, Pageable pageable);

    <T> Page<T> findAllByArgs(int indexOfSqls, Pageable pageable, Object... args);
}
