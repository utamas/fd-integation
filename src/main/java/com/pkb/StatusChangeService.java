package com.pkb;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;

import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.core.logging.LoggerFactory.getLogger;
import static io.vertx.ext.web.api.validation.HTTPRequestValidationHandler.create;
import static io.vertx.ext.web.api.validation.ParameterType.GENERIC_STRING;
import static io.vertx.ext.web.api.validation.ParameterType.INT;
import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;

public class StatusChangeService implements ApiService {
    private static final Logger LOGGER = getLogger(lookup().lookupClass());

    private static final String TICKET_ID = "ticketId";
    private static final String STATUS = "status";

    private static final HTTPRequestValidationHandler VALIDATION_HANDLER = create()
            .addPathParam(TICKET_ID, INT)
            // TODO: use io.vertx.ext.web.api.validation.impl.EnumTypeValidator
            .addPathParam(STATUS, GENERIC_STRING);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
//    private static final Handler<RoutingContext> COUNTER = new InvocationCountingHandler(10_000, "Status");

    private final FdService fdService;

    public StatusChangeService(FdService fdService) {
        LOGGER.info("Deploying: " + lookup().lookupClass());
        this.fdService = fdService;
    }

    @Override
    public Router register(Router api) {
        String path = format("/status/:%s/:%s", TICKET_ID, STATUS);
        LOGGER.info("Listening on: " + path);
        api.put(path)
                .handler(VALIDATION_HANDLER)
//                .handler(COUNTER)
                .handler(new StatusChangeHandler());
        api.get("/services/fd/ticket-status").handler($ -> $.response().end(new JsonObject()
                .put("counter", COUNTER.longValue())
                .toString()));
        return api;
    }

    public class StatusChangeHandler implements Handler<RoutingContext> {
        private final Logger LOGGER = getLogger(lookup().lookupClass());

        @Override
        public void handle(RoutingContext context) {
            RequestParameters params = context.get("parsedParameters");
            int ticketId = params.pathParameter(TICKET_ID).getInteger();
            Status status = Status.valueOf(params.pathParameter(STATUS).getString());
            fdService.changeStatus(ticketId, status)
                    .onSuccess($ -> {
                        try {
                            int updatedStatusCode = $.getInteger("status");
                            context.response()
                                    .end(new JsonObject()
                                            .put("status", "ok")
                                            .put("current_fd_ticket_status", Status.fromCode(updatedStatusCode))
                                            .toString());
                            COUNTER.incrementAndGet();
                        } catch (Exception cause) {
                            handleError(context, cause);
                        }
                    })
                    .onFailure(cause -> handleError(context, cause));
        }

        private void handleError(RoutingContext context, Throwable cause) {
            LOGGER.error(cause);
            context.response().setStatusCode(500)
                    .end(new JsonObject().put("status", "failed").toString());
        }
    }
}

