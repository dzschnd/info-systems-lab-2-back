package org.lab.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import org.lab.model.AdminRequest;
import org.lab.model.Coordinates;
import org.lab.model.RequestStatus;
import org.lab.model.User;

import java.util.List;

@Stateless
public class CoordinatesRepository extends GenericRepository<Coordinates, Integer> {
    public CoordinatesRepository() {
        super(Coordinates.class);
    }
}
