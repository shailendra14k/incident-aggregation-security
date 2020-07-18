package com.redhat.emergency.response.incident.aggregation.service;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.emergency.response.incident.aggregation.model.Shelter;
import com.redhat.emergency.response.incident.aggregation.rest.client.SheltersService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class Shelters {

    @Inject
    @RestClient
    SheltersService sheltersService;

    Set<Shelter> shelters;

    public Set<Shelter> getShelters() {
        if (shelters == null) {
            shelters = sheltersService.getShelters();
        };
        return shelters;
    }
}
