package org.example;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="computer_system")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="ComputerSystemType", discriminatorType=DiscriminatorType.INTEGER)
public abstract class ComputerSystem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID", foreignKey = @ForeignKey())
    protected Company owner = null;

    public void setOwner(Company owner) {
        this.owner = owner;
    }
}