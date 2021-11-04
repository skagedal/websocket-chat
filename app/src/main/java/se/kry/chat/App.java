package se.kry.chat;

import io.vertx.core.Vertx;
import java.util.Arrays;

public class App {
  public static void main(String[] args) {
    final var port = Arrays.stream(args).findFirst().map(Integer::parseInt).orElse(10001);
    Vertx.vertx().deployVerticle(new ChatVerticle(port)).onComplete(result ->
        System.out.printf("Deployed verticle: %s\n", result.result())
    );
  }
}
