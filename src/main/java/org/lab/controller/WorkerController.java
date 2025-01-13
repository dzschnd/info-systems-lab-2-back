package org.lab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.minio.MinioClient;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.lab.annotations.AuthorOrAdmin;
import org.lab.annotations.Secured;
import org.lab.model.*;
import org.lab.service.WorkerService;
import org.lab.utils.ExceptionHandler;
import org.lab.utils.MinioUtils;
import org.lab.validation.WorkerValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    @Path("/from-file-import")
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response handleWorkersFromFile(MultipartFormDataInput input) {
        try {
            InputStream fileInputStream = input.getFormDataPart("file", InputStream.class, null);
            String fileName = input.getFormDataPart("fileName", String.class, null);

            if (fileInputStream == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("File is missing")
                        .build();
            }

            byte[] fileBytes = fileInputStream.readAllBytes();
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
            ByteArrayInputStream fileInputStreamForUpload = new ByteArrayInputStream(fileBytes);

            MinioClient minioClient = MinioUtils.getMinioClient();

            List<Worker> workers;
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            try {
                workers = objectMapper.readValue(fileContent, objectMapper.getTypeFactory().constructCollectionType(List.class, Worker.class));
            } catch (IOException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON format: " + e.getMessage()).build();
            }

            User user = (User) httpServletRequest.getAttribute("currentUser");

            for (int i = 0; i < workers.size(); i++) {
                Worker worker = workers.get(i);
                workerValidator.validate(worker, user, validator);
                workers.set(i, workerValidator.validate(worker, user, validator));
            }

            List<Worker> handledWorkers = workerService.handleImportedWorker(workers, user, minioClient, fileName, fileInputStreamForUpload);

            return Response.ok(handledWorkers).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }


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
            worker.setId(id);
            workerValidator.validate(worker, user, validator);
            Worker updatedWorker = workerService.updateWorker(worker, user);
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
