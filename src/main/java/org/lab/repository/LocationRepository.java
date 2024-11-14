package org.lab.repository;

import jakarta.ejb.Stateless;
import org.lab.model.Location;

@Stateless
public class LocationRepository extends GenericRepository<Location, Integer> {
    public LocationRepository() {
        super(Location.class);
    }
}
