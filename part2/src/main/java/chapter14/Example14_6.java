package chapter14;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * range 예제
 */
@Slf4j
public class Example14_6 {
    public static void main(String[] args) {
        Flux
            .range(5, 10)
            .subscribe(data -> log.info("{}", data));
    }
}
