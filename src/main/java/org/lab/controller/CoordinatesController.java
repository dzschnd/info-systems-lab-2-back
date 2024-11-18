package org.lab.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.annotations.Secured;
import org.lab.model.Coordinates;
import org.lab.service.CoordinatesService;
import org.lab.utils.ExceptionHandler;

import java.util.List;

@Path("/coordinates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoordinatesController {

    @Inject
    private CoordinatesService coordinatesService;

    @GET
    @Secured
    public Response getAllCoordinates() {
        try {
            List<Coordinates> coordinates = coordinatesService.getAllCoordinates();
            return Response.ok(coordinates).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);

        }
    }
}
