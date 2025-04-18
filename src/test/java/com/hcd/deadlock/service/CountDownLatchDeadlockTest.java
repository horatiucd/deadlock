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
import org.springframework.test.annotation.Rollback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest
@Rollback(false)
class CountDownLatchDeadlockTest {

    private static final Logger log = LoggerFactory.getLogger(CountDownLatchDeadlockTest.class);

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
        CountDownLatch latch = new CountDownLatch(1);

        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> future1 = exec.submit(new ProcessTask(latch,
                    () -> entityProcessor.process1(entity1.getId(), entity2.getId())));

            Future<?> future2 = exec.submit(new ProcessTask(latch,
                    () -> entityProcessor.process2(entity1.getId(), entity2.getId())));

            latch.countDown();

            future1.get();
            future2.get();

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("All processors completed.");

        entity1 = entity1Repo.findById(entity1.getId())
                .orElseThrow(() -> new RuntimeException("Entity1 not found"));
        Assertions.assertTrue(entity1.getText().startsWith("Set by process"));

        entity2 = entity2Repo.findById(entity2.getId())
                .orElseThrow(() -> new RuntimeException("Entity2 not found"));
        Assertions.assertTrue(entity2.getText().startsWith("Set by process"));
    }

    private record ProcessTask(CountDownLatch latch, Runnable runnable) implements Runnable {

        private static final Logger log = LoggerFactory.getLogger(ProcessTask.class);

        @Override
        public void run() {
            log.info("START processing on thread {} ...", Thread.currentThread().getName());

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            runnable.run();

            log.info("END processing on thread {} ...", Thread.currentThread().getName());
        }
    }
}
