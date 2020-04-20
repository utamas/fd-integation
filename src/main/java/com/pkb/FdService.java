package com.pkb;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.ext.web.client.WebClient.create;
import static java.lang.String.format;

public class FdService {
    private final AtomicInteger success = new AtomicInteger(0);
    private final AtomicInteger failure = new AtomicInteger(0);

    private final WebClient httpClient;
    private final String token;
    private final String baseUrl;

    public FdService(Vertx vertx, JsonObject config) {
        httpClient = create(vertx, new WebClientOptions());
        token = Base64.getEncoder().encodeToString(format("%s:X", config.getString("token")).getBytes());
        baseUrl = config.getString("baseUrl");
    }

    public JsonObject stats() {
        return new JsonObject()
                .put("success", success.longValue())
                .put("failure", failure.longValue());
    }

    public Future<JsonObject> sendMessage(int ticketId, String message) {
        JsonObject body = new JsonObject().put("body", message);
        return makeCall(httpClient.postAbs(format("%s/api/v2/tickets/%s/reply", baseUrl, ticketId)), body);
    }

    public Future<JsonObject> changeStatus(int ticketId, Status status) {
        JsonObject payload = new JsonObject().put("status", status.code);
        return makeCall(httpClient.putAbs(format("%s/api/v2/tickets/%s", baseUrl, ticketId)), payload);
    }

    private Future<JsonObject> makeCall(HttpRequest<Buffer> req, JsonObject payload) {
        Promise<JsonObject> promise = Promise.promise();
        req
                .putHeader("Authorization", token)
                .putHeader("Content-Type", "application/json")
                .sendJsonObject(payload, fdResponse -> {
                    try {
                        if (fdResponse.succeeded()) {
                            JsonObject result = fdResponse.result().bodyAsJsonObject();
                            promise.complete(result);
                            success.incrementAndGet();
                        } else {
                            promise.fail(fdResponse.cause());
                            failure.incrementAndGet();
                        }
                    } catch (Exception cause) {
                        promise.fail(cause);
                        failure.incrementAndGet();
                    }
                });
        return promise.future();
    }
}
