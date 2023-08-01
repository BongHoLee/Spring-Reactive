package com.reactive.practice;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class SchedulerTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    void test() throws InterruptedException {
        Flux.fromArray(new Integer[] {1, 3, 5, 7})
                .subscribeOn(Schedulers.boundedElastic())                       // 구독 발생 직후 Origin Publisher 동작 처리를 위한 스레드 할당
                .doOnNext(data -> log.info("# doOnNext: {}", data))
                .doOnSubscribe(subscription -> log.info("# doOnSubscribe"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(1000);
    }

    @Test
    void schedulers_immediate_test() throws InterruptedException {
        Flux.just("a", "b", "c")
                .flatMap(s ->
                        Mono.just(s)
                                .publishOn(Schedulers.parallel())
                                .doOnNext(data -> log.info("parallel doOnNext : {}", data))
                )
                .publishOn(Schedulers.immediate())
                .doOnNext(data -> log.info("immedate() doOnNext : {}", data))
                .subscribe();

        Thread.sleep(1000);
    }

    @Test
    void publishOn_test() {
        Flux.fromArray(new Integer[] {1, 3, 5, 7})
                .doOnNext(data -> log.info("# doOnNext : {}", data))
                .doOnSubscribe(subscription -> log.info("# doOnSubscribe "))
                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext : {}", data));
    }

    @Test
    void parallel_test() throws InterruptedException {
        Flux.fromArray(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
                .parallel()
                .runOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {} ", data));

        Thread.sleep(1000);
    }
}
