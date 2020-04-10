## To start
Run ``
## Update status

Possible statuses:
- open
- pending
- resolved
- closed
- waiting_on_customer
- waiting_on_3rd_party
- waiting_on_developer
- waiting_on_senior_stuff_input
- waiting_on_jira
- cie_deletion

```
POST http://localhost:8080/api/status/34677/waiting_on_customer
Accept: application/json
```

## Send message

```
PUT http://localhost:8080/api/message/34677
Accept: application/json
Content-Type: application/json

{
  "message": "Hello from integration"
}
```

## Links
- [To read](https://blog.teemo.co/vertx-in-production-d5ca9e89d7c6)
- [Interceptor test](https://github.com/vert-x3/vertx-web/blob/master/vertx-web-client/src/test/java/io/vertx/ext/web/client/InterceptorTest.java)
- [config](https://vertx.io/docs/vertx-config/java/)
- [vertx rest](https://vertx.io/blog/some-rest-with-vert-x/)
- [vextx web](https://vertx.io/docs/vertx-web/java/) and [examples](https://github.com/vert-x3/vertx-web/blob/master/vertx-web/src/main/java/examples/WebExamples.java)
