package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.lab.model.Organization;

import java.util.List;

@Stateless
public class OrganizationRepository extends GenericRepository<Organization, Integer> {
    public OrganizationRepository() {
        super(Organization.class);
    }

    @PersistenceContext
    private EntityManager entityManager;

    public List<Organization> findAllWithLock() {
        return entityManager.createQuery("SELECT o FROM Organization o", Organization.class)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }
}
