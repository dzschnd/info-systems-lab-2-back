package org.lab.controller;

import org.lab.model.AdminRequest;
import org.lab.model.Role;
import org.lab.service.AdminRequestService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.model.User;
import org.lab.service.UserService;
import org.lab.utils.JwtUtils;

import java.util.List;

@Path("/admin-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminRequestController {

    @Inject
    private AdminRequestService adminRequestService;

    @Inject
    private UserService userService;
    
    @POST
    @Path("/request")
    public Response createRequest(@HeaderParam("Authorization") String token) {
        String username = JwtUtils.extractUsername(token);
        User user = userService.getUserByUsername(username);

        if (user == null || !JwtUtils.validateToken(token, user)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build();
        }

        AdminRequest request = adminRequestService.createRequest(user);
        return Response.status(Response.Status.CREATED).entity(request).build();
    }

    @GET
    @Path("/pending")
    public Response getPendingRequests(@HeaderParam("Authorization") String token) {
        String username = JwtUtils.extractUsername(token);
        User user = userService.getUserByUsername(username);

        if (user == null || !JwtUtils.validateToken(token, user)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build();
        }

        if (!user.getRole().equals(Role.ADMIN)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Only admins can view pending requests").build();
        }

        List<AdminRequest> pendingRequests = adminRequestService.getPendingRequests();
        return Response.ok(pendingRequests).build();
    }

    @POST
    @Path("/{requestId}/approve")
    public Response approveRequest(@HeaderParam("Authorization") String token, @PathParam("requestId") Integer requestId) {
        String username = JwtUtils.extractUsername(token);
        User user = userService.getUserByUsername(username);

        if (user == null || !JwtUtils.validateToken(token, user)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build();
        }

        if (!user.getRole().equals(Role.ADMIN)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Only admins can approve requests").build();
        }

        try {
            adminRequestService.approveRequest(requestId);
            return Response.ok().build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{requestId}/reject")
    public Response rejectRequest(@HeaderParam("Authorization") String token, @PathParam("requestId") Integer requestId) {
        String username = JwtUtils.extractUsername(token);
        User user = userService.getUserByUsername(username);

        if (user == null || !JwtUtils.validateToken(token, user)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build();
        }

        if (!user.getRole().equals(Role.ADMIN)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Only admins can answer requests").build();
        }

        try {
            adminRequestService.rejectRequest(requestId);
            return Response.ok().build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
