package org.lab.repository;

import jakarta.ejb.Stateless;
import org.lab.model.Coordinates;

@Stateless
public class CoordinatesRepository extends GenericRepository<Coordinates, Integer> {
    public CoordinatesRepository() {
        super(Coordinates.class);
    }
}
