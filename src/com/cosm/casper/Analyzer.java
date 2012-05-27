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

public class Analyzer extends Verticle {

  private final Map<Long, Map<String, JsonObject>> reqs;

  public Analyzer() {
    this.reqs = new HashMap<Long, Map<String, JsonObject>>();
  }

  private long getSecond(long millis) {
    return millis - (millis % 1000);
  }

  public void start()  {
    EventBus eb = vertx.eventBus();

    // remove refs to requests more than 30s old
    vertx.setPeriodic(1000, new Handler<Long>() {
      private long firstSec() {
        long now = System.currentTimeMillis();
        return (now - (now % 1000)) - 30000;
      }

      private long last = firstSec();

      public void handle(Long timerID) {
        long first = firstSec();
        for (long sec = last; sec < first; sec+=1000) {
          reqs.remove(sec);
        }
        last = first;
      }
    }); 

    // count requests in each state, for each second
    vertx.setPeriodic(1000, new Handler<Long>() {
      private long firstSec() {
        long now = System.currentTimeMillis();
        return (now - (now % 1000)) - 30000;
      }

      public void handle(Long timerID) {
        JsonArray data = new JsonArray();
        long first = firstSec(); 
        for (long sec = first; sec < first + (30 * 1000); sec += 1000) {
          int active = 0;
          int done = 0;
          Map<String, JsonObject> secReqs = reqs.get(sec);
          if (secReqs != null) {
            for (JsonObject reqInfo: secReqs.values()) {
              System.out.println(reqInfo);
              if (reqInfo.getField("status") == null) {
                active++;
              } else {
                done++;
              }
            }
          }
          JsonObject secData = new JsonObject();
          secData.putNumber("time", sec);
          secData.putNumber("active", active);
          secData.putNumber("done", done);
          data.addObject(secData);
//          System.out.println(sec+": active:"+active+"  done: "+done);            
        }
        eb.send("reqs.recent.report", data);
      }
    }); 


    eb.registerHandler("reqs.start", new Handler<Message<JsonObject>>() {
      public void handle(Message<JsonObject> msg) {
        JsonObject reqInfo = msg.body;
        long sec = getSecond(reqInfo.getNumber("startTime").longValue());
        Map<String, JsonObject> secReqs = reqs.get(sec);
        if (secReqs == null) {
          secReqs = new HashMap<String, JsonObject>();
          reqs.put(sec, secReqs);
        }
        secReqs.put(reqInfo.getString("uid"), reqInfo);
        System.out.println("request start: "+msg.body);
      }
    });

    eb.registerHandler("reqs.finish", new Handler<Message<JsonObject>>() {
      public void handle(Message<JsonObject> msg) {
        JsonObject reqInfo = msg.body;
        long sec = getSecond(reqInfo.getNumber("startTime").longValue());
        Map<String, JsonObject> secReqs = reqs.get(sec);
        if (secReqs != null) {
          secReqs.put(reqInfo.getString("uid"), reqInfo);
        }
        System.out.println("request finish: "+reqInfo);
      }
    });
  }
}
