package com.pkb;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.util.Base64;

import static java.lang.String.format;

public class FdService {
    private final WebClient httpClient;
    private final String token;
    private final String baseUrl;

    public FdService(Vertx vertx, JsonObject config) {
        httpClient = WebClient.create(vertx);
        token = Base64.getEncoder().encodeToString(format("%s:X", config.getString("token")).getBytes());
        baseUrl = config.getString("baseUrl");
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
                        } else {
                            promise.fail(fdResponse.cause());
                        }
                    } catch (Exception cause) {
                        promise.fail(cause);
                    }
                });
        return promise.future();
    }
}
