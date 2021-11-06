package se.kry.chat.testing;

import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;

public record AppTestContext(
    WebClient webClient
) {
}
