package org.lab.repository;

import jakarta.ejb.Stateless;
import org.lab.model.Organization;

@Stateless
public class OrganizationRepository extends GenericRepository<Organization, Integer> {
    public OrganizationRepository() {
        super(Organization.class);
    }
}
