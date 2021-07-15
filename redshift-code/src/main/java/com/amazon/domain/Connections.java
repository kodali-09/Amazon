package com.amazon.domain;

import javax.persistence.*;
import java.sql.Blob;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
public class Connections {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long _id;

	private String displayname;
	@Lob
	private Blob config;

	public Long get_id() {
		return _id;
	}

	public void set_id(Long _id) {
		this._id = _id;
	}




}
