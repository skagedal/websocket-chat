package se.kry.chat;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import java.util.List;

public class RoomConnection {
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
        .closeHandler(__ -> System.out.println("Close-handler"))
        .drainHandler(__ -> System.out.println("Drain-handler"))
        .endHandler(__ -> System.out.println("End-handler"));
  }

  private void showLatest() {
    redisAPI.lrange(roomContentsKey, "-10", "-1").onSuccess(response -> {
      for(var message : response) {
        webSocket.writeTextMessage(message.toString());
      }
    });
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
