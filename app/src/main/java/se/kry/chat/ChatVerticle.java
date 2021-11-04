package se.kry.chat;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.Response;

public class ChatVerticle extends AbstractVerticle {
  private final Integer port;
  private Redis redis;
  private RoomService roomService;

  public ChatVerticle(Integer port) {
    this.port = port;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    final var options = new HttpServerOptions()
        .setLogActivity(true);

    redis = Redis.createClient(vertx, new RedisOptions().setConnectionString(
        "redis://127.0.0.1:10000"
    ));
    roomService = new RoomService(redis);

    final var router = Router.router(vertx);

    router.get("/_health").respond(this::health);
    router.get("/chat/:room").respond(this::chat);

    vertx
        .createHttpServer(options)
        .requestHandler(router)
        .listen(port)
        .onComplete(result -> {
          System.out.printf("Listening to port %d\n", port);
          startPromise.complete();
        });
  }

  private Future<Void> chat(RoutingContext routingContext) {
    final var room = routingContext.pathParam("room");
    final var name = routingContext.queryParam("username").get(0);
    return routingContext.request().toWebSocket().onComplete(webSocket ->
        roomService.enterRoom(webSocket.result(), room, name)
    ).mapEmpty();
  }

  private Future<Health> health(RoutingContext routingContext) {
    return RedisAPI.api(redis)
        .incr("count")
        .map(Response::toInteger)
        .map(count -> new Health("healthy", count));
  }

  public static record Health(String message, long count) { }
}

