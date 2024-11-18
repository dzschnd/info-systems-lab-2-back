package org.lab.filters;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.lab.annotations.AuthorOrAdmin;
import org.lab.model.Role;
import org.lab.model.User;
import org.lab.model.Worker;
import org.lab.service.WorkerService;

import java.lang.annotation.Annotation;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorOrAdminFilter implements ContainerRequestFilter {

    @Context
    ResourceInfo resourceInfo;

    @Inject
    WorkerService workerService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        boolean isAuthorOrAdmin = false;

        for (Annotation annotation : resourceInfo.getResourceMethod().getAnnotations()) {
            if (AuthorOrAdmin.class.isAssignableFrom(annotation.annotationType())) {
                isAuthorOrAdmin = true;
                break;
            }
        }

        if (isAuthorOrAdmin) {
            User user = (User) requestContext.getProperty("currentUser");

            String path = requestContext.getUriInfo().getPath();
            Integer workerId = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));

            Worker worker = workerService.getWorkerById(workerId);

            if (worker == null) {
                requestContext.abortWith(Response.status(Response.Status.NOT_FOUND).entity("Worker not found").build());
                return;
            }

            if (!worker.getAuthor().getId().equals(user.getId()) && !user.getRole().equals(Role.ADMIN)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("User not authorized to modify this worker").build());
            }
        }
    }
}
