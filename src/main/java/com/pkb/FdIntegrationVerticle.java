package com.pkb;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.Router;

import static io.vertx.core.logging.LoggerFactory.getLogger;
import static java.lang.invoke.MethodHandles.lookup;

public class FdIntegrationVerticle extends AbstractVerticle {
    private static final Logger LOGGER = getLogger(lookup().lookupClass());

    @Override
    public void start(Future<Void> startedResult) throws Exception {
        super.start(startedResult);

        ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setOptional(false)
                .setConfig(new JsonObject().put("path", "/home/utamas/projects/pkb/sandbox/fd-integration/fd.json"));

        ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(fileStore))
                .getConfig(configLoading -> {
                    if (configLoading.succeeded()) {
                        JsonObject config = configLoading.result();

                        Router router = Router.router(vertx);
                        router.route("/api/*").subRouter(api(config));
                        router.get("/healthz").handler(request -> request.response().end("ok"));

                        HttpServer server = vertx.createHttpServer();
                        server.requestHandler(router)
                                .listen(8080, startup -> {
                                    if (startup.succeeded()) {
                                        LOGGER.info("Started");
                                        startedResult.succeeded();
                                    } else {
                                        startup.failed();
                                    }
                                });
                    } else {
                        startedResult.failed();
                    }
                });
    }

    private Router api(JsonObject config) {
        FdService fdService = new FdService(vertx, config);

        return new Api(vertx)
                .register(new SendMessageService(fdService))
                .register(new StatusChangeService(fdService))
                .router();
    }
}
