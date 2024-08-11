package org.jolly.nautica;

import com.google.transit.realtime.GtfsRealtime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.postgis.jdbc.PGgeometry;
import net.postgis.jdbc.geometry.Point;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentVehiclePositionListener implements VehiclePositionListener {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public void onFeedMessage(GtfsRealtime.FeedMessage feedMessage) {
        log.info("running");
        int batchSize = 80;
        List<List<GtfsRealtime.FeedEntity>> entityBatches = CollectionUtils.getBatches(feedMessage.getEntityList(), batchSize);
        LocalDateTime now = LocalDateTime.now();

        for (List<GtfsRealtime.FeedEntity> batch : entityBatches) {
            List<MapSqlParameterSource> params = batch.stream()
                            .map(entity -> new MapSqlParameterSource()
                                    .addValues(Map.of(
                                            "vehicle_id", entity.getVehicle().getVehicle().getId(),
                                            "trip_id", entity.getVehicle().getTrip().getTripId(),
                                            "position", new PGgeometry(new Point(
                                                    entity.getVehicle().getPosition().getLatitude(),
                                                    entity.getVehicle().getPosition().getLongitude()
                                            )),
                                            "bearing", entity.getVehicle().getPosition().getBearing(),
                                            "speed", entity.getVehicle().getPosition().getSpeed(),
                                            "created_on", now
                                    )))
                            .collect(Collectors.toList());

            jdbcTemplate.batchUpdate("""
                insert into curr_vehicle_positions (vehicle_id, trip_id, position, bearing, speed, created_on)
                values (:vehicle_id, :trip_id, :position, :bearing, :speed, :created_on)
                on conflict(vehicle_id)
                do update set
                    trip_id = excluded.trip_id,
                    position = excluded.position,
                    bearing = excluded.bearing,
                    speed = excluded.speed,
                    updated_on = excluded.created_on
                """,
                params.toArray(new MapSqlParameterSource[0]));
        }
    }
}
