package org.lab.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.lab.model.FileImport;
import org.lab.model.User;
import org.lab.repository.FileImportRepository;

import java.util.List;

@ApplicationScoped
public class FileImportService {
    @Inject
    private FileImportRepository fileImportRepository;

    public List<FileImport> getAllByUser(User user) {
        return fileImportRepository.findAllByUser(user);
    }
}
