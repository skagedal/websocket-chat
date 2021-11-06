package se.kry.chat.testing;

import io.vertx.junit5.VertxExtension;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.RedisAPI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class RedisExtension implements ParameterResolver, BeforeEachCallback {
  private final Vertx vertx = Vertx.vertx();

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    final Class<?> type = parameterContext.getParameter().getType();
    return type.isAssignableFrom(Redis.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    final Class<?> type = parameterContext.getParameter().getType();
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

  public static class RedisContainer extends GenericContainer<RedisContainer> {
    private static RedisContainer containerInstance;

    RedisContainer(DockerImageName name) {
      super(name);
    }

    public static synchronized RedisContainer sharedContainer() {
      if (containerInstance == null) {
        containerInstance = createContainer();
        containerInstance.start();
      }
      return containerInstance;
    }

    public String connectionString() {
      return String.format(
          "redis://%s:%d", getHost(), getFirstMappedPort()
      );
    }

    private static RedisContainer createContainer() {
      return new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine"))
          .withExposedPorts(6379);
    }
  }
}
