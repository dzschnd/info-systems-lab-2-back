package org.lab.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.lab.model.*;
import org.lab.repository.OrganizationRepository;
import org.lab.repository.PersonRepository;

import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class WorkerValidator {
    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private PersonRepository personRepository;

    public void validate(Worker worker, User author, Validator validator) throws WebApplicationException {
        Map<String, List<String>> validationErrors = new HashMap<>();

        worker.setAuthor(author);

        Set<ConstraintViolation<Worker>> workerViolations = validator.validate(worker);
        if (!workerViolations.isEmpty()) {
            validationErrors.put("Worker", formatViolationsToJson(workerViolations));
        }

        if (worker.getStatus() != null && !worker.getStatus().isEmpty()) {
            List<String> validEnumValues = getEnumValues(Status.class);

            String status = worker.getStatus();
            if (!validEnumValues.contains(status)) {
                validationErrors.computeIfAbsent("Worker", k -> new ArrayList<>())
                        .add("Status '" + status + "' is not valid.");
            }
        }

        if (worker.getCoordinates() != null) {
            worker.getCoordinates().setAuthor(author);
            Set<ConstraintViolation<Coordinates>> coordinatesViolations = validator.validate(worker.getCoordinates());
            if (!coordinatesViolations.isEmpty()) {
                validationErrors.put("Coordinates", formatViolationsToJson(coordinatesViolations));
            }
        }

        if (worker.getOrganization() != null) {
            Organization organization = worker.getOrganization();
            organization.setAuthor(author);
            Set<ConstraintViolation<Organization>> organizationViolations = validator.validate(organization);
            if (!organizationViolations.isEmpty()) {
                validationErrors.put("Organization", formatViolationsToJson(organizationViolations));
            }

            if (worker.getId() == null && organization.getId() == null) {
                if (organizationRepository.findAll().stream()
                        .anyMatch(existingOrganization -> existingOrganization.getFullName().equals(organization.getFullName()))) {
                    validationErrors.computeIfAbsent("Organization", k -> new ArrayList<>())
                            .add("Organization with full name '" + organization.getFullName() + "' already exists.");
                }
            } else if (worker.getId() != null && organization.getId() != null) {
                if (organizationRepository.findAll().stream()
                        .filter(existingOrganization -> !existingOrganization.getId().equals(organization.getId()))
                        .anyMatch(existingOrganization -> existingOrganization.getFullName().equals(organization.getFullName()))) {
                    validationErrors.computeIfAbsent("Organization", k -> new ArrayList<>())
                            .add("Organization with full name '" + organization.getFullName() + "' already exists.");
                }
            }

            if (organization.getType() != null) {
                String type = organization.getType();
                List<String> validEnumValues = getEnumValues(OrganizationType.class);

                if (!validEnumValues.contains(type)) {
                    validationErrors.computeIfAbsent("Organization", k -> new ArrayList<>())
                            .add("Type '" + type + "' is not valid.");
                }
            }

            if (organization.getOfficialAddress() != null) {
                organization.getOfficialAddress().setAuthor(author);
                Set<ConstraintViolation<Address>> addressViolations = validator.validate(organization.getOfficialAddress());
                if (!addressViolations.isEmpty()) {
                    validationErrors.put("Address", formatViolationsToJson(addressViolations));
                }

                if (organization.getOfficialAddress().getTown() != null) {
                    organization.getOfficialAddress().getTown().setAuthor(author);
                    Set<ConstraintViolation<Location>> locationViolations = validator.validate(organization.getOfficialAddress().getTown());
                    if (!locationViolations.isEmpty()) {
                        validationErrors.put("Location", formatViolationsToJson(locationViolations));
                    }
                }
            }
        }

        if (worker.getPerson() != null) {
            Person person = worker.getPerson();
            person.setAuthor(author);
            Set<ConstraintViolation<Person>> personViolations = validator.validate(person);
            if (!personViolations.isEmpty()) {
                validationErrors.put("Person", formatViolationsToJson(personViolations));
            }

            if (worker.getId() == null && person.getId() == null) {
                if (personRepository.findAll().stream()
                        .anyMatch(existingPerson -> existingPerson.getPassportID().equals(person.getPassportID()))) {
                    validationErrors.computeIfAbsent("Person", k -> new ArrayList<>())
                            .add("Person with passport ID '" + person.getPassportID() + "' already exists.");
                }
            } else if (worker.getId() != null && person.getId() != null) {
                if (personRepository.findAll().stream()
                        .filter(existingPerson -> !existingPerson.getId().equals(person.getId()))
                        .anyMatch(existingPerson -> existingPerson.getPassportID().equals(person.getPassportID()))) {
                    validationErrors.computeIfAbsent("Person", k -> new ArrayList<>())
                            .add("Person with passport ID '" + person.getPassportID() + "' already exists.");
                }
            }

            if (person.getEyeColor() != null) {
                List<String> validEnumValues = getEnumValues(Color.class);
                String eyeColor = person.getEyeColor();
                if (eyeColor != null && !validEnumValues.contains(eyeColor)) {
                    validationErrors.computeIfAbsent("Person", k -> new ArrayList<>())
                            .add("Eye color '" + eyeColor + "' is not valid.");
                }
            }
            if (person.getHairColor() != null) {
                List<String> validEnumValues = getEnumValues(Color.class);
                String hairColor = person.getHairColor();
                if (hairColor != null && !validEnumValues.contains(hairColor)) {
                    validationErrors.computeIfAbsent("Person", k -> new ArrayList<>())
                            .add("Hair color '" + hairColor + "' is not valid.");
                }
            }

            if (person.getLocation() != null) {
                person.getLocation().setAuthor(author);
                Set<ConstraintViolation<Location>> locationViolations = validator.validate(person.getLocation());
                if (!locationViolations.isEmpty()) {
                    validationErrors.put("Location (Person)", formatViolationsToJson(locationViolations));
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            Map<String, Map<String, List<String>>> finalErrors = new HashMap<>();
            finalErrors.put("ValidationErrors", validationErrors);

            ObjectMapper mapper = new ObjectMapper();

            String jsonErrors;
            try {
                jsonErrors = mapper.writeValueAsString(finalErrors);
            } catch (JsonProcessingException e) {
                throw new WebApplicationException("Failed to process data", Response.Status.BAD_REQUEST);
            }
            throw new WebApplicationException(jsonErrors, Response.Status.BAD_REQUEST);
        }
    }

    private List<String> getEnumValues(Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    private <T> List<String> formatViolationsToJson(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString() + ": " + violation.getMessage())
                .collect(Collectors.toList());
    }
}
