package org.lab.service;

import io.minio.MinioClient;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.jdbc.Work;
import org.lab.model.*;
import org.lab.repository.*;
import org.lab.utils.DuplicationAvoider;
import org.lab.utils.MinioUtils;
import org.postgresql.util.PSQLException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Stateless
public class WorkerService {

    @PersistenceContext
    private EntityManager entityManager;

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
    private FileImportRepository fileImportRepository;

    @Inject
    private DuplicationAvoider duplicationAvoider;

    public Worker getWorkerById(Integer id) {
        return workerRepository.findByIdWithRelations(id);
    }

    public List<Worker> getAllWorkers() {
        return workerRepository.findAllWithRelations();
    }

    @Transactional
    public List<Worker> handleImportedWorker(List<Worker> workers, User user, MinioClient minioClient,
                                             String fileName, InputStream fileInputStream) {
        String finalFileName = null;
            try {
                try (Session session = entityManager.unwrap(Session.class)) {
                    session.doWork(connection -> connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED));

                    FileImport fileImport = new FileImport();
                    fileImport.setFileName(fileName);
                    fileImport.setUser(user);
                    fileImportRepository.save(fileImport);

                    for (int i = 0; i < workers.size(); i++) {
                        Worker handledWorker = workers.get(i);
                        boolean isCreateRequest = handledWorker.getId() == null;

                        handledWorker = isCreateRequest ? create(handledWorker) : update(handledWorker);

                        saveAction(user, handledWorker, isCreateRequest, fileImport);

                        workers.set(i, handledWorker);
                    }

                    finalFileName = MinioUtils.appendId(fileName, fileImport.getId());
                    MinioUtils.upload(minioClient, finalFileName, fileInputStream);

                    return workers;
                }
            } catch (WebApplicationException e) {
                if (finalFileName != null) {
                    MinioUtils.rollbackUpload(minioClient, finalFileName);
                }
                throw e;
            }
    }

    @Transactional
    public Worker createWorker(Worker worker, User user) throws WebApplicationException {
        Worker savedWorker = create(worker);
        saveAction(user, savedWorker, true, null);

        return savedWorker;
    }

    @Transactional
    public Worker updateWorker(Worker worker, User user) throws WebApplicationException {
        Worker updatedWorker = update(worker);
        saveAction(user, updatedWorker, false, null);

        return updatedWorker;
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

    public Worker update(Worker worker) {
        try {
            Worker existingWorker = workerRepository.findByIdWithLock(worker.getId());
            if (worker.getCoordinates() != null) {
                Coordinates updatedCoordinates = worker.getCoordinates();

                if (updatedCoordinates.getId() != null) {
                    Coordinates existingCoordinates = coordinatesRepository.findByIdWithLock(updatedCoordinates.getId());
                    if (existingCoordinates == null) {
                        throw new IllegalArgumentException("Coordinates with ID " + updatedCoordinates.getId() + " not found.");
                    }
                    worker.setCoordinates(existingCoordinates);
                } else {
                    updatedCoordinates.setId(existingWorker.getCoordinates().getId());
                    System.out.println("CURRENT COORDINATES: (" + coordinatesRepository.findById(updatedCoordinates.getId()).getX() + "," + coordinatesRepository.findById(updatedCoordinates.getId()).getY() + ")");
                    if (updatedCoordinates.getX() == null) {
                        updatedCoordinates.setX(coordinatesRepository.findByIdWithLock(updatedCoordinates.getId()).getX());
                        coordinatesRepository.update(updatedCoordinates);
                    }
                    if (updatedCoordinates.getY() == null) {
                        updatedCoordinates.setY(coordinatesRepository.findByIdWithLock(updatedCoordinates.getId()).getY());
                        coordinatesRepository.update(updatedCoordinates);
                    }
                    System.out.println("UPDATED COORDINATES: (" + updatedCoordinates.getX() + "," + updatedCoordinates.getY() + ")");

                    worker.setCoordinates(updatedCoordinates);
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

                            if (existingWorker.getOrganization() != null) {
                                updatedTown.setId(existingWorker.getOrganization().getOfficialAddress().getTown().getId());
                                locationRepository.update(updatedTown);
                            } else {
                                locationRepository.save(updatedTown);
                            }
                            updatedAddress.setTown(updatedTown);
                        }

                        if (existingWorker.getOrganization() != null) {
                            updatedAddress.setId(existingWorker.getOrganization().getOfficialAddress().getId());
                            addressRepository.update(updatedAddress);
                        } else {
                            addressRepository.save(updatedAddress);
                        }
                        updatedOrganization.setOfficialAddress(updatedAddress);
                    }

                    if (existingWorker.getOrganization() != null) {
                        updatedOrganization.setId(existingWorker.getOrganization().getId());
                        organizationRepository.update(updatedOrganization);
                    } else {
                        organizationRepository.save(updatedOrganization);
                    }
                    worker.setOrganization(updatedOrganization);
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
                    organizationRepository.delete(existingOrganization.getId());
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

            return workerRepository.update(worker);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        }
    }

    public Worker create(Worker worker) {
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
                    Organization organization = worker.getOrganization();

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

                        Organization uniqueOrganization = duplicationAvoider.avoidOrganizationFullNameDuplicates(organization);
                        organizationRepository.save(uniqueOrganization);
                    }
                }

                if (worker.getPerson() != null) {
                    Person person = worker.getPerson();

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

                        Person uniquePerson = duplicationAvoider.avoidPersonPassportDuplicates(person);
                        personRepository.save(uniquePerson);
                    }
                }
                return workerRepository.save(worker);
            } catch (IllegalArgumentException e) {
                throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
            }
    }

    public void saveAction(User user, Worker handledWorker, boolean isCreateRequest, FileImport fileImport) {
        Action action = new Action();
        action.setWorker(handledWorker);
        action.setUser(user);
        action.setActionType(isCreateRequest ? ActionType.CREATE : ActionType.UPDATE);
        if (fileImport != null) action.setFileImport(fileImport);
        actionRepository.save(action);
    }
}
