package se.kry.chat;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.RedisAPI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.kry.chat.testing.RedisExtension;

@ExtendWith(RedisExtension.class)
class ChatVerticleTest {
  @Test
  void testRedis(Redis redis) {
    final var api = RedisAPI.api(redis);

    final var beforeSetGetResponse = api.rxGet("foo").blockingGet();
    assertNull(beforeSetGetResponse, "The value should not be set before we set it");

    final var setResponse = api.rxSet(List.of("foo", "bar")).toSingle().blockingGet().toString();
    assertEquals("OK", setResponse, "Setting message should respond OK");

    final var getResponse = api.rxGet("foo").toSingle().blockingGet().toString();
    assertEquals("bar", getResponse, "After we have set the value, we should be able to get i");
  }
}