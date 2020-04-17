package io.zeebe.monitor.repository;

import io.zeebe.monitor.entity.HazelcastConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HazelcastConfigRepository extends CrudRepository<HazelcastConfig, String> {}
