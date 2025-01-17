package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.LockModeType;
import org.lab.model.Coordinates;
import org.lab.model.Worker;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class WorkerRepository extends GenericRepository<Worker, Integer> {
    public WorkerRepository() {
        super(Worker.class);
    }

    public Worker findByIdWithRelations(Integer id) {
        String jpql = "SELECT w FROM Worker w " +
                "LEFT JOIN FETCH w.coordinates " +
                "LEFT JOIN FETCH w.organization " +
                "LEFT JOIN w.organization.officialAddress " +
                "LEFT JOIN w.organization.officialAddress.town " +
                "LEFT JOIN FETCH w.person " +
                "WHERE w.id = :id";
        TypedQuery<Worker> query = entityManager.createQuery(jpql, Worker.class);
        query.setParameter("id", id);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    public List<Worker> findAllWithRelations() {
        String jpql = "SELECT w FROM Worker w " +
                "LEFT JOIN FETCH w.coordinates " +
                "LEFT JOIN FETCH w.organization " +
                "LEFT JOIN w.organization.officialAddress " +
                "LEFT JOIN w.organization.officialAddress.town " +
                "LEFT JOIN FETCH w.person";
        return entityManager.createQuery(jpql, Worker.class).getResultList();
    }

    public long countByCoordinatesId(Integer coordinatesId) {
        return entityManager.createQuery(
                "SELECT COUNT(w) FROM Worker w WHERE w.coordinates.id = :coordinatesId", Long.class)
                .setParameter("coordinatesId", coordinatesId)
                .getSingleResult();
    }

    public long countByOrganizationId(Integer organizationId) {
        return entityManager.createQuery(
                "SELECT COUNT(w) FROM Worker w WHERE w.organization.id = :organizationId", Long.class)
                .setParameter("organizationId", organizationId)
                .getSingleResult();
    }

    public long countByPersonId(Integer personId) {
        return entityManager.createQuery(
                "SELECT COUNT(w) FROM Worker w WHERE w.person.id = :personId", Long.class)
                .setParameter("personId", personId)
                .getSingleResult();
    }

    public Worker findByIdWithLock(Integer id) {
        return entityManager.createQuery("SELECT c FROM Worker c WHERE c.id = :id", Worker.class)
                .setParameter("id", id)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResult();
    }
}
