package com.redhat.emergency.response.incident.aggregation.rest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Path("/")
public class IncidentResource {

    private static final Logger log = LoggerFactory.getLogger(IncidentResource.class);

    @Inject
    IncidentInteractiveQuery incidentInteractiveQuery;

    @Inject
    Vertx vertx;

    @GET
    @Path("/incident/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIncident(@PathParam("id") String id) {
        Pair<Incident, String> incident = incidentInteractiveQuery.getIncident(id);
        if (incident == null) {
            return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
        } else if (incident.getLeft() != null) {
            return Response.ok(incident.getLeft()).build();
        } else if (incident.getRight() != null) {
            WebClient client = WebClient.create(vertx);
            CompletableFuture<Pair<Incident, Integer>> future = new CompletableFuture<>();
            client.get(8080, incident.getRight(), "/incident/" + id).send(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> response = ar.result();
                    if (response.statusCode() == Response.Status.OK.getStatusCode()) {
                        Incident i = response.bodyAsJson(Incident.class);
                        Pair<Incident, Integer> result = new ImmutablePair<>(i, response.statusCode());
                        future.complete(result);
                    } else if (response.statusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
                        log.warn("Incident with key " + id + " not found on remote host " + incident.getRight());
                        Pair<Incident, Integer> result = new ImmutablePair<>(null, response.statusCode());
                        future.complete(result);
                    }
                } else {
                    log.error("Cannot retrieve incident with key " + id + " from remote host " + incident.getRight(), ar.cause());
                    Pair<Incident, Integer> result = new ImmutablePair<>(null, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    future.complete(result);
                }
            });
            try {
                Pair<Incident, Integer> result = future.get(2, TimeUnit.SECONDS);
                if (result.getRight().equals(Response.Status.OK.getStatusCode())) {
                    return Response.ok(result.getLeft()).build();
                } else if (result.getRight().equals(Response.Status.NOT_FOUND.getStatusCode())) {
                    return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            } catch (Exception e) {
                log.error("Exception while waiting for result of remote call", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
        }
    }

}
