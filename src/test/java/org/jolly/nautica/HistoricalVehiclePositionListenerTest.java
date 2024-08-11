package org.jolly.nautica;

import com.google.transit.realtime.GtfsRealtime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HistoricalVehiclePositionListenerTest {
//    @Autowired
//    private HistoricalVehiclePositionListener listener;
    @Autowired
    private CurrentVehiclePositionListener listener;

    @Test
    void test() {
        List<GtfsRealtime.FeedEntity> entities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entities.add(GtfsRealtime.FeedEntity.newBuilder()
                    .setId(String.valueOf(i))
                    .setVehicle(GtfsRealtime.VehiclePosition.newBuilder()
                            .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
                                    .setTripId("240731020095S1".concat(String.valueOf(i)))
                                    .setRouteId("T56".concat(String.valueOf(i)))
                                    .build())
                            .setPosition(GtfsRealtime.Position.newBuilder()
                                    .setLatitude(3.009742F)
                                    .setLongitude(101.72199F)
                                    .setBearing(153.0F)
                                    .setSpeed(0.0F)
                                    .build())
                            .setTimestamp(1723372910L)
                            .setVehicle(GtfsRealtime.VehicleDescriptor.newBuilder()
                                    .setId("VH359".concat(String.valueOf(i)))
                                    .setLicensePlate("VH359".concat(String.valueOf(i)))
                                    .build())
                            .build())
                    .build());
        }
        GtfsRealtime.FeedMessage message = GtfsRealtime.FeedMessage.newBuilder()
//                .addEntity(GtfsRealtime.FeedEntity.newBuilder()
//                        .setId("0")
//                        .setVehicle(GtfsRealtime.VehiclePosition.newBuilder()
//                                .setTrip(GtfsRealtime.TripDescriptor.newBuilder()
//                                        .setTripId("240731020095S13")
//                                        .setRouteId("T567")
//                                        .build())
//                                .setPosition(GtfsRealtime.Position.newBuilder()
//                                        .setLatitude(3.009742F)
//                                        .setLongitude(101.72199F)
//                                        .setBearing(153.0F)
//                                        .setSpeed(0.0F)
//                                        .build())
//                                .setTimestamp(1723372910L)
//                                .setVehicle(GtfsRealtime.VehicleDescriptor.newBuilder()
//                                        .setId("VH3593")
//                                        .setLicensePlate("VH3593")
//                                        .build())
//                                .build())
//                        .build())
                .addAllEntity(entities)
                .buildPartial();

        listener.onFeedMessage(message);
    }
}