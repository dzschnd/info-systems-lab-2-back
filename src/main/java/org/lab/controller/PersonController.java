package org.lab.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.model.Person;
import org.lab.model.Role;
import org.lab.model.User;
import org.lab.service.PersonService;
import org.lab.service.UserService;
import org.lab.utils.JwtUtils;

import java.util.List;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonController {

    @Inject
    private PersonService personService;

    @Inject
    private UserService userService;


    @GET
    public Response getAllAvailablePersons(@HeaderParam("Authorization") String token) {
        try {
            String username = JwtUtils.extractUsername(token);
            User author = userService.getUserByUsername(username);

            if (author == null || !JwtUtils.validateToken(token, author)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("User not authorized").build();
            }

            List<Person> persons = personService.getAllPerson();
            return Response.ok(persons).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
