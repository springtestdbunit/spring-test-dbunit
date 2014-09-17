package com.github.springtestdbunit.dataset;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.springframework.util.Assert;

/**
 * A {@link DataSetLoader data set loader} that uses a {@link ReplacementDataSet} to replace specific objects or
 * sub-strings. By default will replace "[null]" with <code>null</code>.
 *
 * @author Stijn Van Bael
 * @author Phillip Webb
 */
public class ReplacementDataSetLoader implements DataSetLoader {

	public static final Map<String, Object> DEFAULT_OBJECT_REPLACEMENTS = Collections.singletonMap("[null]", null);

	private final DataSetLoader dataSetLoader;

	private final Map<Object, Object> objectReplacements;

	private final Map<String, String> subStringReplacements;

	/**
	 * Create a new {@link ReplacementDataSetLoader} using a {@link FlatXmlDataSetLoader} to load the source data and
	 * with {@link #DEFAULT_OBJECT_REPLACEMENTS}.
	 */
	public ReplacementDataSetLoader() {
		this(new FlatXmlDataSetLoader());
	}

	/**
	 * Create a new {@link ReplacementDataSetLoader} with {@link #DEFAULT_OBJECT_REPLACEMENTS}.
	 * @param dataSetLoader the source data set loader
	 */
	public ReplacementDataSetLoader(DataSetLoader dataSetLoader) {
		this(dataSetLoader, DEFAULT_OBJECT_REPLACEMENTS);
	}

	/**
	 * Create a new {@link ReplacementDataSetLoader}.
	 * @param dataSetLoader the source data set loader
	 * @param objectReplacements the object replacements or {@code null} if no object replacements are required
	 */
	public ReplacementDataSetLoader(DataSetLoader dataSetLoader, Map<?, ?> objectReplacements) {
		this(dataSetLoader, objectReplacements, null);
	}

	/**
	 * Create a new {@link ReplacementDataSetLoader}.
	 * @param dataSetLoader the source data set loader
	 * @param objectReplacements the object replacements or {@code null} if no object replacements are required
	 * @param subStringReplacements the sub-string replacements or {@code null} if no sub-string replacements are
	 * required
	 */
	public ReplacementDataSetLoader(DataSetLoader dataSetLoader, Map<?, ?> objectReplacements,
			Map<String, String> subStringReplacements) {
		Assert.notNull(dataSetLoader, "Delegate must not be null");
		this.dataSetLoader = dataSetLoader;
		this.objectReplacements = unmodifiableMap(objectReplacements);
		this.subStringReplacements = unmodifiableMap(subStringReplacements);
	}

	private <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> map) {
		Map<K, V> result = new LinkedHashMap<K, V>();
		if (map != null) {
			result.putAll(map);
		}
		return Collections.unmodifiableMap(result);
	}

	public IDataSet loadDataSet(Class<?> testClass, String location) throws Exception {
		IDataSet dataSet = this.dataSetLoader.loadDataSet(testClass, location);
		ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet, this.objectReplacements,
				this.subStringReplacements);
		return replacementDataSet;
	}

}
