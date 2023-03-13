package se.kry.chat;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.redis.client.ResponseType;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.ServerWebSocket;
import io.vertx.rxjava3.redis.client.RedisAPI;
import io.vertx.rxjava3.redis.client.RedisConnection;
import io.vertx.rxjava3.redis.client.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomConnection {
  private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
  private final String roomContentsKey;
  private final String roomChannelKey;
  private final String username;
  private final ServerWebSocket webSocket;
  private final RedisConnection redisConnection;
  private final RedisAPI redisAPI;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Dependency injection")
  public RoomConnection(
      String room, String username, ServerWebSocket webSocket, RedisConnection redisConnection) {
    this.roomContentsKey = "contents_" + room;
    this.roomChannelKey = "channel_" + room;
    this.username = username;
    this.webSocket = webSocket;
    this.redisConnection = redisConnection;
    this.redisAPI = RedisAPI.api(redisConnection);
  }

  public void start() {
    handleWebSocket();
    showLatest();
    subscribeToRoomChannel();
  }

  private void handleWebSocket() {
    webSocket
        .handler(
            buffer -> {
              logger.info("Received {}", buffer);
              publishMessage(buffer);
            })
        .closeHandler(__ -> logger.info("Close handler called"))
        .drainHandler(__ -> logger.info("Drain handler called"))
        .endHandler(__ -> logger.info("End handler called"));
  }

  private void showLatest() {
    redisAPI
        .lrange(roomContentsKey, "-10", "-1")
        .subscribe(
            this::writeAllMessages, throwable -> logger.error("Fetching messages", throwable))
        .isDisposed();
  }

  private void writeAllMessages(Response response) {
    for (var message : response) {
      webSocket.writeTextMessage(message.toString());
    }
  }

  private void publishMessage(Buffer buffer) {
    final var message = String.format("%s: %s", username, buffer.toString());
    redisAPI
        .rpush(List.of(roomContentsKey, message.strip()))
        .toSingle()
        .flatMap(response -> redisAPI.publish(roomChannelKey, message).toSingle())
        .subscribe(
            response -> logger.info("Publishing message: success, response {}", response),
            error -> logger.error("Publishing message: error", error))
        .isDisposed();
  }

  private void subscribeToRoomChannel() {
    redisConnection.handler(this::handleRedisMessage);
    redisAPI
        .subscribe(List.of(roomChannelKey))
        .subscribe(
            response -> logger.info("Success in subscribing: {}", response),
            error -> logger.info("Error in subscribing", error),
            () -> logger.info("Completed subscribing to Redis")
        )
        .isDisposed();
  }

  private void handleRedisMessage(Response message) {
    switch (message.type()) {
      case SIMPLE -> logger.warn("Simple message from Redis: {}", message);
      case ERROR -> logger.error("Error from Redis: {}", message);
      case BOOLEAN -> logger.warn("Boolean from Redis: {}", message);
      case NUMBER -> logger.warn("Number from Redis: {}", message);
      case BULK -> logger.warn("Bulk message from Redis: {}", message);
      case PUSH -> {
        webSocket
            .writeTextMessage(message.get(2).toString())
            .subscribe(
                () -> logger.info("Writing message to websocket: success"),
                error -> logger.error("Writing message to websocket: error", error)
            )
            .isDisposed();
        logger.info("Push from Redis: {}", message);
      }
      case ATTRIBUTE -> logger.warn("Attribute from Redis: {}", message);
      case MULTI -> {
        if (message.size() == 3 && message.get(0).toString().equals("message")) {
          webSocket
              .writeTextMessage(message.get(2).toString())
              .subscribe(
                  () -> logger.info("Writing message to websocket: success"),
                  error -> logger.error("Writing message to websocket: error", error))
              .isDisposed();
        } else {
          logger.warn("Unknown Multi from Redis: {}", message);
        }
      }
    }
  }
}
