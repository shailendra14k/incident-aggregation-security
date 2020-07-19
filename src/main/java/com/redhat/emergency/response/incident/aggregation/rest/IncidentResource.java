package com.redhat.emergency.response.incident.aggregation.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.emergency.response.incident.aggregation.model.Incident;
import com.redhat.emergency.response.incident.aggregation.streams.IncidentInteractiveQuery;

@ApplicationScoped
@Path("/")
public class IncidentResource {

    @Inject
    IncidentInteractiveQuery incidentInteractiveQuery;

    @GET
    @Path("/incident/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIncident(@PathParam("id") String id) {
        Incident result = incidentInteractiveQuery.getIncident(id);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
        } else {
            return Response.ok(result).build();
        }
    }

}
