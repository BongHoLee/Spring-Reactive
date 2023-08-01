package com.reactive.practice.backpressure;

import java.time.Duration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class BackPressureEx2 {
    @SneakyThrows
    public static void main(String[] args) {

        dropStrategy(Flux.interval(Duration.ofMillis(1)))
                .doOnNext(data -> log.info("$ doOnNext : {}", data))
                .publishOn(Schedulers.parallel())
                .subscribe(
                        data -> {
                            try {Thread.sleep(5);} catch (InterruptedException e) {}
                            log.info("# onNext : {}", data);
                        },
                        ex -> log.error("# onError", ex)
                );


        Thread.sleep(10000);
    }

    private static <T> Flux<T> dropStrategy(Flux<T> origin) {
        return origin.onBackpressureDrop(drop -> log.info("# dropped : {}", drop));
    }

    private static <T> Flux<T> ErrorStrategy(Flux<T> origin) {
        return origin.onBackpressureError();
    }
}
