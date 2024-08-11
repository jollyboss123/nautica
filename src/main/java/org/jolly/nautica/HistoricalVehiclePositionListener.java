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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Component
@Slf4j
@RequiredArgsConstructor
public class HistoricalVehiclePositionListener implements VehiclePositionListener {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public void onFeedMessage(GtfsRealtime.FeedMessage feedMessage) {
        int batchSize = 80;
        List<List<GtfsRealtime.FeedEntity>> entityBatches = getBatches(feedMessage.getEntityList(), batchSize);
        LocalDateTime createdOn = LocalDateTime.now();

        for (List<GtfsRealtime.FeedEntity> batch : entityBatches) {
            long[] sequences = getNextSequenceBatch(batch.size());
            List<MapSqlParameterSource> params = IntStream.range(0, batch.size())
                    .mapToObj(i -> new MapSqlParameterSource()
                            .addValues(Map.of(
                                    "id", sequences[i],
                                    "vehicle_id", batch.get(i).getVehicle().getVehicle().getId(),
                                    "trip_id", batch.get(i).getVehicle().getTrip().getTripId(),
                                    "position", new PGgeometry(new Point(
                                            batch.get(i).getVehicle().getPosition().getLatitude(),
                                            batch.get(i).getVehicle().getPosition().getLongitude()
                                    )),
                                    "bearing", batch.get(i).getVehicle().getPosition().getBearing(),
                                    "speed", batch.get(i).getVehicle().getPosition().getSpeed(),
                                    "created_on", createdOn
                            )))
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate("""
                insert into hist_vehicle_positions (id, vehicle_id, trip_id, position, bearing, speed, created_on)
                values (:id, :vehicle_id, :trip_id, :position, :bearing, :speed, :created_on)
                """,
                params.toArray(new MapSqlParameterSource[0]));
        }
    }

    private long[] getNextSequenceBatch(int batchSize) {
        Long firstSeq = jdbcTemplate.queryForObject("SELECT nextval('hist_vehicle_positions_seq')", Collections.emptyMap(), Long.class);
        if (firstSeq == null)
            throw new RuntimeException();

        return LongStream.range(firstSeq, firstSeq + batchSize).toArray();
    }

    private static <T> List<List<T>> getBatches(List<T> collection, int batchSize) {
        return IntStream.iterate(0, i -> i < collection.size(), i -> i + batchSize)
                .mapToObj(i -> collection.subList(i, Math.min(i + batchSize, collection.size())))
                .collect(Collectors.toList());
    }
}
