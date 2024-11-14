package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import org.lab.model.Organization;
import org.lab.model.User;

import java.util.List;

@Stateless
public class OrganizationRepository extends GenericRepository<Organization, Integer> {
    public OrganizationRepository() {
        super(Organization.class);
    }
}
