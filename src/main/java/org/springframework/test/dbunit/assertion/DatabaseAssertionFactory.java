package org.springframework.test.dbunit.assertion;

import org.springframework.test.dbunit.annotation.DatabaseAssertionMode;

/**
 * Factory for specific database assertion strategy.
 * 
 * @author Mario Zagar
 *
 */
public class DatabaseAssertionFactory {

	/**
	 * Returns specific database assertion implementation.
	 * 
	 * @param assertionMode for which to create database assertion implementation
	 * @return specific database assertion implementation
	 */
	public static DatabaseAssertion createDatabaseAssertion(DatabaseAssertionMode assertionMode) {
		if ( assertionMode == DatabaseAssertionMode.DEFAULT ) {
			return new DefaultDatabaseAssertion();
		}
		
		if ( assertionMode == DatabaseAssertionMode.NON_STRICT ) {
			return new NonStrictDatabaseAssertion();
		}
		
		throw new IllegalArgumentException("no database assertion implementation specified for: " + assertionMode);
	}

}
