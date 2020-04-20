package com.pkb;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.core.logging.LoggerFactory.getLogger;
import static java.lang.invoke.MethodHandles.lookup;

public class InvocationCountingHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = getLogger(lookup().lookupClass());
    private static final AtomicInteger INVOCATION_COUNT = new AtomicInteger(0);

    private final int occurrence;
    private final String message;

    public InvocationCountingHandler(int occurrence, String message) {

        this.occurrence = occurrence;
        this.message = message;
    }

    @Override
    public void handle(RoutingContext event) {
        try {
            event.next();
        } finally {
            int count = INVOCATION_COUNT.incrementAndGet();

            if (count % occurrence == 0) {
                LOGGER.info("Invocation count " + message + ": " + count);
            }
        }
    }
}
