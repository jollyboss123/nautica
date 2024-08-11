package org.jolly.nautica;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GtfsClient {
    private final WebClient webClient;
    private final List<VehiclePositionListener> listeners;

    public void run() {
        log.info("running");
        URI uri = UriComponentsBuilder.fromUriString("https://api.data.gov.my/gtfs-realtime/vehicle-position/{agency}")
                .queryParam("category", "rapid-bus-mrtfeeder")
                .buildAndExpand("prasarana")
                .toUri();

//        log.info("there: {}", uri);
        webClient.get()
                .uri(uri)
//                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.createException().flatMap(Mono::error))
                .toEntity(ByteBuffer.class)
                .subscribe(res -> {
//                    log.info("headers: {}", res.getHeaders());

                    List<CompletableFuture<Void>> cfs = listeners.stream()
                                    .map(listener -> CompletableFuture.runAsync(() -> {
                                        try {
                                            listener.onFeedMessage(GtfsRealtime.FeedMessage.parseFrom(res.getBody()));
                                        } catch (InvalidProtocolBufferException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }, Executors.newVirtualThreadPerTaskExecutor()))
                            .collect(Collectors.toList());
                    try {
                        CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0])).join();
                    } catch (CancellationException | CompletionException e) {
                        log.error("error occurred in completable future: ", e);
                    }
                }, err -> log.error("error occurred: ", err));
    }
}
