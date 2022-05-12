package se.kry.chat;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.chat.utils.RxLogging;

public final class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  private App() {}

  public static void main(String[] args) {
    System.setProperty(
        "vertx.logger-delegate-factory-class-name",
        "io.vertx.core.logging.SLF4JLogDelegateFactory");
    deploy(Configuration.defaultLocal(args));
  }

  public static void deploy(Configuration configuration) {
    deployVerticle(Vertx.vertx(), configuration).subscribe();
  }

  public static Single<DeployedChatVerticle> deployVerticle(
      Vertx vertx, Configuration configuration) {
    final var verticle = ChatVerticle.fromConfiguration(configuration);
    return vertx
        .rxDeployVerticle(verticle)
        .map(deploymentId -> new DeployedChatVerticle(verticle, deploymentId))
        .to(
            RxLogging.logSingle(
                logger,
                "deploying chat verticle",
                options -> options.includeValue(DeployedChatVerticle::deploymentId)));
  }
}
