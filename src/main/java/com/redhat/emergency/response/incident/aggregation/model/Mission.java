package com.redhat.emergency.response.incident.aggregation.model;

import java.math.BigDecimal;
import java.util.List;

public class Mission {

    private String id;
    private String incidentId;
    private String responderId;

    private BigDecimal responderStartLat;

    private BigDecimal responderStartLong;

    private BigDecimal incidentLat;

    private BigDecimal incidentLong;

    private BigDecimal destinationLat;

    private BigDecimal destinationLong;

    private List<ResponderLocationHistory> responderLocationHistory;

    private List<MissionStep> steps;

    private String status;

    public String getId() {
        return id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getResponderId() {
        return responderId;
    }

    public BigDecimal getResponderStartLat() {
        return responderStartLat;
    }

    public BigDecimal getResponderStartLong() {
        return responderStartLong;
    }

    public BigDecimal getIncidentLat() {
        return incidentLat;
    }

    public BigDecimal getIncidentLong() {
        return incidentLong;
    }

    public BigDecimal getDestinationLat() {
        return destinationLat;
    }

    public BigDecimal getDestinationLong() {
        return destinationLong;
    }

    public List<ResponderLocationHistory> getResponderLocationHistory() {
        return responderLocationHistory;
    }

    public List<MissionStep> getSteps() {
        return steps;
    }

    public String getStatus() {
        return status;
    }

}
