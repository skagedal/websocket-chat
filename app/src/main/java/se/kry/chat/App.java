package se.kry.chat;

import io.vertx.core.Vertx;

public class App {
  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new ChatVerticle()).onComplete(result ->
        System.out.printf("Deployed verticle: %s\n", result.result())
    );
  }
}
