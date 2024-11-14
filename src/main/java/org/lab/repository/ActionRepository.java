package org.lab.repository;

import jakarta.ejb.Stateless;
import org.lab.model.Action;

@Stateless
public class ActionRepository extends GenericRepository<Action, Integer> {
    public ActionRepository() {
        super(Action.class);
    }
}