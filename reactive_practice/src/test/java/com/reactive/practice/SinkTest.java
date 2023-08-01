package com.reactive.practice;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.scheduler.Schedulers;

class SinkTest {
    Logger log = LoggerFactory.getLogger(SinkTest.class);

    @Test
    void create_sink() throws InterruptedException {
        int tasks = 6;

        Flux.create((FluxSink<String> sink) -> {
                    IntStream.range(1, tasks)
                            .forEach(n -> sink.next(doTasks(n)));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(n -> log.info("# created() : {}", n))
                .publishOn(Schedulers.parallel())
                .map(result -> result + "success!")
                .doOnNext(n -> log.info("# map(): {}", n))
                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(1000);
    }

    @Test
    void create_sink_thread_safe() throws InterruptedException {
        int tasks = 6;
        Sinks.Many<String> unicastSink = Sinks.many().unicast().onBackpressureBuffer();

        Flux<String> fluxView = unicastSink.asFlux();
        IntStream.range(1, tasks)
                .forEach(n -> {
                    new Thread(() -> {
                        log.info("# emitted : {}", n);
                        unicastSink.emitNext(doTasks(n), EmitFailureHandler.FAIL_FAST);

                    }).start();
                });


        fluxView
                .publishOn(Schedulers.parallel())
                .map(result -> result + " success!")
                .doOnNext(n -> log.info("# map() : {}", n))
                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(2000L);
    }


    private String doTasks(int taskNumber) {
        // now tasking
        // complte to task
        return "task " + taskNumber + " result";
    }
}
