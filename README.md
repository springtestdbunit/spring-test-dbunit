Introduction
============
Spring DBUnit provides integration between the Spring testing framework and the popular DBUnit project. It allows you
to setup and teardown database tables using simple annotations as well as checking expected table contents once a test
completes.

The project can be configured to run DBUnit tests using a Spring TestExecutionListener.


Configuration
=============
To have Spring process DBUnit annotations you must first configure your tests to use the `DbUnitTestExecutionListener`
class. To do this you need to use the Spring `@TestExecutionListeners` annotation. Generally, as well as
`DbUnitTestExecutionListener`, you will also want to include the standard Spring listeners as well. Here are the
annotations for a typical JUnit 4 test:

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration
    @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    		DirtiesContextTestExecutionListener.class,
     		TransactionalTestExecutionListener.class,
    		DbUnitTestExecutionListener.class })


See the Spring JavaDocs for details of the standard listeners.

In order to access the database, Spring DBUnit requires a bean to be registered in your test context. By default a bean
named `dbUnitDatabaseConnection` or `dataSource` can be used (see the Advanced Configuration section below if you need
to use another name). The bean can reference either a `IDatabaseConnection` or more typically a standard Java
`DataSource`. Here is a typical XML configuration for accessing an in-memory hypersonic database:

    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    	<property name="driverClassName" value="org.hsqldb.jdbcDriver" />
    	<property name="url" value="jdbc:hsqldb:mem:paging" />
    	<property name="username" value="sa" />
    	<property name="password" value="" />
    </bean>

Once you have configured the `DbUnitTestExecutionListener` and provided the bean to access you database you can use the
DBUnit annotations.


Setup and TearDown
==================
Note: You need to complete the steps from the configuration section above before any annotations can be used. Without
appropriate configuration DBUnit annotations will be silently ignored.

The `@DatabaseSetup` and `@DatabaseTearDown` annotations can be used to configure database table before tests execute
and  reset them once tests have completed.


Setup
=====
The `@DatabaseSetup` annotation indicates how database tables should be setup before test methods are run. The
annotation can be applied to individual test methods or to a whole class. When applied at the class level the setup
occurs before each method in the test. The annotation value references a file that contains the table DataSet used
when resetting the database. Typically this is a standard DBUnit XML file, although it is possible to load custom
formats (see below).

Here is a typical setup annotation. In this case a file named `sampleData.xml` is contained in the same package as the
test class.

    @DatabaseSetup("sampleData.xml")

It is also possible to reference specific resource locations, for example:

    @DatabaseSetup("/META-INF/dbtest/sampleData.xml")

By default setup will perform a `CLEAN_INSERT` operation, this means that all data from tables referenced in the
DataSet  XML will be removed before inserting new rows. The standard DBUnit operations are supported using type
attribute. See the JavaDocs for full details.


TearDown
========
The `@DatabaseTearDown` annotation can be used to reset database tables once a test has completed. As with
`@DatabaseSetup` the annotation can be applied at the method or class level. When using `@DatabaseTearDown` use the
value and type attributes in the same way as `@DatabaseSetup`.

Note:  If you are running a teardown in conjunction with a `@Transactional` test you may need to use an alternative
configuration. See the section on below.


Expected results
================
The `@ExpectedDatabase` annotation can be used to verify the contents of database once a test has completed. You would
typically use this annotation when a test performs an insert, update or delete. You can apply the annotation on a
single test method or a class. When applied at the class level verification occurs after each test method.

The `@ExpectedDatabase` annotation takes a value attribute that references the DataSet file used to verify results.
Here is a typical example:

    @ExpectedDatabase("expectedData.xml")


The `@ExpectedDatabase` annotation supports two different modes. `DatabaseAssertionMode.DEFAULT` operates as any
standard DbUnit test, performing a complete compare of the expected and actual datasets.
`DatabaseAssertionMode.NON_STRICT` will ignore tables and column names which are not specified in the expected dataset
but exist in the actual datasets. This can be useful during integration tests performed on live databases containing
multiple tables that have many columns, so one must not specify all of them, but only the 'interesting' ones.

Note:  If you are using this annotation in conjunction with a `@Transactional` test you may need to use an alternative
configuration. See the section on below.


Transactions
============
If you have configured DBUnit tests to run using the `DbUnitTestExecutionListener` and you are also using the
`TransactionalTestExecutionListener` you may experience problems with transactions not being started before your data
is setup, or being rolled back before expected results can be verified. In order to support `@Transactional` tests with
DBUnit you should use the `TransactionDbUnitTestExecutionListener` class.


Here are the annotations for a typical JUnit 4 test:

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration
    @Transactional
    @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    		DirtiesContextTestExecutionListener.class,
    		TransactionDbUnitTestExecutionListener.class })

Transactions start before `@DatabaseSetup` and end after `@DatabaseTearDown` and `@ExpectedDatabase`.


Advanced configuration of the DbUnitTestExecutionListener
=========================================================
The `@DbUnitConfiguration` annotation can be used if you need to configure advanced options for DBUnit.

