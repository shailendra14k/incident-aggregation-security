package com.redhat.emergency.response.incident.aggregation.streams;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.emergency.response.incident.aggregation.model.Incident;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class IncidentInteractiveQuery {

    private static Logger log = LoggerFactory.getLogger(IncidentInteractiveQuery.class);

    @ConfigProperty(name = "pod.ip")
    String podIP;

    @Inject
    KafkaStreams streams;

    public Pair<Incident, String> getIncident(String id) {
        KeyQueryMetadata metadata = streams.queryMetadataForKey("incidents-store", id, Serdes.String().serializer());
        if (metadata == null || metadata == KeyQueryMetadata.NOT_AVAILABLE) {
            log.warn("No metadata found for key: " + id);
            return null;
        }

        if (podIP.equals(metadata.getActiveHost().host())) {
            log.debug("Key available on local host: " + id);
            return new ImmutablePair<>(incidentsStore().get(id), null);
        } else {
            log.debug("Key available on remote host " + metadata.getActiveHost().host() + ": " + id);
            return new ImmutablePair<>(null, metadata.getActiveHost().host());
        }

    }

    private ReadOnlyKeyValueStore<String, Incident> incidentsStore() {
        while (true) {
            try {
                return streams.store(StoreQueryParameters.fromNameAndType("incidents-store", QueryableStoreTypes.keyValueStore()));
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}
