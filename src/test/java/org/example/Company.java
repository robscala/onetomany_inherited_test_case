package org.example;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name="company")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="CompanyType", discriminatorType=DiscriminatorType.INTEGER)
public abstract class Company {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    protected long id;
}
