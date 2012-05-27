package com.cosm.casper;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.*;
import org.vertx.java.core.SimpleHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.deploy.Verticle;
import java.util.*;
import org.vertx.java.core.json.*;

public class Reporter extends Verticle {

  public void start()  {
    vertx.eventBus().registerHandler("reqs.report", new Handler<Message<JsonObject>>() {
      public void handle(Message<JsonObject> msg) {
        System.out.println("got a report: "+msg.body);
      }
    });
  }
}
