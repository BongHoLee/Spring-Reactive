import java.time.Duration;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class TempTest {
    Logger log = LoggerFactory.getLogger(TempTest.class);

    @Test
    void test() throws InterruptedException {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4)
                .map(data -> task(data))
                .doOnNext(data -> log.info("# map : {} ", data))
                .filter(each -> each % 2 == 0);

        flux
                .subscribe(
                        new TempSubscriber<>()
                );

        Thread.sleep(1000);
    }

    private int task(int data) {
        return data + 1;
    }

    @Test
    void upstream_downstreamTest() throws InterruptedException {
        Flux.just(1, 2, 3, 4)
                .doOnCancel(() -> log.info("# doOnCancel occurred"))
                .map(data -> {
                    if (data == 3) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                    return data;
                })
                .timeout(Duration.ofMillis(500))
                .doOnError(ex -> log.error("# doOnError! occurred!"))
                .subscribe(
                        data -> log.info("# onNext : {}", data),
                        ex -> log.error("# onError", ex)
                );

        Thread.sleep(2000);

    }
}

class TempSubscriber<T> implements Consumer<T> {

    @Override
    public void accept(T t) {
        System.out.println("# onNext : " + t);
    }
}
