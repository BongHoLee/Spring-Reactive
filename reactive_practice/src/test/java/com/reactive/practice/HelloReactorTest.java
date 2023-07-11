package com.reactive.practice;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

class HelloReactorTest {

    @Test
    void test() {
        Flux.just(1, 2, 3, 4)
                .map(each -> each + 1)
                .subscribe(
                        data -> System.out.println(data + " is in"),
                        t -> System.out.println("error occred : " + t.getMessage()),
                        () -> System.out.println("Complete!")
                );
    }

}