package org.example;

import java.io.CharArrayWriter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.schema.SourceType;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToWriter;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.JpaTargetAndSourceDescriptor;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.hibernate.tool.schema.spi.SchemaMigrator;
import org.hibernate.tool.schema.spi.ScriptSourceInput;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class JPAUnitTestCase
{
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void init() {

        // Perform initialization by creating all the tables using native queries, and populating those tables.

        entityManagerFactory = Persistence.createEntityManagerFactory( "templatePU" );
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        createTablesWithNativeQueries(entityManager);
        populateTablesWithJPA(entityManager);
        entityManager.close();
    }

    @After
    public void destroy() {
        entityManagerFactory.close();
    }

    @Test
    public void schemaTest() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        // Test retrieval of entities.
        CustomerCompany customer1 = entityManager.find(CustomerCompany.class, 1);
        assertEquals("Customer1 ID", 1L, customer1.id);
        List<CustomerComputerSystem> customerComputerSystems = customer1.getComputerSystems();
        assertEquals("Computer systems size", 1, customerComputerSystems.size());

        DistributorCompany distributor1 = entityManager.find(DistributorCompany.class, 2);
        assertEquals("Distributor1 ID", 2L, distributor1.id);
//        List<DistributorComputerSystem> distributorComputerSystems = distributor1.getComputerSystems();
//        assertEquals("Distributor systems size", 2, distributorComputerSystems.size());

        // Describe the current tables
        describeTable("company", entityManager);
        describeTable("computer_system", entityManager);
        describeTable("customer_company", entityManager);
        describeTable("distributor_company", entityManager);
        describeTable("customer_computer_system", entityManager);
        describeTable("distributor_computer_system", entityManager);

        // Create an evolve script.
        String evolveScript = getEvolveScript();
        System.out.println("---- The evolve script ----");
        System.out.print(evolveScript);

        assertEquals("EvolveScript should be empty", "", evolveScript);
    }

    private String getEvolveScript() {
        Properties connectionProperties = new Properties();

        connectionProperties.put("hibernate.connection.url", "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1");
        connectionProperties.put("hibernate.connection.username", "sa");
        connectionProperties.put("hibernate.integrator_provider", (IntegratorProvider) () -> Collections.singletonList(
                MetadataExtractorIntegrator.INSTANCE
        ));

        // Cause the SessionFactory to be created.  This will build the Metadata.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("templatePU", connectionProperties);

        // Get the SchemaManagementTool and the SchemaMigrator.
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(connectionProperties).build();
        SchemaManagementTool schemaManagementTool = serviceRegistry.getService(SchemaManagementTool.class );
        SchemaMigrator schemaMigrator = schemaManagementTool.getSchemaMigrator(
                propertiesToStringMap(connectionProperties));

        // Set the SchemaMigrator's options and run it.
        Map<String,Object> schemaMigrationOptions = new HashMap<>();
        ExecutionOptions executionOptions = SchemaManagementToolCoordinator.buildExecutionOptions(
                schemaMigrationOptions,
                null
        );
        CharArrayWriter writer = new CharArrayWriter();
        Metadata metadata = MetadataExtractorIntegrator.INSTANCE.getMetadata();
        TargetDescriptor targetDescriptor = new JpaTargetAndSourceDescriptor() {
            @Override public SourceType getSourceType()                 { return null; }
            @Override public ScriptSourceInput getScriptSourceInput()   { return null; }
            @Override public EnumSet<TargetType> getTargetTypes()       { return EnumSet.of(TargetType.SCRIPT); }
            @Override public ScriptTargetOutput getScriptTargetOutput() { return new ScriptTargetOutputToWriter(writer); }
        };
        schemaMigrator.doMigration(metadata, executionOptions, ContributableMatcher.ALL, targetDescriptor);
        emf.close();
        return writer.toString();
    }

    /**
     * Create one CustomerCompany and one DistributorCompany.
     * Add a CustomerComputerSystem to the CustomerCompany
     * Add two DistributorComputerSystems to the DistributorCompany
     * @param entityManager
     */
    private void populateTablesWithJPA(EntityManager entityManager) {
        // Create companies
        entityManager.getTransaction().begin();
        CustomerCompany customerCompany1 = new CustomerCompany();
        DistributorCompany distributorCompany1 = new DistributorCompany();
        entityManager.persist(customerCompany1);
        entityManager.persist(distributorCompany1);
        entityManager.getTransaction().commit();

        // Create computer systems
        entityManager.getTransaction().begin();
        customerCompany1.addComputerSystem(new CustomerComputerSystem());
        distributorCompany1.addComputerSystem(new DistributorComputerSystem());
        distributorCompany1.addComputerSystem(new DistributorComputerSystem()); // Add a second computer system for distributor company.
        entityManager.getTransaction().commit();
    }

    private void createTablesWithNativeQueries(EntityManager entityManager) {
        entityManager.getTransaction().begin();
        entityManager.createNativeQuery(
                "create table company (\n" +
                        "        CompanyType integer not null,\n" +
                        "        id bigint generated by default as identity,\n" +
                        "        primary key (id)\n" +
                        "    )"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "create table computer_system (\n" +
                        "        ComputerSystemType integer not null,\n" +
                        "        OWNER_ID bigint,\n" +
                        "        id bigint generated by default as identity,\n" +
                        "        primary key (id)\n" +
                        "    )"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "create table customer_company (\n" +
                        "        id bigint not null,\n" +
                        "        primary key (id)\n" +
                        "    )"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "create table customer_computer_system (\n" +
                        "        id bigint not null,\n" +
                        "        primary key (id)\n" +
                        "    )"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "create table distributor_company (\n" +
                        "        id bigint not null,\n" +
                        "        primary key (id)\n" +
                        "    )"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "create table distributor_computer_system (\n" +
                        "        id bigint not null,\n" +
                        "        primary key (id)\n" +
                        "    )"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "alter table if exists computer_system \n" +
                        "       add constraint FKce9lt5npgkfcq5tw4shpma2o \n" +
                        "       foreign key (OWNER_ID) \n" +
                        "       references company"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "alter table if exists customer_company \n" +
                        "       add constraint FKsqr49lri4ewqfajw6a8pgvf8x \n" +
                        "       foreign key (id) \n" +
                        "       references company"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "alter table if exists customer_computer_system \n" +
                        "       add constraint FK92k74polx6hnadp8gq7fn68xe \n" +
                        "       foreign key (id) \n" +
                        "       references computer_system"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "alter table if exists distributor_company \n" +
                        "       add constraint FK4u0j068jxs579m1x23w17fpge \n" +
                        "       foreign key (id) \n" +
                        "       references company"
        ).executeUpdate();
        entityManager.createNativeQuery(
                "alter table if exists distributor_computer_system \n" +
                        "       add constraint FK7ai1d0k2orq195fpyu4pp8hi8 \n" +
                        "       foreign key (id) \n" +
                        "       references computer_system"
        ).executeUpdate();
        entityManager.getTransaction().commit();

    }

    private void describeTable(String tableName, EntityManager entityManager) {
        String sql = "show columns from " + tableName;
        Query query = entityManager.createNativeQuery(sql);
        StringBuilder sb = new StringBuilder();
        sb.append("---- " + sql + " ----\n");
        sb.append("field              | type            | null | key | default\n");
        for (Object object : query.getResultList()) {
            Object[] row = (Object[]) object;
            sb.append(String.format("%-18s | %-15s | %-4s | %-3s | %s\n", row[0], row[1], row[2], row[3], row[4]));
        }
        System.out.println(sb);
    }

    private static Map<String,Object> propertiesToStringMap(Properties properties) {
        Map<String,Object> map = new HashMap<>();
        for (Map.Entry<Object,Object> entry : properties.entrySet())
            map.put((String) entry.getKey(), entry.getValue());
        return map;
    }
}