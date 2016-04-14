# Frequently Asked Questions

## Is there an easy way to specify the Oracle database schema name to use?
You will need to use a custom dbUnitDatabaseConnection bean if you want to specify a schema.  Something like:

    <bean id="dbUnitDatabaseConnection" class="org.springframework.test.dbunit.bean.DatabaseDataSourceConnectionFactoryBean">
        <property name="schema" ref="myschema"/>
    </bean>

This specified schema will be passed to the org.dbunit.database.DatabaseConnection [constructor](http://www.dbunit.org/apidocs/org/dbunit/database/DatabaseConnection.html#DatabaseConnection%28java.sql.Connection,%20java.lang.String%29).

## Is this project in a Maven repository?
Yes it is. see http://mvnrepository.com/artifact/com.github.springtestdbunit/spring-test-dbunit for details.

## Is there a way to clear out the Database when tests are complete?
The recommendation from DBUnit is that you have a good database setup and don't cleanup (see http://www.dbunit.org/bestpractices.html#nocleanup).  That being said there are occasions where you might want to cleanup your database after every test.

There are a couple of strategies that you can use:

1) Use the Spring TransactionalTestExecutionListener and rollback after each test.  See the section on Transactions in the project [readme](index.html).  This approach can work work for many tests, however, sometimes you may not want to rollback transactions to be sure that no exceptions would have been raised on the commit.  For more information about Spring testing with JDBC see the [Spring Reference Documentation](http://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/html/testing.html#integration-testing-support-jdbc).

2) Use the @DatabaseTearDown annotation on the test class and provide a reset DBUnit XML file.  Creating a reset script can often be difficult if you have foreign key constraints (see
[this blog post](http://www.andrewspencer.net/2011/solve-foreign-key-problems-in-dbunit-test-data/)).  Any empty table element tells DBUnit to delete all data, so a reset script is generally a list of tables in the order that they can be deleted without causing foreign key constraints, e.g.:

    <address/>
    <custom/>

