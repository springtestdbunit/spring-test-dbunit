package com.github.springtestdbunit.dataset;

import com.github.springtestdbunit.testutils.ExtendedTestContextManager;
import org.dbunit.dataset.IDataSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link XlsDataSetLoader}.
 *
 * @author Yoshikazu Nojima
 */
public class XlsDataSetLoaderTest {

    private TestContext testContext;

    private XlsDataSetLoader loader;

    @Before
    public void setup() throws Exception {
        this.loader = new XlsDataSetLoader();
        ExtendedTestContextManager manager = new ExtendedTestContextManager(getClass());
        this.testContext = manager.accessTestContext();
    }

    @Test
    public void shouldLoadFromXlsFile() throws Exception {
        IDataSet dataset = this.loader.loadDataSet(this.testContext.getTestClass(), "test.xls");
        assertEquals("Sample", dataset.getTableNames()[0]);
    }

    @Test
    public void shouldLoadFromXlsxFile() throws Exception {
        IDataSet dataset = this.loader.loadDataSet(this.testContext.getTestClass(), "test.xlsx");
        assertEquals("Sample", dataset.getTableNames()[0]);
    }

    @Test
    public void shouldReturnNullOnMissingFile() throws Exception {
        IDataSet dataset = this.loader.loadDataSet(this.testContext.getTestClass(), "doesnotexist.xls");
        assertNull(dataset);
    }


}
