package org.lab.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.lab.model.*;
import org.lab.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class WorkerValidator {
    @Inject
    WorkerRepository workerRepository;

    @Inject
    private CoordinatesRepository coordinatesRepository;

    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private AddressRepository addressRepository;

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private PersonRepository personRepository;

    @Transactional
    public Worker validate(Worker worker, User author, Validator validator) throws WebApplicationException {
        Map<String, List<String>> validationErrors = new HashMap<>();

        if (worker.getId() == null) {
            worker.setAuthor(author);
        } else {
            Worker existingWorker = workerRepository.findById(worker.getId());
            if (existingWorker == null) {
                validationErrors.computeIfAbsent("Worker", k -> new ArrayList<>())
                        .add("Worker with id " + worker.getId() + " does not exist");
            } else {
                worker.setAuthor(existingWorker.getAuthor());
            }
        }

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
            if (worker.getCoordinates().getId() == null) {
                worker.getCoordinates().setAuthor(author);
            } else {
                Coordinates existingCoordinates = coordinatesRepository.findById(worker.getCoordinates().getId());
                if (existingCoordinates == null) {
                    validationErrors.computeIfAbsent("Coordinates", k -> new ArrayList<>())
                            .add("Coordinates with id " + worker.getCoordinates().getId() + " does not exist");
                } else {
                    worker.getCoordinates().setAuthor(existingCoordinates.getAuthor());
                }
            }
            Set<ConstraintViolation<Coordinates>> coordinatesViolations = validator.validate(worker.getCoordinates());
            if (!coordinatesViolations.isEmpty()) {
                validationErrors.put("Coordinates", formatViolationsToJson(coordinatesViolations));
            }
        }

        if (worker.getOrganization() != null) {
            Organization organization = worker.getOrganization();
            if (organization.getId() == null) {
                organization.setAuthor(author);
            } else {
                Organization existingOrganization = organizationRepository.findById(organization.getId());
                if (existingOrganization == null) {
                    validationErrors.computeIfAbsent("Organization", k -> new ArrayList<>())
                            .add("Organization with id " + worker.getCoordinates().getId() + " does not exist");
                } else {
                    organization.setAuthor(existingOrganization.getAuthor());
                }
            }
            Set<ConstraintViolation<Organization>> organizationViolations = validator.validate(organization);
            if (!organizationViolations.isEmpty()) {
                validationErrors.put("Organization", formatViolationsToJson(organizationViolations));
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
                if (organization.getOfficialAddress().getId() == null) {
                    organization.getOfficialAddress().setAuthor(author);
                } else {
                    Address existingAddress = addressRepository.findById(organization.getOfficialAddress().getId());
                    if (existingAddress == null) {
                        validationErrors.computeIfAbsent("Address", k -> new ArrayList<>())
                                .add("Address with id " + worker.getCoordinates().getId() + " does not exist");
                    } else {
                        organization.getOfficialAddress().setAuthor(existingAddress.getAuthor());
                    }
                }
                Set<ConstraintViolation<Address>> addressViolations = validator.validate(organization.getOfficialAddress());
                if (!addressViolations.isEmpty()) {
                    validationErrors.put("Address", formatViolationsToJson(addressViolations));
                }

                if (organization.getOfficialAddress().getTown() != null) {
                    if (organization.getOfficialAddress().getTown().getId() == null) {
                        organization.getOfficialAddress().getTown().setAuthor(author);
                    } else {
                        Location existingTown = locationRepository.findById(organization.getOfficialAddress().getTown().getId());
                        if (existingTown == null) {
                            validationErrors.computeIfAbsent("Town", k -> new ArrayList<>())
                                    .add("Town with id " + worker.getCoordinates().getId() + " does not exist");
                        } else {
                            organization.getOfficialAddress().getTown().setAuthor(existingTown.getAuthor());
                        }
                    }
                    Set<ConstraintViolation<Location>> locationViolations = validator.validate(organization.getOfficialAddress().getTown());
                    if (!locationViolations.isEmpty()) {
                        validationErrors.put("Location", formatViolationsToJson(locationViolations));
                    }
                }
            }
        }

        if (worker.getPerson() != null) {
            Person person = worker.getPerson();
            if (worker.getPerson().getId() == null) {
                person.setAuthor(author);
            } else {
                Person existingPerson = personRepository.findById(worker.getPerson().getId());
                if (existingPerson == null) {
                    validationErrors.computeIfAbsent("Person", k -> new ArrayList<>())
                            .add("Person with id " + worker.getCoordinates().getId() + " does not exist");
                } else {
                    person.setAuthor(existingPerson.getAuthor());
                }
            }
            Set<ConstraintViolation<Person>> personViolations = validator.validate(person);
            if (!personViolations.isEmpty()) {
                validationErrors.put("Person", formatViolationsToJson(personViolations));
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
                if (person.getLocation().getId() == null) {
                    person.getLocation().setAuthor(author);
                } else {
                    Location existingLocation = locationRepository.findById(person.getLocation().getId());
                    if (existingLocation == null) {
                        validationErrors.computeIfAbsent("Location", k -> new ArrayList<>())
                                .add("Location with id " + worker.getCoordinates().getId() + " does not exist");
                    } else {
                        person.getLocation().setAuthor(existingLocation.getAuthor());
                    }
                }
                Set<ConstraintViolation<Location>> locationViolations = validator.validate(person.getLocation());
                if (!locationViolations.isEmpty()) {
                    validationErrors.put("Town", formatViolationsToJson(locationViolations));
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

        return worker;
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
