package org.lab.utils;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.lab.model.Organization;
import org.lab.model.Person;
import org.lab.repository.OrganizationRepository;
import org.lab.repository.PersonRepository;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class DuplicationAvoider {

    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private PersonRepository personRepository;

    public Organization avoidOrganizationFullNameDuplicates(Organization organization) {
        Set<String> existingFullNames = organizationRepository.findAll().stream()
                    .filter(existingOrganization -> organization.getId() == null || !existingOrganization.getId().equals(organization.getId()))
                    .map(Organization::getFullName)
                    .collect(Collectors.toSet());

        String modifiedFullName = organization.getFullName();
        int i = 1;
        while (existingFullNames.contains(modifiedFullName)) {
            modifiedFullName = organization.getFullName() + " (" + i + ")";
            i++;
        }
        organization.setFullName(modifiedFullName);

        return organization;
    }

    public Person avoidPersonPassportDuplicates(Person person) {
        Set<String> existingPassportIDs = personRepository.findAll().stream()
                .filter(existingPerson -> person.getId() == null || !existingPerson.getId().equals(person.getId()))
                .map(Person::getPassportID)
                .collect(Collectors.toSet());

        String originalPassportID = person.getPassportID();
        String newPassportID = originalPassportID;
        int i = 1;

        while (existingPassportIDs.contains(newPassportID)) {
            newPassportID = originalPassportID + " (" + i + ")";
            i++;
        }

        person.setPassportID(newPassportID);

        return person;
    }
}
