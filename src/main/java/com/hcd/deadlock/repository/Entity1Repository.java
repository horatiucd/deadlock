package com.hcd.deadlock.repository;

import com.hcd.deadlock.domain.Entity1;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Entity1Repository extends CrudRepository<Entity1, Long> {
}