The databaseConnection attribute allows you to specify a specific bean name from the Spring Context that contains the
database connection. When not specified the names or can be used. The bean must be either an `IDatabaseConnection`
or a `DataSource`.

The `dataSetLoader` or `dataSetLoaderBean` attribute allows you to specify a custom loader that will be used when
reading datasets (see below). If no specific loader is specified a `dbUnitDataSetLoader` bean will be used from the
`ApplicationContext` (or if no such bean exists, the `FlatXmlDataSetLoader` will be used).

The `databaseOperationLookup` attribute allows you to specify a custom lookup strategy for DBUnit database operations
(see below).


Working with multiple connections
=================================
It is possible to configure Spring Test DBUnit to work with multiple connections within the same test. First declare
multiple `DataSource` or `IDatabaseConnection` beans in your application context. For example, here is XML configuration
for two in-memory databases:

    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    	<property name="driverClassName" value="org.hsqldb.jdbcDriver" />
    	<property name="url" value="jdbc:hsqldb:mem:paging" />
    	<property name="username" value="sa" />
    	<property name="password" value="" />
    </bean>
    <bean id="customerDataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    	<property name="driverClassName" value="org.hsqldb.jdbcDriver" />
    	<property name="url" value="jdbc:hsqldb:mem:customers" />
    	<property name="username" value="sa" />
    	<property name="password" value="" />
    </bean>

You then need to use the `@DbUnitConfiguration` on your test to link to the connections:

    @DbUnitConfiguration(databaseConnection={"dataSource", "customerDataSource"})

The `@DatabaseSetup`, `@DatabaseTearDown` and `@ExpectedDatabase` annotations all have a `connection` attribute which
can be used if you need to target a specific connection. If you don't specify a `connection` the first
`databaseConnection` from `@DbUnitConfiguration` will be used ("dataSource" in the example above).

Spring Test DBUnit Annotations are repeatable so if you are using Java 8+ you can use several with the same test. For
example:

    @Test
    @DatabaseSetup(value = "insert.xml")
	@DatabaseSetup(connection="customerDataSource", value="insert-custs.xml")
	public void testInsert() throws Exception {
		// Inserts "insert.xml" into dataSource and "insert-custs.xml" into customerDataSource
    	// ...
    }

If you are using an earlier version of Java you will need to use one of the wrapper annotations:

    @Test
    @DatabaseSetups({
    	@DatabaseSetup(value = "insert.xml")
    	@DatabaseSetup(connection="customerDataSource", value="insert-custs.xml")
	})
	public void testInsert() throws Exception {
		// Inserts "insert.xml" into dataSource and "insert-custs.xml" into customerDataSource
    	// ...
    }


Custom IDatabaseConnections
===========================
In some situations you may need to create an `IDatabaseConnection` with a specific DBUnit configuration. Unfortunately,
the standard DBUnit DatabaseConfig class cannot be easily using with Spring. In order to overcome this limitation,
the `DatabaseConfigBean` provides an alternative method to configure a connection; with  standard getter/setter access
provided for all configuration options. The `DatabaseDataSourceConnectionFactoryBean` accepts a configuration property
and should be used to construct the final connection. Here is a typical example:

    <bean id="dbUnitDatabaseConfig" class="com.github.springtestdbunit.bean.DatabaseConfigBean">
    	<property name="skipOracleRecyclebinTables" value="true"/>
    </bean>
    <bean id="dbUnitDatabaseConnection" class="com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean">
    	<property name="databaseConfig" ref="dbUnitDatabaseConfig"/>
    </bean>

NOTE: In most circumstances the username and password properties should not be set on the
`DatabaseDataSourceConnectionFactoryBean`. These properties will cause DBUnit to start a new transaction and may cause
unexpected behavior.


Writing a DataSet Loader
========================
By default DBUnit datasets are loaded from flat XML files. If you need to load data from another source you will need
to write your own DataSet loader and configure your tests to use it. Custom loaders must implement the `DataSetLoader`
interface and provide an implementation of the `loadDataSet` method. The `AbstractDataSetLoader` is also available and
provides a convenient base class for most loaders.

Here is an example loader that reads data from a CSV formatted file.

    public class CsvDataSetLoader extends AbstractDataSetLoader {
    	protected IDataSet createDataSet(Resource resource) throws Exception {
    		return new CsvURLDataSet(resource.getURL());
    	}
    }

See above for details of how to configure a test class to use the loader.


Custom DBUnit Database Operations
=================================
In some situations you may need to use custom DBUnit DatabaseOperation classes. For example, DBUnit includes
`org.dbunit.ext.mssql.InsertIdentityOperation` for use with Microsoft SQL Server. The `DatabaseOperationLookup`
interface can be used to create your own lookup strategy if you need support custom operations. A
`MicrosoftSqlDatabaseOperationLookup` class is provided to support the aforementioned MSSQL operations.

See above for details of how to configure a test class to use the custom lookup.
