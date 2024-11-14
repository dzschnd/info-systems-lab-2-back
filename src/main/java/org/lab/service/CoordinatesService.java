package org.lab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.lab.model.Coordinates;
import org.lab.model.User;
import org.lab.repository.CoordinatesRepository;

import java.util.List;

@Stateless
public class CoordinatesService {
    @Inject
    private CoordinatesRepository coordinatesRepository;

    public List<Coordinates> getAllCoordinates() {
        return coordinatesRepository.findAll();
    }
}
