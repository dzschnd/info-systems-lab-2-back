package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.lab.model.Coordinates;

@Stateless
public class CoordinatesRepository extends GenericRepository<Coordinates, Integer> {
    public CoordinatesRepository() {
        super(Coordinates.class);
    }

    @PersistenceContext
    private EntityManager entityManager;

    public Coordinates findByIdWithLock(Integer id) {
        return entityManager.createQuery("SELECT c FROM Coordinates c WHERE c.id = :id", Coordinates.class)
                .setParameter("id", id)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResult();
    }
}
