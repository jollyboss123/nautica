package org.jolly.nautica;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GtfsClientTest {
    @Autowired
    private GtfsClient gtfsClient;
//    @SpyBean
//    private GtfsClient gtfsClient;

    @Test
    void test() {
        gtfsClient.run();
//        Awaitility.await().atMost(Durations.ONE_MINUTE)
//                .untilAsserted(() -> Mockito.verify(gtfsClient, Mockito.atLeast(4)).run());
    }

}