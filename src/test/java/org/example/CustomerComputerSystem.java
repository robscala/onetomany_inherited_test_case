package org.example;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="customer_computer_system")
@DiscriminatorValue("3")
public class CustomerComputerSystem extends ComputerSystem {
}
