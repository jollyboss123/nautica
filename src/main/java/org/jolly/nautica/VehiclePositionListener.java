package org.jolly.nautica;

import com.google.transit.realtime.GtfsRealtime;

public interface VehiclePositionListener {
    void onFeedMessage(GtfsRealtime.FeedMessage feedMessage);
}
