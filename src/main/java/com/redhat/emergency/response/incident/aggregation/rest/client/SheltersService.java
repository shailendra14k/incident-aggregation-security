package com.redhat.emergency.response.incident.aggregation.rest.client;

import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.redhat.emergency.response.incident.aggregation.model.Shelter;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient(configKey="shelters")
public interface SheltersService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/shelters")
    Set<Shelter> getShelters();

}
