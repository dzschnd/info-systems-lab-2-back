package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import org.lab.model.*;

import java.util.List;

@Stateless
public class ActionRepository extends GenericRepository<Action, Integer> {
    public ActionRepository() {
        super(Action.class);
    }
}