package se.kry.chat;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.kry.chat.testing.AppExtension;
import se.kry.chat.testing.AppTestContext;

@ExtendWith(AppExtension.class)
class ChatVerticleTest {

  @Test
  void health_endpoint(AppTestContext context) {
    final var response = context.webClient().get("/_health").rxSend().blockingGet();
    assertEquals(200, response.statusCode(), "status code was not 200");
  }
}
