package com.github.springtestdbunit.expected;

import com.github.springtestdbunit.assertion.DatabaseAssertion;
import com.github.springtestdbunit.assertion.DatabaseAssertionLookup;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.IColumnFilter;

import java.util.List;

public class AssertAllDatabaseAssertionLookup implements DatabaseAssertionLookup {
    private static class AssertAllDatabaseAssertion implements DatabaseAssertion {
        @Override
        public void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet, List<IColumnFilter> columnFilters) throws DatabaseUnitException {
        }

        @Override
        public void assertEquals(ITable expectedTable, ITable actualTable, List<IColumnFilter> columnFilters) throws DatabaseUnitException {
        }
    }

    @Override
    public DatabaseAssertion getDatabaseAssertion(DatabaseAssertionMode mode) {
        return new AssertAllDatabaseAssertion();
    }
}
