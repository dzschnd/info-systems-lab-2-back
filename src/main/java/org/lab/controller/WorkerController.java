package org.lab.controller;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.annotations.AuthorOrAdmin;
import org.lab.annotations.Secured;
import org.lab.model.*;
import org.lab.service.WorkerService;
import org.lab.utils.ExceptionHandler;
import org.lab.validation.WorkerValidator;

import java.util.List;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkerController {

    @Inject
    private WorkerService workerService;

    @Context
    private HttpServletRequest httpServletRequest;

    @Inject
    private Validator validator;

    @Inject
    private WorkerValidator workerValidator;

    @POST
    @Secured
    public Response createWorker(Worker worker) {
        try {
            User user = (User) httpServletRequest.getAttribute("currentUser");
            workerValidator.validate(worker, user, validator);
            Worker createdWorker = workerService.createWorker(worker, user);
            return Response.status(Response.Status.CREATED).entity(createdWorker).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
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
            return ExceptionHandler.handle(e);
        }
    }

    @GET
    public Response getAllWorkers() {
        try {
            List<Worker> workers = workerService.getAllWorkers();
            return Response.ok(workers).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @PUT
    @Path("/{id}")
    @Secured
    @AuthorOrAdmin
    public Response updateWorker(@PathParam("id") Integer id, Worker worker) {
        try {
            User user = (User) httpServletRequest.getAttribute("currentUser");

            workerValidator.validate(worker, user, validator);

            worker.setId(id);
            worker.setAuthor(user);
            Worker updatedWorker = workerService.updateWorker(worker);
            return Response.ok(updatedWorker).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @DELETE
    @Path("/{id}")
    @Secured
    @AuthorOrAdmin
    public Response deleteWorker(@PathParam("id") Integer id) {
        try {
            workerService.deleteWorker(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }
}
