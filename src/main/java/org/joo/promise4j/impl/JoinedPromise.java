package org.joo.promise4j.impl;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class JoinedPromise<D, F extends Throwable> extends CompletableDeferredObject<JoinedResults<D>, F> {

    private final InternalJoinedResults<D> results;

    private final AtomicInteger counter = new AtomicInteger(0);

    @SafeVarargs
    public JoinedPromise(Promise<D, F>... promises) {
        final int total = promises.length;
        this.results = new InternalJoinedResults<>(total);
        if (total == 0) {
            this.resolve(results.toJoinedResults());
            return;
        }

        int count = 0;
        for (Promise<D, F> promise : promises) {
            final int index = count++;
            promise.done(response -> {
                results.set(index, response);

                if (counter.incrementAndGet() == total)
                    resolve(results.toJoinedResults());
            }).fail(this::reject);
        }
    }

    @SafeVarargs
    public static final <D, F extends Throwable> JoinedPromise<D, F> of(Promise<D, F>... promises) {
        return new JoinedPromise<>(promises);
    }

    @SuppressWarnings("unchecked")
    public static final <D, F extends Throwable> JoinedPromise<D, F> of(Deferred<D, F>... deferreds) {
        Promise<D, F>[] promises = Arrays.stream(deferreds).map(deferred -> deferred.promise())
                                         .toArray(size -> new Promise[size]);
        return new JoinedPromise<>(promises);
    }

    @SuppressWarnings("unchecked")
    public static final <D, F extends Throwable> JoinedPromise<D, F> of(Collection<Promise<D, F>> promises) {
        return new JoinedPromise<>(promises.toArray(new Promise[0]));
    }

    @SafeVarargs
    public static final <D, F extends Throwable> JoinedPromise<D, F> from(Promise<D, F>... promises) {
        return of(promises);
    }

    @SuppressWarnings("unchecked")
    public static final <D, F extends Throwable> JoinedPromise<D, F> from(Deferred<D, F>... deferreds) {
        return of(deferreds);
    }

    public static final <D, F extends Throwable> JoinedPromise<D, F> from(Collection<Promise<D, F>> promises) {
        return of(promises);
    }
}

class InternalJoinedResults<D> {

    private final List<D> results;

    public InternalJoinedResults(int size) {
        results = new CopyOnWriteArrayList<>(Collections.nCopies(size, null));
    }

    public void set(int idx, D result) {
        results.set(idx, result);
    }

    public JoinedResults<D> toJoinedResults() {
        return new JoinedResults<>(results);
    }
}
