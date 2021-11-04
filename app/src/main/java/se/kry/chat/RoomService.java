package se.kry.chat;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisConnection;

@SuppressWarnings("ClassCanBeRecord")
public class RoomService {
  private final Redis redis;

  public RoomService(Redis redis) {
    this.redis = redis;
  }

  void enterRoom(ServerWebSocket webSocket, String room, String username) {
    redis.connect().onSuccess(connection ->
        startConnection(room, username, webSocket, connection));
  }

  private void startConnection(String room, String username, ServerWebSocket webSocket, RedisConnection connection) {
    new RoomConnection(room, username, webSocket, connection)
        .start();
  }
}
