package com.redhat.emergency.response.incident.aggregation.model;

import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Shelter {

    private String name;

    private BigDecimal lat;

    private BigDecimal lon;

    public String getName() {
        return name;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    static Set<Shelter> getShelters() {
        return null;
    }
}

