package com.redhat.emergency.response.incident.aggregation.streams;

import java.math.BigDecimal;
import java.util.Arrays;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.redhat.emergency.response.incident.aggregation.model.Incident;
import com.redhat.emergency.response.incident.aggregation.model.Mission;
import com.redhat.emergency.response.incident.aggregation.model.Shelter;
import com.redhat.emergency.response.incident.aggregation.service.Shelters;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TopologyProducer {

    private static final Logger log = LoggerFactory.getLogger(TopologyProducer.class);

    private static final String INCIDENT_REPORTED_EVENT = "IncidentReportedEvent";
    private static final String INCIDENT_UPDATED_EVENT = "IncidentUpdatedEvent";

    private static final String MISSION_STARTED_EVENT = "MissionStartedEvent";
    private static final String MISSION_PICKEDUP_EVENT = "MissionPickedUpEvent";
    private static final String MISSION_COMPLETED_EVENT = "MissionCompletedEvent";

    private static final String[] INCIDENT_ACCEPTED_MESSAGE_TYPES = {INCIDENT_REPORTED_EVENT, INCIDENT_UPDATED_EVENT};
    private static final String[] MISSION_ACCEPTED_MESSAGE_TYPES = {MISSION_STARTED_EVENT, MISSION_PICKEDUP_EVENT, MISSION_COMPLETED_EVENT};

    @ConfigProperty(name = "kafka.topic.incident-event")
    String incidentEventTopic;

    @ConfigProperty(name = "kafka.topic.mission-event")
    String missionEventTopic;

    @Inject
    Shelters shelters;

    @Produces
    public Topology buildTopology() {

        StreamsBuilder builder = new StreamsBuilder();

        ObjectMapperSerde<Incident> incidentSerde = new ObjectMapperSerde<>(Incident.class);

        KTable<String, Mission> missions = builder.table(missionEventTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(value -> {
                    try {
                        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject(value);
                        log.debug("Processing message: " + value);
                        return json;
                    } catch (Exception e) {
                        log.warn("Unexpected message which is not a valid JSON object");
                        return new io.vertx.core.json.JsonObject();
                    }
                })
                .filter((key, value) -> {
                    String messageType = value.getString("messageType");
                    if (Arrays.asList(MISSION_ACCEPTED_MESSAGE_TYPES).contains(messageType)) {
                        return true;
                    }
                    log.debug("Message with type '" + messageType + "' is ignored");
                    return false;
                }).mapValues(value -> {
                    io.vertx.core.json.JsonObject body = value.getJsonObject("body");
                    try {
                        return body.mapTo(Mission.class);
                    } catch (Exception e) {
                        log.error("Exception while deserializing Mission", e);
                        return null;
                    }
                });

        KTable<String, Incident> incidents = builder.table(incidentEventTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(value -> {
                    try {
                        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject(value);
                        log.debug("Processing message: " + value);
                        return json;
                    } catch (Exception e) {
                        log.warn("Unexpected message which is not a valid JSON object");
                        return new io.vertx.core.json.JsonObject();
                    }
                }).filter((key, value) -> {
                    String messageType = value.getString("messageType");
                    if (Arrays.asList(INCIDENT_ACCEPTED_MESSAGE_TYPES).contains(messageType)) {
                        return true;
                    }
                    log.debug("Message with type '" + messageType + "' is ignored");
                    return false;
                }).mapValues(value -> {
                    io.vertx.core.json.JsonObject body = value.getJsonObject("body");
                    try {
                        return body.mapTo(Incident.class);
                    } catch (Exception e) {
                        log.error("Exception while deserializing Incident", e);
                        return null;
                    }
                }).leftJoin(missions, (incident, mission) -> {
                    if (mission == null) {
                        return incident;
                    }
                    incident.setDestinationLat(mission.getDestinationLat());
                    incident.setDestinationLon(mission.getDestinationLong());
                    incident.setDestinationName(destinationName(mission.getDestinationLat(), mission.getDestinationLong()));
                    if (mission.getResponderLocationHistory().isEmpty()) {
                        incident.setCurrentPositionLat(incident.getLat());
                        incident.setCurrentPositionLon(incident.getLon());
                    } else {
                        int last = mission.getResponderLocationHistory().size() - 1;
                        incident.setCurrentPositionLat(mission.getResponderLocationHistory().get(last).getLat());
                        incident.setCurrentPositionLon(mission.getResponderLocationHistory().get(last).getLon());
                    }
                    return incident;
                }, Materialized.<String, Incident, KeyValueStore<Bytes, byte[]>> as("incidents-store").withKeySerde(Serdes.String()).withValueSerde(incidentSerde));

        return builder.build();
    }

    private String destinationName(BigDecimal lat, BigDecimal lon) {
        return shelters.getShelters().stream()
                .filter(shelter -> shelter.getLat().equals(lat) && shelter.getLon().equals(lon))
                .map(Shelter::getName).findFirst().orElse("");
    }
}
