package com.github.springtestdbunit.dataset;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * A {@link DataSetLoader data set loader} that can be used to load {@link org.dbunit.dataset.excel.XlsDataSet XlsDataSets}
 *
 * @author Yoshikazu Nojima
 */
public class XlsDataSetLoader extends AbstractDataSetLoader {

    @Override
    protected IDataSet createDataSet(Resource resource) throws Exception {
        InputStream inputStream = resource.getInputStream();
        try {
            return new XlsDataSet(inputStream);
        } finally {
            inputStream.close();
        }
    }
}
