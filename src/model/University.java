package model;

import java.util.ArrayList;
import java.util.List;

public class University {
	
	private String name;
	private String url;

	private List<School> schools = new ArrayList<School>();
	
	
	public University(String name, String url) {
		this.name = name;
		this.url = url;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUrl() {
		return url;
	}
	
	public List<School> getSchools() {
		return schools;
	}
	
	public School addSchool(School school) {
		if(this.schools == null) { this.schools = new ArrayList<School>();}
		this.schools.add(school);
		return school;
	}
}
