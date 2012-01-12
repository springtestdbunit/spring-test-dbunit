package org.springframework.test.dbunit.helper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.Columns;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;

/**
 * Implements helper methods for custom data set assertions.
 * 
 * @author mzagar
 *
 */
public class AssertionHelper {

	/**
	 * Compares data sets ignoring all tables and columns which are not specified in expectedDataSet but exist in 
	 * actual data set.
	 * 
	 * @param expectedDataSet to compare with actual data set
	 * @param actualDataSet to compare with expected data set
	 * @throws DatabaseUnitException in case assertion fails
	 */
	public static void assertEqualsNonStrict(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
		// do not continue if same instance
		if (expectedDataSet == actualDataSet) {
		    return;
		}

		List<String> expectedNames = Arrays.asList(getSortedUpperTableNames(expectedDataSet));
		
		// compare tables, but ignore columns not specified in expectedDataSet
		for (int i = 0; i < expectedNames.size(); i++) {
		    String name = expectedNames.get(i);
		    
			String[] columnsToIgnore = getColumnsToIgnore(expectedDataSet.getTableMetaData(name), actualDataSet.getTableMetaData(name));
		    
			Assertion.assertEqualsIgnoreCols(expectedDataSet.getTable(name), actualDataSet.getTable(name), columnsToIgnore);
		}
	}

    private static String[] getColumnsToIgnore(ITableMetaData expectedMetaData, ITableMetaData actualMetaData) throws DataSetException {
    	Column[] notSpecifiedInExpected = Columns.getColumnDiff(expectedMetaData, actualMetaData).getActual();	
    	List<String> result = new LinkedList<String>();
    	for( Column c : notSpecifiedInExpected ) {
    		result.add(c.getColumnName());
    	}
    	return result.toArray(new String[result.size()]);
    }

	private static String[] getSortedUpperTableNames(IDataSet dataSet) throws DataSetException {
        String[] names = dataSet.getTableNames();
        for (int i = 0; i < names.length; i++) {
            names[i] = names[i].toUpperCase();
        }
        Arrays.sort(names);
        return names;
    }
}


