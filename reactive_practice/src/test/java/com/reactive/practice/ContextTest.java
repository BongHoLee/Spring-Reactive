package com.reactive.practice;

import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import lombok.Data;
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
                .contextWrite(context -> context.putAll(
                        Context.of(key2, "Steve", key3, "Jobs").readOnly())) // Context#putAll 메서드와 Context.of 정적 메서드
                .contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(1000);
    }

    @Test
    void ContextView_API_사용_샘플() throws InterruptedException {
        final String key1 = "company";
        final String key2 = "firstName";
        final String key3 = "lastName";

        Mono.deferContextual(contextView -> Mono.just(contextView.get(key1) + ", " +
                        contextView.getOrEmpty(key2).orElse("no firstName") + " " +
                        contextView.getOrDefault("key3", "no lastName")))
                .publishOn(Schedulers.parallel())
                .contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(1000);
    }

    @Test
    void subsribe에대해_각각_Context생성_샘플() throws InterruptedException {
        final String key1 = "company";

        Mono<String> mono = Mono.deferContextual(contextView -> Mono.just("Company: " + contextView.get(key1)))
                .publishOn(Schedulers.parallel());

        // 첫 번째 subscribe
        mono.contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# subscribe1 onNext: {}", data));

        // 두 번째 subscribe
        mono.contextWrite(context -> context.put(key1, "Microsoft"))
                .subscribe(data -> log.info("# subscribe2 onNext: {}", data));

        Thread.sleep(1000);
    }

    @Test
    void Context_upstream_전파_샘플() throws InterruptedException {
        final String key1 = "company";
        final String key2 = "name";

        Mono
                .deferContextual(contextView -> Mono.just(contextView.get(key1)))
                .publishOn(Schedulers.parallel())
                .contextWrite(context -> context.put(key2, "Bill"))
                .transformDeferredContextual(
                        (mono, contextView) -> mono.map(data -> data + ", " + contextView.getOrDefault(key2, "Steve")))
                .contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNExt: {}", data));

        Thread.sleep(1000);
    }

    @Test
    void InnerSequence_에서는_OuterSequence_Context에_접근이_가능함_샘플() throws InterruptedException {
        final String key1 = "company";

        Mono.just("Steve")
                .flatMap(name ->
                        Mono.deferContextual(ctx ->
                                Mono
                                        .just(ctx.get(key1) + ", " + name)
                                        .transformDeferredContextual((mono, innerCtx) -> mono.map(data -> data + ", " + innerCtx.get("role")))
                                        .contextWrite(context -> context.put("role", "CEO"))
                        )
                )
                .publishOn(Schedulers.parallel())
                .contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {} ", data));

        Thread.sleep(1000);
    }

    @Test
    void OuterSequence_에서는_InnerSequence에_접근이_불가능함_샘플() throws InterruptedException {
        final String key = "company";

        try {
            Mono.just("Steve")
                    .transformDeferredContextual((mono, outerCtx) -> mono.map(
                            data -> data + ", " + outerCtx.get("role")))   // EXCEPTION!! : Outer Sequence의 Context에서는 Inner Sequence의 Context에 접근이 불가능하다(전파가 안된다.)
                    .flatMap(name ->
                            Mono.deferContextual(ctx ->
                                    Mono
                                            .just(ctx.get(key) + ", " + name)
                                            .transformDeferredContextual((mono, innerCtx) -> mono.map(
                                                    data -> data + ", " + innerCtx.get("role")))
                                            .contextWrite(context -> context.put("role", "CEO"))
                            )
                    )
                    .publishOn(Schedulers.parallel())
                    .contextWrite(context -> context.put(key, "Apple"))
                    .subscribe(data -> log.info("# onNExt : {}", data));
        } catch (NoSuchElementException e) {
            log.error("exception ", e);
        }

        Thread.sleep(1000);
    }

    static final String HEADER_AUTH_TOKEN = "authToken";
    @Test
    void Context전파_샘플2_MonoZip에서도_전파됨() throws InterruptedException {
        Mono
                .zip(
                        Mono.just(new Book("abcd-123", "Reactors book", "leebongho")),
                        Mono.deferContextual(ctx -> Mono.just(ctx.get(HEADER_AUTH_TOKEN)))
                )
                .flatMap(tuple -> {
                    Book book = tuple.getT1();
                    String header_token_from_context = (String) tuple.getT2();
                    String response = "POST the book (" + book.getBookName() + ", " + book.getAuthor() +") with token : " + header_token_from_context;
                    return Mono.just(response);
                })
                .contextWrite(Context.of(HEADER_AUTH_TOKEN, "header_token_1234"))
                .subscribe(data -> log.info("# onNext: {} ", data));

        Thread.sleep(1000);
    }
}

@AllArgsConstructor
@Data
class Book {
    private String isbn;
    private String bookName;
    private String author;
}
