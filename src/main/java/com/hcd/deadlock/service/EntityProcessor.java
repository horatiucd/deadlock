package com.hcd.deadlock.service;

import com.hcd.deadlock.domain.Entity1;
import com.hcd.deadlock.domain.Entity2;
import com.hcd.deadlock.repository.Entity1Repository;
import com.hcd.deadlock.repository.Entity2Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EntityProcessor {

    private static final Logger log = LoggerFactory.getLogger(EntityProcessor.class);

    private final Entity1Repository entity1Repo;
    private final Entity2Repository entity2Repo;

    public EntityProcessor(Entity1Repository entity1Repo, Entity2Repository entity2Repo) {
        this.entity1Repo = entity1Repo;
        this.entity2Repo = entity2Repo;
    }

    @Transactional
    public void process1(long entity1Id, long entity2Id) {
        log.info("START Process 1 - entity1Id: {}, entity2Id: {}", entity1Id, entity2Id);

        processEntity1(entity1Id);

        processEntity2(entity2Id);

        log.info("END Process 1");
    }

    @Transactional
    public void process2(long entity1Id, long entity2Id) {
        log.info("START Process 2 - entity1Id: {}, entity2Id: {}", entity1Id, entity2Id);

        processEntity2(entity2Id);

        processEntity1(entity1Id);

        log.info("END Process 2");
    }

    private void processEntity1(long entityId) {
        Entity1 entity1 = entity1Repo.findById(entityId)
                .orElseThrow(() -> new RuntimeException("Entity1 not found"));

        entity1.setText(UUID.randomUUID().toString());
    }

    private void processEntity2(long entityId) {
        Entity2 entity2 = entity2Repo.findById(entityId)
                .orElseThrow(() -> new RuntimeException("Entity2 not found"));

        entity2.setText(UUID.randomUUID().toString());
    }
}
