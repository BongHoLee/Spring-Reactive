package com.reactive.practice;

import java.lang.Thread.UncaughtExceptionHandler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


class HelloReactorTest {
    Logger log = LoggerFactory.getLogger(HelloReactor.class);
    @Test
    void test() {
        Flux.just(1, 2, 3, 4)
                .flatMap(each -> {
                    subscribeProcess(each);
                    return publisherProcess(each);
                })
                .doOnNext(each -> log.info("flatmap next : " + each))
                .subscribe(
                        System.out::println,
                        ex -> {
                            log.error("ex occured!", ex);

                        }
                );
    }

    void subscribeProcess(int data) {
        Mono.just(data)
                .map(each -> each/0)  // 오류가 발생하더라도 Inner Stream의 onError 이벤트는 Outer Publisher에 전파되지 않는다(생명주기가 독립적)
                .subscribe();
    }

    Mono<Integer> publisherProcess(int data) {
        return Mono.just(data + 1);
    }
}