package com.pkb;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Api {
    private final Router api;

    public Api(Vertx vertx) {
        api = Router.router(vertx);
        api.route()
                .produces("application/json")
                .handler(BodyHandler.create());
    }

    public Api register(ApiService service) {
        service.register(api);
        return this;
    }

    public Router router() {
        return api;
    }
}
