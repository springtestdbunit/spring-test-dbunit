package com.github.springtestdbunit.entity;

import javax.persistence.*;

/**
 * @author Oleksii Lomako
 */
@Entity
public class OtherSampleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;

	@Column(name = "value")
	private String value;

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "OtherSampleEntity{" + "id=" + this.id + ", value='" + this.value + '\'' + '}';
	}

}
