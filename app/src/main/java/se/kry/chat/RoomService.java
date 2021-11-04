package se.kry.chat;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class RoomService {
  private final Redis redis;

  public RoomService(Redis redis) {
    this.redis = redis;
  }

  void enterRoom(ServerWebSocket webSocket, String room, String username) {
    webSocket
        .handler(buffer -> {
          System.out.printf("Received: %s", buffer);
          // would be better to use same connection as the receiver
          RedisAPI.api(redis).publish(room, String.format("%s: %s", username, buffer.toString()));
        })
        .closeHandler(__ -> System.out.println("Close-handler"))
        .drainHandler(__ -> System.out.println("Drain-handler"))
        .endHandler(__ -> System.out.println("End-handler"));

    redis.connect()
        .onSuccess(conn -> {
          conn.handler(message -> {
            System.out.printf("Handling Redis message: %s\n", message.toString());
            webSocket.writeTextMessage(message.get(2).toString());
          });
          RedisAPI.api(conn).subscribe(List.of(room));
        });
  }
}
