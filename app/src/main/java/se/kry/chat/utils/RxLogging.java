package se.kry.chat.utils;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleConverter;
import java.util.function.Function;
import org.slf4j.Logger;

public final class RxLogging {
  private RxLogging() {}

  public static <T> SingleConverter<T, Single<T>> logSingle(Logger logger, String operation) {
    return logSingle(logger, operation, Function.identity());
  }

  public static <T> SingleConverter<T, Single<T>> logSingle(
      Logger logger,
      String operation,
      Function<SingleLogOptions<T>, SingleLogOptions<T>> optionModifiers) {
    final var options = optionModifiers.apply(new SingleLogOptions<>());
    return upstream ->
        upstream
            .doOnSubscribe(__ -> logger.info("Subscribe: " + operation))
            .doOnError(throwable -> logger.error("Error: " + operation))
            .doOnSuccess(
                value -> {
                  if (options.valueConverter != null) {
                    logger.info("Success: {} - {}", operation, options.valueConverter.apply(value));
                  } else {
                    logger.info("Success: {}", operation);
                  }
                });
  }

  public static class SingleLogOptions<T> {
    private Function<T, Object> valueConverter = null;

    public SingleLogOptions<T> includeValue() {
      this.valueConverter = t -> t;
      return this;
    }

    public SingleLogOptions<T> includeValue(Function<T, Object> valueConverter) {
      this.valueConverter = valueConverter;
      return this;
    }
  }
}
