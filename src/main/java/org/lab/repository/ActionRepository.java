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

    public List<Action> findAllImported(User user) {
        TypedQuery<Action> query;
        if (user.getRole() == Role.ADMIN) {
            query = entityManager.createQuery(
                    "SELECT r FROM Action r WHERE r.fromFileImport = :fromFileImport", Action.class);
            query.setParameter("fromFileImport", true);
        } else {
            query = entityManager.createQuery(
                    "SELECT r FROM Action r WHERE r.fromFileImport = :fromFileImport AND r.user = :user", Action.class);
            query.setParameter("fromFileImport", true);
            query.setParameter("user", user);
        }

        return query.getResultList();
    }
}