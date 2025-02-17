package io.zeebe.monitor.repository;

import io.zeebe.monitor.entity.ProcessInstanceBusinessKeyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ProcessInstanceBusinessKeyRepository
    extends CrudRepository<ProcessInstanceBusinessKeyEntity, Long> {

  @Query(
      nativeQuery = true,
      value =
          """
                    select p.INSTANCE_KEY_ from PROCESS_INSTANCE_BUSINESS_KEY p
                    where p.BUSINESS_KEY_ like CONCAT(:businessKey,'%')
            """)
  List<Long> findAllByBusinessKeyStartsWith(@Param("businessKey") String businessKey);
}
