package com.pkb;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.schema.JsonSchema;

import static io.vertx.core.logging.LoggerFactory.getLogger;
import static io.vertx.ext.web.api.validation.HTTPRequestValidationHandler.create;
import static io.vertx.ext.web.api.validation.ParameterType.INT;
import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static org.codehaus.jackson.map.SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING;

public class SendMessageService implements ApiService {
    private static final Logger LOGGER = getLogger(lookup().lookupClass());
    private static final String TICKET_ID = "ticketId";

    private static final HTTPRequestValidationHandler VALIDATION_HANDLER;

    static {
        try {
            JsonSchema schema = new ObjectMapper()
                    .configure(WRITE_ENUMS_USING_TO_STRING, true)
                    .generateJsonSchema(Payload.class);
            VALIDATION_HANDLER = create()
                    .addPathParam(TICKET_ID, INT)
                    .addJsonBodySchema(schema.toString());
        } catch (JsonMappingException e) {
            throw new IllegalStateException(e);
        }
    }

    private final FdService fdService;

    public SendMessageService(FdService fdService) {
        this.fdService = fdService;
    }

    @Override
    public Router register(Router api) {
        api.put(format("/message/:%s", TICKET_ID))
                .handler(VALIDATION_HANDLER)
                .handler(new SendMessageHandler());
        return api;
    }

    public static class Payload {
        private String message;

        public Payload() {
        }

        public Payload(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    private class SendMessageHandler implements Handler<RoutingContext> {
        @Override
        public void handle(RoutingContext context) {
            RequestParameters params = context.get("parsedParameters");
            int ticketId = params.pathParameter(TICKET_ID).getInteger();

            Payload payload = Json.decodeValue(context.getBodyAsString(), Payload.class);

            fdService.sendMessage(ticketId, payload.message)
                    .onComplete($ -> {
                        if ($.succeeded()) {
                            context.response()
                                    .end(new JsonObject().put("status", "ok").toString());
                        } else {
                            LOGGER.error($.cause());
                            context.response().setStatusCode(500)
                                    .end(new JsonObject().put("status", "failed").toString());
                        }
                    });
        }
    }
}
