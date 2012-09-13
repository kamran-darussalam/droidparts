package org.droidparts.test.model;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;

@Table(name = "tracks")
public class Track extends Entity {
	private static final long serialVersionUID = 1L;

	@Column
	public Album album;

	@Column
	public String name;

}