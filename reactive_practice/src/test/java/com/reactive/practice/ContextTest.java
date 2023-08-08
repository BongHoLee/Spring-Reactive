package com.reactive.practice;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

@Slf4j
class ContextTest {

    @Test
    void Context_샘플() throws InterruptedException {
        Mono.deferContextual(context ->
                        Mono.just("Hello" + " " + context.get("firstName"))
                                .doOnNext(data -> log.info("# just doOnNext : {}", data))
                )
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel())

                .transformDeferredContextual(
                        (mono, contextView) -> mono.map(data -> data + " " + contextView.get("lastName"))
                )
                .contextWrite(context -> context.put("lastName", "Jobs"))
                .contextWrite(context -> context.put("firstName", "Steve"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(1000);
    }

    @Test
    void Context_API_사용_샘플() throws InterruptedException {
        final String key1 = "company";
        final String key2 = "firstName";
        final String key3 = "lastName";

        Mono.deferContextual(contextView ->
                        Mono.just(contextView.get(key1) + ", " + contextView.get(key2) + " " + contextView.get(key3)))
                .publishOn(Schedulers.parallel())
                .contextWrite(context -> context.putAll(Context.of(key2, "Steve", key3, "Jobs").readOnly())) // Context#putAll 메서드와 Context.of 정적 메서드
                .contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(1000);
    }
}
