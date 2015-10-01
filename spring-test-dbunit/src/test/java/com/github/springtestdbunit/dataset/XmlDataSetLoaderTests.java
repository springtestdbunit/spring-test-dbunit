package com.github.springtestdbunit.dataset;

import static org.junit.Assert.*;

import org.dbunit.dataset.IDataSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestContext;

import com.github.springtestdbunit.testutils.ExtendedTestContextManager;

/**
 * Tests for {@link XmlDataSetLoader}.
 *
 * @author Phillip Webb
 */
public class XmlDataSetLoaderTests {

	private TestContext testContext;

	private XmlDataSetLoader loader;

	@Before
	public void setup() throws Exception {
		this.loader = new XmlDataSetLoader();
		ExtendedTestContextManager manager = new ExtendedTestContextManager(getClass());
		this.testContext = manager.accessTestContext();
	}

	@Test
	public void shouldLoadFromRelativeFile() throws Exception {
		IDataSet dataset = this.loader.loadDataSet(this.testContext.getTestClass(), "non-flat-xmldataset.xml");
		assertEquals("Sample", dataset.getTableNames()[0]);
	}

	@Test
	public void shouldReturnNullOnMissingFile() throws Exception {
		IDataSet dataset = this.loader.loadDataSet(this.testContext.getTestClass(), "doesnotexist.xml");
		assertNull(dataset);
	}

}
