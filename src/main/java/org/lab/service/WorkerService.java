package org.lab.service;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.lab.model.*;
import org.lab.repository.*;
import org.lab.utils.DuplicationAvoider;

import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class WorkerService {

    @Inject
    private WorkerRepository workerRepository;

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

    @Inject
    private ActionRepository actionRepository;

    @Inject
    private DuplicationAvoider duplicationAvoider;

    @Transactional
    public Worker createWorker(Worker worker, User author, boolean fromFileImport) {
        try {
            if (worker.getCoordinates() != null) {
                Coordinates coordinates = worker.getCoordinates();

                if (coordinates.getId() != null) {
                    Coordinates existingCoordinates = coordinatesRepository.findById(coordinates.getId());
                    if (existingCoordinates != null) {
                        worker.setCoordinates(existingCoordinates);
                    } else {
                        throw new IllegalArgumentException("Coordinates with ID " + coordinates.getId() + " not found.");
                    }
                } else {
                    coordinatesRepository.save(coordinates);
                }
            }

            if (worker.getOrganization() != null) {
                Organization organization = duplicationAvoider.avoidOrganizationFullNameDuplicates(worker.getOrganization());

                if (organization.getId() != null) {
                    Organization existingOrganization = organizationRepository.findById(organization.getId());
                    if (existingOrganization != null) {
                        worker.setOrganization(existingOrganization);
                    } else {
                        throw new IllegalArgumentException("Organization with ID " + organization.getId() + " not found.");
                    }
                } else {
                    if (organization.getOfficialAddress() != null) {
                        Address address = organization.getOfficialAddress();
                        if (address.getTown() != null) {
                            Location location = address.getTown();
                            locationRepository.save(location);
                        }
                        addressRepository.save(address);
                    }

                    organizationRepository.save(organization);
                }
            }

            if (worker.getPerson() != null) {
                Person person = duplicationAvoider.avoidPersonPassportDuplicates(worker.getPerson());

                if (person.getId() != null) {
                    Person existingPerson = personRepository.findById(person.getId());
                    if (existingPerson != null) {
                        worker.setPerson(existingPerson);
                    } else {
                        throw new IllegalArgumentException("Person with ID " + person.getId() + " not found.");
                    }
                } else {
                    if (person.getLocation() != null) {
                        Location location = person.getLocation();
                        locationRepository.save(location);
                    }

                    personRepository.save(person);
                }
            }
            Worker savedWorker = workerRepository.save(worker);

            Action action = new Action();
            action.setWorker(savedWorker);
            action.setUser(author);
            action.setActionType(ActionType.CREATE);
            action.setFromFileImport(fromFileImport);
            actionRepository.save(action);

            return savedWorker;
        }
        catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (Exception e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public Worker getWorkerById(Integer id) {
        return workerRepository.findByIdWithRelations(id);
    }

    public List<Worker> getAllWorkers() {
        return workerRepository.findAllWithRelations();
    }

    @Transactional
    public Worker updateWorker(Worker worker, boolean fromFileImport) {
        try {
            Worker existingWorker = workerRepository.findById(worker.getId());
            if (existingWorker == null) {
                throw new IllegalArgumentException("Worker with ID " + worker.getId() + " not found.");
            } else {
                if (worker.getCoordinates() != null) {
                    Coordinates updatedCoordinates = worker.getCoordinates();

                    if (updatedCoordinates.getId() != null) {
                        Coordinates existingCoordinates = coordinatesRepository.findById(updatedCoordinates.getId());
                        if (existingCoordinates == null) {
                            throw new IllegalArgumentException("Coordinates with ID " + updatedCoordinates.getId() + " not found.");
                        }
                        worker.setCoordinates(existingCoordinates);
                    } else {
                        updatedCoordinates.setId(existingWorker.getCoordinates().getId());
                        coordinatesRepository.update(updatedCoordinates);
                        worker.setCoordinates(existingWorker.getCoordinates());
                    }
                }
                if (worker.getOrganization() != null) {
                    Organization updatedOrganization = duplicationAvoider.avoidOrganizationFullNameDuplicates(worker.getOrganization());

                    if (updatedOrganization.getId() != null) {
                        Organization existingOrganization = organizationRepository.findById(updatedOrganization.getId());
                        if (existingOrganization == null) {
                            throw new IllegalArgumentException("Organization with ID " + updatedOrganization.getId() + " not found.");
                        }
                        worker.setOrganization(existingOrganization);
                    } else {
                        if (updatedOrganization.getOfficialAddress() != null) {
                            Address updatedAddress = updatedOrganization.getOfficialAddress();

                            if (updatedAddress.getTown() != null) {
                                Location updatedTown = updatedAddress.getTown();

                                updatedTown.setId(existingWorker.getOrganization().getOfficialAddress().getTown().getId());
                                locationRepository.update(updatedTown);
                                updatedAddress.setTown(existingWorker.getOrganization().getOfficialAddress().getTown());
                            }

                            updatedAddress.setId(existingWorker.getOrganization().getOfficialAddress().getId());
                            addressRepository.update(updatedAddress);
                            updatedOrganization.setOfficialAddress(existingWorker.getOrganization().getOfficialAddress());
                        }

                        updatedOrganization.setId(existingWorker.getOrganization().getId());
                        organizationRepository.update(updatedOrganization);
                        worker.setOrganization(existingWorker.getOrganization());
                    }
                } else {
                    Organization existingOrganization = existingWorker.getOrganization();

                    if (existingOrganization != null) {
                        Address address = existingOrganization.getOfficialAddress();
                        Location town = address.getTown();

                        if (town.getId() != null)
                            locationRepository.delete(town.getId());
                        if (address.getId() != null)
                            addressRepository.delete(address.getId());
                        organizationRepository.delete(existingOrganization.getId());;
                    }
                }

                if (worker.getPerson() != null) {
                    Person updatedPerson = duplicationAvoider.avoidPersonPassportDuplicates(worker.getPerson());

                    if (updatedPerson.getId() != null) {
                        Person existingPerson = personRepository.findById(updatedPerson.getId());
                        if (existingPerson == null) {
                            throw new IllegalArgumentException("Person with ID " + updatedPerson.getId() + " not found.");
                        }
                        worker.setPerson(existingPerson);
                    } else {
                        if (updatedPerson.getLocation() != null) {
                            Location updatedLocation = updatedPerson.getLocation();

                            updatedLocation.setId(existingWorker.getPerson().getLocation().getId());
                            locationRepository.update(updatedLocation);
                            updatedPerson.setLocation(existingWorker.getPerson().getLocation());
                        }

                        updatedPerson.setId(existingWorker.getPerson().getId());
                        personRepository.update(updatedPerson);
                        worker.setPerson(existingWorker.getPerson());
                    }
                }

                User updatingUser = worker.getAuthor();
                Worker updatedWorker = workerRepository.update(worker);

                Action action = new Action();
                action.setWorker(updatedWorker);
                action.setUser(updatingUser);
                action.setActionType(ActionType.UPDATE);
                action.setFromFileImport(fromFileImport);
                actionRepository.save(action);

                return updatedWorker;
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        }
    }

    @Transactional
    public void deleteWorker(Integer workerId) {
        try {
            Worker existingWorker = workerRepository.findById(workerId);
            if (existingWorker == null) {
                throw new IllegalArgumentException("Worker with ID " + workerId + " not found.");
            }

            Coordinates existingCoordinates = existingWorker.getCoordinates();

            long existingCoordinatesCount = 0;
            if (existingCoordinates != null) {
                existingCoordinatesCount = workerRepository.countByCoordinatesId(existingCoordinates.getId());
            }


            Organization existingOrganization = existingWorker.getOrganization();

            long existingOrganizationCount = 0;
            if (existingOrganization != null) {
                existingOrganizationCount = workerRepository.countByOrganizationId(existingOrganization.getId());
            }

            Person existingPerson = existingWorker.getPerson();

            long existingPersonCount = 0;
            if (existingPerson != null) {
                existingPersonCount = workerRepository.countByPersonId(existingPerson.getId());
            }

            workerRepository.delete(existingWorker.getId());

            if (existingCoordinatesCount == 1) {
                coordinatesRepository.delete(existingWorker.getCoordinates().getId());
            }

            if (existingOrganizationCount == 1) {
                Address address = existingOrganization.getOfficialAddress();
                organizationRepository.delete(existingOrganization.getId());

                if (address != null) {
                    Location town = address.getTown();
                    addressRepository.delete(address.getId());
                    if (town != null) {
                        locationRepository.delete(town.getId());
                    }
                }
            }

            if (existingPersonCount == 1) {
                Location location = existingPerson.getLocation();
                personRepository.delete(existingPerson.getId());
                if (existingPerson.getLocation() != null) {
                    locationRepository.delete(location.getId());
                }
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        }
    }
}
