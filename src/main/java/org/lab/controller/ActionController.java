package org.lab.controller;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.annotations.Secured;
import org.lab.model.Action;
import org.lab.model.User;
import org.lab.service.ActionService;
import org.lab.utils.ExceptionHandler;

import java.util.List;

@Path("/actions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ActionController {
    @Inject
    private ActionService actionService;

    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    @Path("/from-file-import")
    @Secured
    public Response getFromFileImport() {
        User user = (User) httpServletRequest.getAttribute("currentUser");
        try {
            List<Action> fileImportHistory = actionService.getAllImported(user);
            if (fileImportHistory.isEmpty())
                return Response.status(Response.Status.NO_CONTENT).build();
            return Response.status(Response.Status.OK).entity(fileImportHistory).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }
}
