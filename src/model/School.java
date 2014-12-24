package model;

import java.util.ArrayList;
import java.util.List;

public class School {
	
	private String name;
	private String url;

	private List<Department> departments;

	public School(String name, String url) {
		this.name = name;
		this.url = url;
	}
	
	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}
	
	public List<Department> getDepartments() {
		return departments;
	}
	
	public Department addDepartment(Department department) {
		if(this.departments == null) { this.departments = new ArrayList<Department>();}
		this.departments.add(department);
		return department;
	}
}
