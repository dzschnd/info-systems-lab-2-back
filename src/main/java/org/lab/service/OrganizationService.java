package org.lab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.lab.model.Organization;
import org.lab.model.User;
import org.lab.repository.OrganizationRepository;

import java.util.List;

@Stateless
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }
}
