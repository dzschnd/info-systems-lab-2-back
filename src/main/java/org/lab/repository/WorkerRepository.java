package org.lab.repository;

import jakarta.ejb.Stateless;
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
                "LEFT JOIN FETCH w.organization " +  // Removed alias 'o'
                "LEFT JOIN w.organization.officialAddress " + // Removed alias 'a'
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
                "LEFT JOIN FETCH w.organization " +  // Removed alias
                "LEFT JOIN w.organization.officialAddress " + // Removed alias
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
}
