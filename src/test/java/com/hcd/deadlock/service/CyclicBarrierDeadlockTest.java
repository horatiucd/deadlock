package com.hcd.deadlock.service;

import com.hcd.deadlock.domain.Entity1;
import com.hcd.deadlock.domain.Entity2;
import com.hcd.deadlock.repository.Entity1Repository;
import com.hcd.deadlock.repository.Entity2Repository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
class CyclicBarrierDeadlockTest {

    @Autowired
    private EntityProcessor entityProcessor;

    @Autowired
    private Entity1Repository entity1Repo;

    @Autowired
    private Entity2Repository entity2Repo;

    private Entity1 entity1;
    private Entity2 entity2;

    @BeforeEach
    void setUp() {
        entity1 = entity1Repo.save(new Entity1(1L));
        entity2 = entity2Repo.save(new Entity2(2L));
    }

    @AfterEach
    void tearDown() {
        entity1Repo.delete(entity1);
        entity2Repo.delete(entity2);
    }

    @Test
    void run()  {
        CyclicBarrier barrier = new CyclicBarrier(3);

        try (ExecutorService exec = Executors.newFixedThreadPool(2)) {
            Future<?> future1 = exec.submit(new ProcessTask(barrier,
                    () -> entityProcessor.process1(entity1.getId(), entity2.getId())));

            Future<?> future2 = exec.submit(new ProcessTask(barrier,
                    () -> entityProcessor.process2(entity1.getId(), entity2.getId())));

            barrier.await();

            future1.get();
            future2.get();

        } catch (ExecutionException | InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }

        entity1 = entity1Repo.findById(entity1.getId())
                .orElseThrow(() -> new RuntimeException("Entity1 not found"));
        Assertions.assertFalse(entity1.getText().isEmpty());

        entity2 = entity2Repo.findById(entity2.getId())
                .orElseThrow(() -> new RuntimeException("Entity2 not found"));
        Assertions.assertFalse(entity2.getText().isEmpty());
    }

    private record ProcessTask(CyclicBarrier barrier, Runnable runnable) implements Runnable {

        private static final Logger log = LoggerFactory.getLogger(ProcessTask.class);

        @Override
        public void run() {
            log.info("START processing on thread {} ...", Thread.currentThread().getName());

            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                log.error("Could not await().", e);
            }

            runnable.run();

            log.info("END processing on thread {} ...", Thread.currentThread().getName());
        }
    }
}
