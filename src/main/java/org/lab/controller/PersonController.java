package org.lab.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lab.annotations.Secured;
import org.lab.model.Person;
import org.lab.service.PersonService;
import org.lab.utils.ExceptionHandler;

import java.util.List;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonController {

    @Inject
    private PersonService personService;

    @GET
    @Secured
    public Response getAllPersons() {
        try {
            List<Person> persons = personService.getAllPerson();
            return Response.ok(persons).build();
        } catch (Exception e) {
            return ExceptionHandler.handle(e);
        }
    }
}
