package model;


public class Department {

	private String name;
	private String url;
	private String about;
	
	public Department(String name, String url, String about) {
		super();
		this.name = name;
		this.url = url;
		this.about = about;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getAbout() {
		return about;
	}
}
