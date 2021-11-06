package se.kry.chat;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.redis.client.RedisOptions;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.RedisAPI;
import io.vertx.rxjava3.redis.client.Response;

public class ChatVerticle extends AbstractVerticle {
  private final Integer port;
  private Redis redis;
  private RoomService roomService;

  public ChatVerticle(Integer port) {
    this.port = port;
  }

  @Override
  public Completable rxStart() {
    redis = Redis.createClient(vertx, new RedisOptions().setConnectionString(
        "redis://127.0.0.1:10000"
    ));
    roomService = new RoomService(redis);

    final var router = Router.router(vertx);
    router.get("/_health").respond(this::health);
    router.get("/chat/:room").respond(this::chat);

    final var options = new HttpServerOptions()
        .setLogActivity(true);

    return vertx
        .createHttpServer(options)
        .requestHandler(router)
        .listen(port)
        .doOnSuccess(result -> {
          System.out.printf("Listening on port %d\n", result.actualPort());
        })
        .ignoreElement();
  }

  private Maybe<Object> chat(RoutingContext routingContext) {
    final var room = routingContext.pathParam("room");
    final var name = routingContext.queryParam("username").get(0);
    return routingContext.request()
        .toWebSocket()
        .doOnSuccess(webSocket ->
            roomService.enterRoom(webSocket, room, name)
        )
        .flatMapMaybe(__ -> Maybe.just(new Object()));
  }

  private Maybe<Health> health(RoutingContext routingContext) {
    return RedisAPI.api(redis)
        .incr("count")
        .map(Response::toInteger)
        .map(count -> new Health("healthy", count));
  }

  public static record Health(String message, long count) {
  }
}

