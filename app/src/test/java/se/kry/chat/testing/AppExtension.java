package se.kry.chat.testing;

import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import se.kry.chat.App;
import se.kry.chat.Configuration;

public class AppExtension implements ParameterResolver, BeforeEachCallback {
  private static final Vertx vertx = Vertx.vertx();
  private static AppTestContext testContextInstance;

  @Override
  public void beforeEach(ExtensionContext context) {

  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final var type = parameterContext.getParameter().getType();
    return type.isAssignableFrom(AppTestContext.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final var type = parameterContext.getParameter().getType();
    if (type.isAssignableFrom(AppTestContext.class)) {
      return sharedTestContext();
    }
    return null;
  }

  private static synchronized AppTestContext sharedTestContext() {
    if (testContextInstance == null) {
      final var configuration = Configuration.testing(RedisContainer.sharedContainer().connectionString());
      final var verticle = App.deployVerticle(vertx, configuration).blockingGet().verticle();
      testContextInstance = new AppTestContext(
          createWebClient(verticle.actualServicePort())
      );
    }
    return testContextInstance;
  }

  private static WebClient createWebClient(int port) {
    return WebClient.create(
        vertx,
        new WebClientOptions()
            .setLogActivity(true)
            .setDefaultPort(port)
    );
  }
}
