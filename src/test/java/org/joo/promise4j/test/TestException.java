package org.joo.promise4j.test;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class TestException {

    @Test
    public void testExceptionOnPipeDone() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Deferred<?, ?> deferred = new CompletableDeferredObject<>();
        deferred.promise().pipeDone(result -> {
            throw new RuntimeException("fail");
        }).fail(ex -> {
            latch.countDown();
        });
        deferred.resolve(null);
        latch.await();
    }
}
