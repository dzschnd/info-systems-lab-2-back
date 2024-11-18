package org.lab.filters;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.lab.annotations.Secured;
import org.lab.model.User;
import org.lab.service.UserService;
import org.lab.utils.JwtUtils;
import java.lang.annotation.Annotation;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Inject
    UserService userService;

    @Context
    ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        boolean isSecured = false;

        for (Annotation annotation : resourceInfo.getResourceMethod().getAnnotations()) {
            if (Secured.class.isAssignableFrom(annotation.annotationType())) {
                isSecured = true;
                break;
            }
        }

        if (isSecured) {
            String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authHeader == null) {
                requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build()
                );
                return;
            }

            User user = JwtUtils.extractUser(authHeader);

            if (user == null || !JwtUtils.validateToken(authHeader, user)) {
                requestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build()
                );
                return;
            }

            httpServletRequest.setAttribute("currentUser", user);
        }
    }
}
