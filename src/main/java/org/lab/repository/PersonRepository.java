package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import org.lab.model.Person;
import org.lab.model.User;

import java.util.List;

@Stateless
public class PersonRepository extends GenericRepository<Person, Integer> {
    public PersonRepository() {
        super(Person.class);
    }
}

