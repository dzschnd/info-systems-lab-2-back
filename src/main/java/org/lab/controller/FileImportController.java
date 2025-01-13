package org.lab.controller;

import io.minio.MinioClient;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.annotations.Secured;
import org.lab.model.FileImport;
import org.lab.model.User;
import org.lab.service.FileImportService;
import org.lab.utils.ExceptionHandler;
import org.lab.utils.MinioUtils;

import java.io.InputStream;
import java.util.List;

@Path("/file-imports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FileImportController {
    @Inject
    private FileImportService fileImportService;

    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    @Path("/")
    @Secured
    public Response getFromFileImport() {
        User user = (User) httpServletRequest.getAttribute("currentUser");
        try {
            List<FileImport> fileImportHistory = fileImportService.getAllByUser(user);
            if (fileImportHistory.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();
            return Response.status(Response.Status.OK).entity(fileImportHistory).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }

    @GET
    @Path("/download")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadFile(@QueryParam("fileName") String fileName) {
        try {
            if (fileName == null || fileName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("File name is required").build();
            }

            MinioClient minioClient = MinioUtils.getMinioClient();

            InputStream fileStream = MinioUtils.download(minioClient, fileName);

            return Response.ok(fileStream)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }
}
