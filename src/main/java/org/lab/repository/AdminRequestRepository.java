package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import org.lab.model.AdminRequest;
import org.lab.model.RequestStatus;

import java.util.List;

@Stateless
public class AdminRequestRepository extends GenericRepository<AdminRequest, Integer> {
    public AdminRequestRepository() {
        super(AdminRequest.class);
    }
    public List<AdminRequest> findAllByStatus(RequestStatus status) {
        TypedQuery<AdminRequest> query = entityManager.createQuery(
                "SELECT r FROM AdminRequest r WHERE r.status = :status", AdminRequest.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
}