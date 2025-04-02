package com.hcd.deadlock.repository;

import com.hcd.deadlock.domain.Entity2;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Entity2Repository extends CrudRepository<Entity2, Long> {
}
