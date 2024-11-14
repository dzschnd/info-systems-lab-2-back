package org.lab.controller;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.model.*;
import org.lab.service.UserService;
import org.lab.service.WorkerService;
import org.lab.utils.JwtUtils;
import org.lab.validation.WorkerValidator;

import java.util.List;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkerController {

    @Inject
    private WorkerService workerService;

    @Inject
    private UserService userService;
    @Inject
    private Validator validator;

    @Inject
    private WorkerValidator workerValidator;

    @POST
    public Response createWorker(Worker worker, @HeaderParam("Authorization") String token) {
        try {
            String username = JwtUtils.extractUsername(token);
            User author = userService.getUserByUsername(username);

            if (author == null || !JwtUtils.validateToken(token, author)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build();
            }

            workerValidator.validate(worker, author, validator);

            Worker createdWorker = workerService.createWorker(worker, author);
            return Response.status(Response.Status.CREATED).entity(createdWorker).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getWorkerById(@PathParam("id") Integer id) {
        try {
            Worker worker = workerService.getWorkerById(id);
            if (worker != null) {
                return Response.ok(worker).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Worker not found").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    public Response getAllWorkers() {
        try {
            List<Worker> workers = workerService.getAllWorkers();
            return Response.ok(workers).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateWorker(@PathParam("id") Integer id, Worker worker, @HeaderParam("Authorization") String token) {
        try {
            String username = JwtUtils.extractUsername(token);
            User author = userService.getUserByUsername(username);

            if (author == null || !JwtUtils.validateToken(token, author)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build();
            }

            Worker existingWorker = workerService.getWorkerById(id);
            if (existingWorker == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Worker not found").build();
            }

            if (!existingWorker.getAuthor().getId().equals(author.getId()) && !author.getRole().equals(Role.ADMIN)) {
                return Response.status(Response.Status.FORBIDDEN).entity("User not authorized to update this worker").build();
            }

            workerValidator.validate(worker, author, validator);

            worker.setId(id);
            worker.setAuthor(author);
            Worker updatedWorker = workerService.updateWorker(worker);
            return Response.ok(updatedWorker).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteWorker(@PathParam("id") Integer id, @HeaderParam("Authorization") String token) {
        try {
            String username = JwtUtils.extractUsername(token);
            User author = userService.getUserByUsername(username);

            if (author == null || !JwtUtils.validateToken(token, author)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build();
            }

            Worker existingWorker = workerService.getWorkerById(id);
            if (existingWorker == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Worker not found").build();
            }

            if (!existingWorker.getAuthor().getId().equals(author.getId()) && !author.getRole().equals(Role.ADMIN)) {
                return Response.status(Response.Status.FORBIDDEN).entity("User not authorized to delete this worker").build();
            }

            workerService.deleteWorker(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
