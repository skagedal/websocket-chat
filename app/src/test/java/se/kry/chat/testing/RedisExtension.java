package se.kry.chat.testing;

import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.RedisAPI;
import java.util.List;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class RedisExtension implements ParameterResolver, BeforeEachCallback {
  private final Vertx vertx = Vertx.vertx();

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final var type = parameterContext.getParameter().getType();
    return type.isAssignableFrom(Redis.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final var type = parameterContext.getParameter().getType();
    if (type.isAssignableFrom(Redis.class)) {
      return createClient();
    }
    return null;
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    final var api = RedisAPI.api(createClient());
    api.flushall(List.of());
  }

  private Redis createClient() {
    final var connectionString = RedisContainer.sharedContainer().connectionString();
    return Redis.createClient(vertx, connectionString);
  }

}
