package se.kry.chat;

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
  private final String room;
  private final String roomContentsKey;
  private final String roomChannelKey;
  private final String username;
  private final ServerWebSocket webSocket;
  private final RedisConnection redisConnection;
  private final RedisAPI redisAPI;

  public RoomConnection(String room, String username, ServerWebSocket webSocket, RedisConnection redisConnection) {
    this.room = room;
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
        .handler(buffer -> {
          System.out.printf("Received: %s", buffer);
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
            this::writeAllMessages,
            throwable -> logger.error("Fetching messages", throwable)
        )
        .isDisposed();
  }

  private void writeAllMessages(Response response) {
    for(var message : response) {
      webSocket.writeTextMessage(message.toString());
    }
  }

  private void publishMessage(Buffer buffer) {
    final var message = String.format("%s: %s", username, buffer.toString());
    redisAPI.rpush(List.of(roomContentsKey, message.strip()));
    redisAPI.publish(roomChannelKey, message);
  }

  private void subscribeToRoomChannel() {
    redisConnection.handler(message -> {
      if (message.get(0).toString().equals("message")) {
        webSocket.writeTextMessage(message.get(2).toString());
      }
    });
    redisAPI.subscribe(List.of(roomChannelKey));
  }
}
