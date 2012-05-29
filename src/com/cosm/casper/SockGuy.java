package com.cosm.casper;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.core.sockjs.SockJSSocket;
import org.vertx.java.deploy.Verticle;
import org.vertx.java.core.json.*;

public class SockGuy extends Verticle {

  public void start() {
    HttpServer server = vertx.createHttpServer();

    server.requestHandler(new Handler<HttpServerRequest>() {
      public void handle(HttpServerRequest req) {
        System.out.println("req: "+req.path);
        if (req.path.equals("/")) req.response.sendFile("index.html");
        if (req.path.equals("/vertxbus.js")) req.response.sendFile("vertxbus.js");
      }
    });

    JsonArray whitelist = new JsonArray();
    whitelist.add(new JsonObject());
    SockJSServer sockServer = vertx.createSockJSServer(server);
    sockServer.bridge(new JsonObject().putString("prefix", "/eventbus"), whitelist);

    server.listen(8081);
    System.out.println("8081 started");
  }
}
