package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import org.lab.model.*;

import java.util.List;

@Stateless
public class FileImportRepository extends GenericRepository<FileImport, Integer> {
    public FileImportRepository() {
        super(FileImport.class);
    }

    public List<FileImport> findAllByUser(User user) {
        TypedQuery<FileImport> query;
        if (user.getRole() == Role.ADMIN) {
            query = entityManager.createQuery(
                    "SELECT fi FROM FileImport fi", FileImport.class);
        } else {
            query = entityManager.createQuery(
                    "SELECT fi FROM FileImport fi WHERE fi.user = :user", FileImport.class);
            query.setParameter("user", user);
        }

        return query.getResultList();
    }
}
