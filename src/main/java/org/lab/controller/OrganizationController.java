package org.lab.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.annotations.Secured;
import org.lab.model.Organization;
import org.lab.service.OrganizationService;
import org.lab.utils.ExceptionHandler;

import java.util.List;

@Path("/organizations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrganizationController {

    @Inject
    private OrganizationService organizationService;

    @GET
    @Secured
    public Response getAllOrganizations() {
        try {
            List<Organization> organizations = organizationService.getAllOrganizations();
            return Response.ok(organizations).build();

        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }
}
