package org.lab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.lab.model.Person;
import org.lab.model.User;
import org.lab.repository.PersonRepository;

import java.util.List;

@Stateless
public class PersonService {

    @Inject
    private PersonRepository personRepository;

    public List<Person> getAllPerson() {
        return personRepository.findAll();
    }
}
