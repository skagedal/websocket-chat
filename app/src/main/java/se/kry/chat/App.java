package se.kry.chat;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.rxjava3.core.Vertx;
import java.util.concurrent.TimeUnit;
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

    final var vertx = Vertx.vertx(
        new VertxOptions()
            .setMetricsOptions(new DropwizardMetricsOptions()
                .setEnabled(true)
                .setRegistryName("websocket-chat"))
    );
    MetricRegistry registry = SharedMetricRegistries.getOrCreate("websocket-chat");
    ConsoleReporter.forRegistry(registry)
        .filter(MetricFilter.contains("websocket"))
        .build()
        .start(10, TimeUnit.SECONDS);
    deployVerticle(vertx, Configuration.defaultLocal(args)).subscribe();
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
