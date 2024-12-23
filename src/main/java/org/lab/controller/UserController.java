package org.lab.controller;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.annotations.Secured;
import org.lab.model.User;
import org.lab.service.UserService;
import org.lab.utils.ExceptionHandler;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    private UserService userService;

    @Context
    private HttpServletRequest httpServletRequest;

    @POST
    @Path("/register")
    public Response register(User user) {
        String token = userService.register(user);
        return Response.status(Response.Status.CREATED).entity(token).build();
    }

    @POST
    @Path("/login")
    public Response login(User user) {
        try {
            String token = userService.login(user);
            if (token == null)
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
            return Response.ok(token).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @POST
    @Path("/logout")
    public Response logout() {
        try {
            userService.logout();
            return Response.ok().entity("Logged out successfully").build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @GET
    @Path("/me")
    @Secured
    public Response getCurrentUser() {
        try {
            User user = (User) httpServletRequest.getAttribute("currentUser");
            return Response.ok(user).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Integer id) {
        try {
            User user = userService.getUserById(id);
            if (user == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            return Response.ok(user).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @GET
    public Response getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return Response.ok(users).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Integer id, User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            if (updatedUser == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            return Response.ok(updatedUser).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Integer id) {
        try {
            userService.deleteUser(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }
}
