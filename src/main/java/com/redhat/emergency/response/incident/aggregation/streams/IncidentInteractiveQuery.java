package com.redhat.emergency.response.incident.aggregation.streams;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.emergency.response.incident.aggregation.model.Incident;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@ApplicationScoped
public class IncidentInteractiveQuery {

    @Inject
    KafkaStreams streams;

    public Incident getIncident(String id) {
        return incidentsStore().get(id);
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
