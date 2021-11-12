package se.kry.chat;

import java.util.Arrays;

public record Configuration(int servicePort, String redisConnectionString) {
  public static Configuration defaultLocal(String[] args) { // NOPMD
    final var servicePort = Arrays.stream(args).findFirst().map(Integer::parseInt).orElse(10001);
    return new Configuration(servicePort, "redis://127.0.0.1:10000");
  }

  public static Configuration testing(String redisConnectionString) {
    return new Configuration(0, redisConnectionString);
  }
}
