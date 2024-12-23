package org.lab.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.lab.model.Action;
import org.lab.model.User;
import org.lab.repository.ActionRepository;
import java.util.List;

@ApplicationScoped
public class ActionService {
    @Inject
    private ActionRepository actionRepository;

    public List<Action> getAllImported(User user) {
        return actionRepository.findAllImported(user);
    }
}
