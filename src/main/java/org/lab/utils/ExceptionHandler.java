package org.lab.utils;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class ExceptionHandler {
    public static Response handle(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) cause;
            return Response.status(webEx.getResponse().getStatus())
                    .entity(webEx.getMessage())
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("An unexpected error occurred: " + e.getMessage())
                .build();
    }
}
