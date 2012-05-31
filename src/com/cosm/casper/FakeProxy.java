package com.cosm.casper;

import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.deploy.Verticle;
import org.vertx.java.core.eventbus.*;
import org.vertx.java.core.json.*;
import java.util.Random;

public class FakeProxy extends Verticle {

  public void start() {

    vertx.setPeriodic(10, new Handler<Long>() {
      public void handle (Long timerID) {
        final JsonObject reqInfo = new JsonObject();
        reqInfo.putNumber("startTime", System.currentTimeMillis());
        reqInfo.putString("uri", "/foo");
        reqInfo.putString("method", "GET");
        reqInfo.putString("uid", "x"+(new Random().nextLong()));
        vertx.eventBus().send("reqs.start", reqInfo);	
        
        long delay = 3000;
        vertx.setTimer(delay, new Handler<Long>() {
          public void handle (Long timerID) {
            reqInfo.putNumber("elapsedTime", System.currentTimeMillis() - reqInfo.getNumber("startTime").longValue());
            reqInfo.putNumber("status", 200);
            vertx.eventBus().send("reqs.finish", reqInfo);	
          }
        });
      }
    });
  }
}
