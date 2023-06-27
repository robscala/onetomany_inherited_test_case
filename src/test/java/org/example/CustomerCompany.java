package org.example;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@DiscriminatorValue("9")
@Table(name = "customer_company")
public class CustomerCompany extends Company {
    @OneToMany(mappedBy = "owner", orphanRemoval = true, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<CustomerComputerSystem> computerSystems = new ArrayList<>();

    public void addComputerSystem(CustomerComputerSystem computerSystem) {
        computerSystems.add(computerSystem);
        computerSystem.setOwner(this);
    }

    public List<CustomerComputerSystem> getComputerSystems() {
        return computerSystems;
    }
}
