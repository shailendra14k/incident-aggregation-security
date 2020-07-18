package com.redhat.emergency.response.incident.aggregation.model;

import java.math.BigDecimal;

public class MissionStep {

    private BigDecimal lat;

    private BigDecimal lon;

    private Boolean wayPoint;

    private Boolean destination;

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public boolean isWayPoint() {
        return wayPoint;
    }

    public boolean isDestination() {
        return destination;
    }
}
