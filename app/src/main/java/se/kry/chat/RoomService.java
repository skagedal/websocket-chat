package se.kry.chat;

import io.vertx.rxjava3.core.http.ServerWebSocket;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.RedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("ClassCanBeRecord")
public class RoomService {
  private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
  private final Redis redis;

  public RoomService(Redis redis) {
    this.redis = redis;
  }

  void enterRoom(ServerWebSocket webSocket, String room, String username) {
    redis
        .connect()
        .subscribe(
            connection -> startConnection(room, username, webSocket, connection),
            throwable -> logger.error("Connecting to Redis", throwable)
        )
        .isDisposed();
  }

  private void startConnection(String room, String username, ServerWebSocket webSocket, RedisConnection connection) {
    new RoomConnection(room, username, webSocket, connection)
        .start();
  }
}
