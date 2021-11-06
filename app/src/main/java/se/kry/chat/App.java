package se.kry.chat;

import io.vertx.rxjava3.core.Vertx;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);
  public static void main(String[] args) {
    final var port = Arrays.stream(args).findFirst().map(Integer::parseInt).orElse(10001);
    Vertx.vertx()
        .rxDeployVerticle(new ChatVerticle(port))
        .subscribe(deploymentId -> logger.info("Deployed verticle: {}", deploymentId))
        .isDisposed();
  }
}
