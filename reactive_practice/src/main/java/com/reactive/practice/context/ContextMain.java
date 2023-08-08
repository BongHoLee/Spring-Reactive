package com.reactive.practice.context;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class ContextMain {

    public static void main(String[] args) throws InterruptedException {
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
}
