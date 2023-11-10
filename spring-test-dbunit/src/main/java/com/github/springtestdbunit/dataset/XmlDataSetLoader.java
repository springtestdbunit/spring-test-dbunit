package com.github.springtestdbunit.dataset;

import java.io.InputStream;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.springframework.core.io.Resource;

/**
 * A {@link DataSetLoader data set loader} that can be used to load {@link XmlDataSet XmlDataSets}.
 *
 * @author Jorge Davison
 * @since 1.3.0
 */
public class XmlDataSetLoader extends AbstractDataSetLoader {

	@Override
	protected IDataSet createDataSet(Resource resource) throws Exception {
		try (InputStream inputStream = resource.getInputStream()) {
			return new XmlDataSet(inputStream);
		}
	}

}
