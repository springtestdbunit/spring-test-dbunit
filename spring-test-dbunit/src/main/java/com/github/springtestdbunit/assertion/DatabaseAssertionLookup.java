package com.github.springtestdbunit.assertion;

/**
 * Strategy used to lookup {@link DatabaseAssertion} from a value {@link DatabaseAssertionMode enum value}.
 */
public interface DatabaseAssertionLookup {
    /**
     * Get the {@link DatabaseAssertion} implementation for this mode.
     *
     * @param mode Database assertion mode
     * @return Database assertion
     */
    DatabaseAssertion getDatabaseAssertion(DatabaseAssertionMode mode);
}
