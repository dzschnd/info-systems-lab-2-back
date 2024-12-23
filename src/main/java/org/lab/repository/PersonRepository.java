package org.lab.repository;

import jakarta.ejb.Stateless;
import org.lab.model.Person;

@Stateless
public class PersonRepository extends GenericRepository<Person, Integer> {
    public PersonRepository() {
        super(Person.class);
    }
}

