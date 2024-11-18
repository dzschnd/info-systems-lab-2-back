package org.lab.filters;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.lab.annotations.AdminOnly;
import org.lab.annotations.AuthorOrAdmin;
import org.lab.annotations.Secured;
import org.lab.model.Role;
import org.lab.model.User;

import java.lang.annotation.Annotation;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class AdminOnlyFilter implements ContainerRequestFilter {

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        boolean isAdminOnly = false;

        for (Annotation annotation : resourceInfo.getResourceMethod().getAnnotations()) {
            if (AdminOnly.class.isAssignableFrom(annotation.annotationType())) {
                isAdminOnly = true;
                break;
            }
        }

        if (isAdminOnly) {
            User user = (User) requestContext.getProperty("currentUser");

            if (user == null || !user.getRole().equals(Role.ADMIN)) {
                requestContext.abortWith(
                        Response.status(Response.Status.FORBIDDEN).entity("Only admins can access this resource").build()
                );
            }
        }
    }
}
