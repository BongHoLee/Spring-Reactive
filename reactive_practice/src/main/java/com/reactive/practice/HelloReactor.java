package com.reactive.practice;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
public class HelloReactor {
    private static final URI worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
            .host("worldtimeapi.org")
            .port(80)
            .path("/api/timezone/Asia/Seoul")
            .build()
            .encode()
            .toUri();

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final HttpHeaders headers = new HttpHeaders();

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Mono.fromSupplier(() ->
                restTemplate.exchange(
                        worldTimeUri,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class))
                .map(response -> {
                    DocumentContext jsonContext = JsonPath.parse(response.getBody());
                    return jsonContext.<String>read("$.datetime");
                })
                .subscribe(
                        data -> log.info("$ emitted data : {}", data),
                        ex -> log.error("exception occurred ", ex),
                        () -> {
                            log.info("COMPLETED");
                            latch.countDown();
                        }
                );

        latch.await();
    }
}
