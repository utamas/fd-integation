package com.pkb;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;

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

    private final FdService fdService;

    public StatusChangeService(FdService fdService) {
        this.fdService = fdService;
    }

    @Override
    public Router register(Router api) {
        api.post(format("/status/:%s/:%s", TICKET_ID, STATUS))
                .handler(VALIDATION_HANDLER)
                .handler(new StatusChangeHandler());
        return api;
    }

    public class StatusChangeHandler implements Handler<RoutingContext> {
        @Override
        public void handle(RoutingContext context) {
            RequestParameters params = context.get("parsedParameters");
            int ticketId = params.pathParameter(TICKET_ID).getInteger();
            Status status = Status.valueOf(params.pathParameter(STATUS).getString());

            fdService.changeStatus(ticketId, status)
                    .onComplete($ -> {
                        int updatedStatusCode = $.result().getInteger("status");

                        context.response()
                                .end(new JsonObject()
                                        .put("status", "ok")
                                        .put("current_fd_ticket_status", Status.fromCode(updatedStatusCode))
                                        .toString());
                    })
                    .onFailure(cause -> {
                        LOGGER.error(cause);
                        context.response().setStatusCode(500)
                                .end(new JsonObject().put("status", "failed").toString());
                    });
        }
    }
}

