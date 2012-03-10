Quick Start Example
===================

Spring DBUnit provides integration between the Spring test framework and the DBUnit project.  In this example we show how to test a Hibernate JPA project using  a Hypersonic in-memory database.

Dependencies
============

This project will be built using Apache Maven.  Here is the complete POM file with all of the dependencies that we will need:


<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>example</groupId>
	<artifactId>spring-dbunit-example</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<properties>
		<spring.version>3.0.5.RELEASE</spring.version>
		<hibernate.core.version>3.5.6-Final</hibernate.core.version>
	</properties>
	<repositories>
		<repository>
			<id>jboss</id>
			<url>https://repository.jboss.org/nexus/content/groups/public/</url>
			<releases>
				<enabled>true</enabled>
			</releases>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</repository>
	</repositories>
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>2.3.1</version>
				<configuration>
					<compilerVersion>1.5</compilerVersion>
					<source>1.5</source>
				</configuration>
			</plugin>
		</plugins>
	</build>
	<dependencies>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context</artifactId>
			<version>${spring.version}</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-jdbc</artifactId>
			<version>${spring.version}</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-orm</artifactId>
			<version>${spring.version}</version>
		</dependency>
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-annotations</artifactId>
			<version>${hibernate.core.version}</version>
		</dependency>
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-entitymanager</artifactId>
			<version>${hibernate.core.version}</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-test</artifactId>
			<version>${spring.version}</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.dbunit</groupId>
			<artifactId>dbunit</artifactId>
			<version>2.4.8</version>
			<type>jar</type>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.test.dbunit</groupId>
			<artifactId>spring-test-dbunit</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-log4j12</artifactId>
			<version>1.5.2</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.8.1</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.hsqldb</groupId>
			<artifactId>hsqldb</artifactId>
			<version>2.0.0</version>
			<scope>test</scope>
		</dependency>
	</dependencies>
</project>


Entity
======

For this simple project we will create a single “Person” Entity with attributes for their title, first name and last name.  We will also declare a query that we will make use of later.

package example.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({ @NamedQuery(name = "Person.find", query = "SELECT p from Person p where p.firstName like :name "
		+ "or p.lastName like :name") })
public class Person {

	@Id
	private int id;

	private String title;
	
	private String firstName;
	
	private String lastName;

	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}

We also need to setup a persistence.xml file for hibernate to use:

<persistence xmlns="http://java.sun.com/xml/ns/persistence"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"
	version="2.0">
	<persistence-unit name="pagingDatabase"
		transaction-type="RESOURCE_LOCAL">
		<provider>org.hibernate.ejb.HibernatePersistence</provider>
		<class>example.entity.Person</class>
		<properties>
			<property name="hibernate.dialect" value="org.hibernate.dialect.HSQLDialect" />
			<property name="hibernate.hbm2ddl.auto" value="create-drop" />
			<property name="hibernate.show_sql" value="true" />
			<property name="hibernate.cache.provider_class" value="org.hibernate.cache.HashtableCacheProvider" />
		</properties>
	</persistence-unit>
</persistence>


Service
=======

We need to have something to test so we’ll create a simple service that lets us search our Person entity using the named query from above:


package example.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.entity.Person;

@Service
@Transactional
public class PersonService {

	@PersistenceContext
	private EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public List<Person> find(String name) {
		Query query = entityManager.createNamedQuery("Person.find");
		query.setParameter("name", "%"+name+"%");
		return query.getResultList();
	}
}


Testing
=======

To make sure that our service is working we need to create a JUnit test.  Here is the basic structure of the test class:


package example.service;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.dbunit.DbUnitTestExecutionListener;
import org.springframework.test.dbunit.annotation.DatabaseSetup;
import org.springframework.test.dbunit.annotation.ExpectedDatabase;

import example.entity.Person;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
public class PersonServiceTest {

	@Autowired
	private PersonService personService;
}


As the test is using the SpringJUnit4ClassRunner we also need a context xml configuration file.  Here we tell spring to scan for beans and we also setup Hibernate and the in-memory database.

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:tx="http://www.springframework.org/schema/tx"
	xsi:schemaLocation="
	http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd
	http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-3.0.xsd">

	<context:component-scan base-package="example" />
	<tx:annotation-driven transaction-manager="transactionManager" />
	
	<bean id="dataSource"
		class="org.springframework.jdbc.datasource.DriverManagerDataSource">
		<property name="driverClassName" value="org.hsqldb.jdbcDriver" />
		<property name="url" value="jdbc:hsqldb:mem:paging" />
		<property name="username" value="sa" />
		<property name="password" value="" />
	</bean>

	<bean id="entityManagerFactory"
		class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
		<property name="dataSource" ref="dataSource" />
		<property name="jpaDialect">
			<bean class="org.springframework.orm.jpa.vendor.HibernateJpaDialect"/>
		</property>
	</bean>
	
	<bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager">
		<property name="entityManagerFactory" ref="entityManagerFactory" />
	</bean>
</beans>


We can now create a test method to check that the service can find entities:

@Test
public void testFind() throws Exception {
	List<Person> personList = personService.find("hil");
	assertEquals(1, personList.size());
	assertEquals("Phillip", personList.get(0).getFirstName());
}

At this point the test should fail because the returned personList contains 0 items.  To make sure that the test passes we need to insert some database data.  This is where DBUnit comes in.  You may have noticed that the TestExecutionListeners annotation includes a reference to DbUnitTestExecutionListener.  This means that we can use the @DatabaseSetup annotation on the test method to configure the database from a flat XML file.  The xml file follows the standard DBUnit conventions:

<?xml version="1.0" encoding="UTF-8"?>
<dataset>
	<Person id="0" title="Mr" firstName="Phillip" lastName="Webb"/>
	<Person id="1" title="Mr" firstName="Fred" lastName="Bloggs"/>
</dataset>


@Test
@DatabaseSetup("sampleData.xml")
public void testFind() throws Exception {
	List<Person> personList = personService.find("hil");
	assertEquals(1, personList.size());
	assertEquals("Phillip", personList.get(0).getFirstName());
}

DBUnit should now execute before the test method runs to insert appropriate data into the database.  As a result the test should pass.

Testing expected data
=====================

As well as configuring the database before a test runs it is also possible to verify database set after the test completes.  Let’s update our service with a method to remove entities:

...
public class PersonService {
...
	public void remove(int personId) {
		Person person = entityManager.find(Person.class, personId);
		entityManager.remove(person);
	}
...

The method to test if remove works can use the @ExpectedDatabase annotation.  This will use DBUnit to ensure that the database contains expected data after the test method has finshed.  

@Test
@DatabaseSetup("sampleData.xml")
@ExpectedDatabase("expectedData.xml")
public void testRemove() throws Exception {
	personService.remove(1);
}

The sampleData.xml file is shown below:

<?xml version="1.0" encoding="UTF-8"?>
<dataset>
	<Person id="0" title="Mr" firstName="Phillip" lastName="Webb"/>
</dataset>


Summary
=======

This quick introduction has shown how Spring DBUnit can be used to setup a database and verify expected content using simple annotations.

