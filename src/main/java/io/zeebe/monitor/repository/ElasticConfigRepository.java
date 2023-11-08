package io.zeebe.monitor.repository;

import io.zeebe.monitor.entity.ElasticConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticConfigRepository extends CrudRepository<ElasticConfig, String>  {
}
