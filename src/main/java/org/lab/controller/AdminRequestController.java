package org.lab.controller;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import org.lab.annotations.AdminOnly;
import org.lab.annotations.Secured;
import org.lab.model.AdminRequest;
import org.lab.service.AdminRequestService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.model.User;
import org.lab.utils.ExceptionHandler;

import java.util.List;

@Path("/admin-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminRequestController {

    @Inject
    private AdminRequestService adminRequestService;

    @Context
    private ContainerRequestContext requestContext;
    
    @POST
    @Path("/request")
    @Secured
    public Response createRequest() {
        User user = (User) requestContext.getProperty("currentUser");
        AdminRequest request = adminRequestService.createRequest(user);
        return Response.status(Response.Status.CREATED).entity(request).build();
    }

    @GET
    @Path("/pending")
    @Secured
    @AdminOnly
    public Response getPendingRequests() {
        List<AdminRequest> pendingRequests = adminRequestService.getPendingRequests();
        return Response.ok(pendingRequests).build();
    }

    @POST
    @Path("/{requestId}/approve")
    @Secured
    @AdminOnly
    public Response approveRequest(@PathParam("requestId") Integer requestId) {
        try {
            adminRequestService.approveRequest(requestId);
            return Response.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @POST
    @Path("/{requestId}/reject")
    @Secured
    @AdminOnly
    public Response rejectRequest(@PathParam("requestId") Integer requestId) {
        try {
            adminRequestService.rejectRequest(requestId);
            return Response.ok().build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }
}
