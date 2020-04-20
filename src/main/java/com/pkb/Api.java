package com.pkb;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Api {
    private final Router api;

    public Api(Vertx vertx, FdService fdService) {
        api = Router.router(vertx);
        api.route()
                .produces("application/json")
                .handler(BodyHandler.create());
        api.get("/services/fd/stats").handler(context -> context.response()
                .end(fdService.stats().toString()));
    }

    public Api register(ApiService service) {
        service.register(api);
        return this;
    }

    public Router router() {
        return api;
    }
}
