package org.example;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@DiscriminatorValue("2")
@Table(name="distributor_company")
public class DistributorCompany extends Company {
    @OneToMany(mappedBy = "owner", orphanRemoval = true, cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<DistributorComputerSystem> computerSystems = new ArrayList<>();

    public List<DistributorComputerSystem> getComputerSystems() {
        return computerSystems;
    }

    public void addComputerSystem(DistributorComputerSystem computerSystem) {
        computerSystems.add(computerSystem);
        computerSystem.setOwner(this);
    }
}
