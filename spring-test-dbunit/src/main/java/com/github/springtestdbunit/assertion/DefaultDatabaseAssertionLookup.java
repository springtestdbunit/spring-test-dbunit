package com.github.springtestdbunit.assertion;

/**
 * Default implementation for {@link DatabaseAssertionLookup}, delegating to {@link DatabaseAssertionMode#databaseAssertion}
 */
public class DefaultDatabaseAssertionLookup implements DatabaseAssertionLookup {

    public DatabaseAssertion getDatabaseAssertion(DatabaseAssertionMode mode) {
        return mode.getDatabaseAssertion();
    }
}
