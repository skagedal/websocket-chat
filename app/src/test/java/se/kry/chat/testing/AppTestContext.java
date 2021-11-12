package se.kry.chat.testing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.rxjava3.ext.web.client.WebClient;

public record AppTestContext(
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
            justification = "Dependency injection")
        WebClient webClient) {}
