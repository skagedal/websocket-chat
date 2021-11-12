package se.kry.chat.testing;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class RedisContainer extends GenericContainer<RedisContainer> {
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
    return String.format("redis://%s:%d", getHost(), getFirstMappedPort());
  }

  private static RedisContainer createContainer() {
    return new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);
  }
}
