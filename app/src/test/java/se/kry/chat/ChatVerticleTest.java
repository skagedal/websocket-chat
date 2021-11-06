package se.kry.chat;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.RedisAPI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.kry.chat.testing.AppExtension;
import se.kry.chat.testing.AppTestContext;
import se.kry.chat.testing.RedisExtension;

@ExtendWith(AppExtension.class)
class ChatVerticleTest {

  @Test
  void health_endpoint(AppTestContext context) {
    final var response = context.webClient().get("/_health").rxSend().blockingGet();
    assertEquals(200, response.statusCode(), "status code was not 200");
  }

}