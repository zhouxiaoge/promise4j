package org.joo.promise4j.impl;

import org.joo.promise4j.*;
import org.joo.promise4j.util.FutureCanceller;
import org.joo.promise4j.util.TimeoutScheduler;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class CompletableDeferredObject<D, F extends Throwable> extends AbstractPromise<D, F> implements Deferred<D, F> {

    private CompletableFuture<D> future;

    public CompletableDeferredObject() {
        this.future = new CompletableFuture<>();
    }

    public CompletableDeferredObject(final CompletableFuture<D> future) {
        this.future = future;
    }

    @Override
    public Promise<D, F> done(final DoneCallback<D> callback) {
        future.thenAccept(response -> complete(callback, response));
        return this;
    }

    @Override
    public Promise<D, F> fail(final FailCallback<F> callback) {
        future.exceptionally(ex -> complete(callback, ex));
        return this;
    }

    @Override
    public Promise<D, F> always(AlwaysCallback<D, F> callback) {
        future.whenComplete((result, cause) -> complete(callback, result, cause));
        return this;
    }

    @Override
    public Deferred<D, F> resolve(final D result) {
        future.complete(result);
        return this;
    }

    @Override
    public Deferred<D, F> reject(final F failedCause) {
        future.completeExceptionally(failedCause);
        return this;
    }

    @Override
    public Promise<D, F> promise() {
        return this;
    }

    @Override
    public D get() throws PromiseException, InterruptedException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            throw new PromiseException(e.getCause());
        }
    }

    @Override
    public D get(long timeout, TimeUnit unit) throws PromiseException, TimeoutException, InterruptedException {
        try {
            return future.get(timeout, unit);
        } catch (ExecutionException e) {
            throw new PromiseException(e.getCause());
        }
    }

    @Override
    public Deferred<D, F> withTimeout(long timeout, TimeUnit unit, Supplier<F> exceptionSupplier) {
        ScheduledFuture<?> timeoutFuture = TimeoutScheduler.delay(() -> {
            if (!future.isDone())
                future.completeExceptionally(exceptionSupplier.get());
        }, timeout, unit);
        future.whenComplete(new FutureCanceller(timeoutFuture));
        return this;
    }

    @Override
    public Promise<D, F> timeoutAfter(long duration, TimeUnit unit, Supplier<F> exceptionSupplier) {
        withTimeout(duration, unit, exceptionSupplier);
        return this;
    }

    @Override
    public DeferredStatus getStatus() {
        if (!future.isDone())
            return null;
        return future.isCompletedExceptionally() ? DeferredStatus.REJECTED : DeferredStatus.RESOLVED;
    }
}