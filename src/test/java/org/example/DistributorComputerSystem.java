package org.example;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="distributor_computer_system")
@DiscriminatorValue("1")
public class DistributorComputerSystem extends ComputerSystem {
}
