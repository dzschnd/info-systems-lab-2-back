package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import org.lab.model.User;

@Stateless
public class UserRepository extends GenericRepository<User, Integer> {
    public UserRepository() {
        super(User.class);
    }
    public User findByUsername(String username) {
        try {
            return entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
