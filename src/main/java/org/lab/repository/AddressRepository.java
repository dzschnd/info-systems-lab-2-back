package org.lab.repository;

import jakarta.ejb.Stateless;
import org.lab.model.Address;

@Stateless
public class AddressRepository extends GenericRepository<Address, Integer> {
    public AddressRepository() {
        super(Address.class);
    }
}
