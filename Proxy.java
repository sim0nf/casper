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

public class Proxy extends Verticle {

  public void start()  {

    final HttpClient client = vertx.createHttpClient().setHost("reddit.com").setPort(80);

    vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
      public void handle(final HttpServerRequest req) {
        final JsonObject reqInfo = new JsonObject();
        reqInfo.putNumber("startTime", System.currentTimeMillis());
        reqInfo.putString("uri", req.uri);
        reqInfo.putString("method", req.method);
        reqInfo.putString("uid", "x"+(new Random().nextLong()));

        final HttpClientRequest cReq = client.request(req.method, req.uri, new Handler<HttpClientResponse>() {
          public void handle(HttpClientResponse cRes) {
            reqInfo.putNumber("elapsedTime", System.currentTimeMillis() - reqInfo.getNumber("startTime").longValue());
            reqInfo.putNumber("status", cRes.statusCode);
            vertx.eventBus().send("reqs.finish", reqInfo);	

            req.response.statusCode = cRes.statusCode;
            req.response.headers().putAll(cRes.headers());
            req.response.setChunked(true);
            cRes.dataHandler(new Handler<Buffer>() {
              public void handle(Buffer data) {
                req.response.write(data);
              }
            });
            cRes.endHandler(new SimpleHandler() {
              public void handle() {
                req.response.end();
              }
            });
          }
        });
        cReq.headers().putAll(req.headers());
        cReq.headers().put("Host", "www.reddit.com");
        cReq.setChunked(req.headers().get("Transfer-Encoding") == "chunked");

        req.dataHandler(new Handler<Buffer>() {
          public void handle(Buffer data) {
            cReq.write(data);
          }
        });

        req.endHandler(new SimpleHandler() {
          public void handle() {
            cReq.end();
            vertx.eventBus().send("reqs.start", reqInfo);	
          }
        });
      }
    }).listen(8080);
  }
}
