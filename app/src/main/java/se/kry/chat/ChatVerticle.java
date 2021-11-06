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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(ChatVerticle.class);
  private final int servicePort;
  private final String redisConnectionString;
  private Redis redis;
  private RoomService roomService;
  private int actualServicePort;

  public static ChatVerticle fromConfiguration(Configuration configuration) {
    return new ChatVerticle(
        configuration.servicePort(),
        configuration.redisConnectionString()
    );
  }
  public ChatVerticle(int servicePort, String redisConnectionString) {
    this.servicePort = servicePort;
    this.redisConnectionString = redisConnectionString;
  }

  @Override
  public Completable rxStart() {
    redis = Redis.createClient(
        vertx,
        new RedisOptions().setConnectionString(redisConnectionString)
    );
    roomService = new RoomService(redis);

    final var router = Router.router(vertx);
    router.get("/_health").respond(this::health);
    router.get("/chat/:room").respond(this::chat);

    final var options = new HttpServerOptions()
        .setLogActivity(true);

    return vertx
        .createHttpServer(options)
        .requestHandler(router)
        .listen(servicePort)
        .doOnSuccess(result -> {
          actualServicePort = result.actualPort();
          logger.info("Listening on port {}", result.actualPort());
        })
        .ignoreElement();
  }

  public int actualServicePort() {
    return actualServicePort;
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

  private Maybe<Health> health(RoutingContext ignoredRoutingContext) {
    return RedisAPI.api(redis)
        .incr("count")
        .map(Response::toInteger)
        .map(count -> new Health("healthy", count));
  }

  public static record Health(String message, long count) {
  }
}

