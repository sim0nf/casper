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
  
  private int randomStatus(Random rnd) {
    int[] statuses = {200,200,200,200,200,200,200,200,404,500};
    double i = statuses.length;
    i *= rnd.nextDouble();
    int s = statuses[(int)i];
    System.out.println("status: "+s);
    return s;
  }

  public void start() {

    final Random rnd = new Random();
    

    vertx.setPeriodic(10, new Handler<Long>() {
      public void handle (Long timerID) {
        final JsonObject reqInfo = new JsonObject();
        reqInfo.putNumber("startTime", System.currentTimeMillis());
        reqInfo.putString("uri", "/foo");
        reqInfo.putString("method", "GET");
        reqInfo.putString("uid", "x"+(new Random().nextLong()));
        vertx.eventBus().send("reqs.start", reqInfo);	
        
        long delay = (long)(10 + (20000 * rnd.nextDouble()));
        vertx.setTimer(delay, new Handler<Long>() {
          public void handle (Long timerID) {
            reqInfo.putNumber("elapsedTime", System.currentTimeMillis() - reqInfo.getNumber("startTime").longValue());
            reqInfo.putNumber("status", randomStatus(rnd));
            vertx.eventBus().send("reqs.finish", reqInfo);	
          }
        });
      }
    });
  }
}
