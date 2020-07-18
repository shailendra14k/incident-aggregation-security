package com.redhat.emergency.response.incident.aggregation.model;

import java.math.BigDecimal;

public class ResponderLocationHistory{

    private long timestamp;

    private BigDecimal lat;

    private BigDecimal lon;

    public long getTimestamp() {
        return timestamp;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLon() {
        return lon;
    }
}
