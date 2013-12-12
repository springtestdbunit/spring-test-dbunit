package com.github.springtestdbunit.dataset;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;

/**
 * A {@link DataSetLoader data set loader} that can be used to load {@link org.dbunit.dataset.xml.FlatXmlDataSet xml datasets},
 * replacing "[null]" with <code>null</code>.
 *
 * @author Stijn Van Bael
 */
public class ReplacementDataSetLoader implements DataSetLoader {
    private DataSetLoader delegate;

    public ReplacementDataSetLoader() {
        this(new FlatXmlDataSetLoader());
    }

    public ReplacementDataSetLoader(DataSetLoader delegate) {
        this.delegate = delegate;
    }

    public IDataSet loadDataSet(Class<?> testClass, String location) throws Exception {
        ReplacementDataSet replacementDataSet = new ReplacementDataSet(delegate.loadDataSet(testClass, location));
        replacementDataSet.addReplacementObject("[null]", null);
        return replacementDataSet;
    }
}

