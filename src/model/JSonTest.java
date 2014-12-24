package model;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JSonTest {

	public static void main(String[] args) {
		
		University tamu = new University("Texas A & M University", "https://www.tamu.edu/");
		School dwight = tamu.addSchool(new School("Dwight Look College of Engineering","https://www.tamu.edu/about/departments.html#engineering"));
		
		Department cse = new Department("Computer Science and Engineering","http://engineering.tamu.edu/cse/","About Department of Computer Science and Engineering");
		Department ece = new Department("Electrical and Computer Engineering","http://engineering.tamu.edu/electrical","About Department of Electrical and Computer Engineering");
		
		dwight.addDepartment(cse);
		dwight.addDepartment(ece);
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tamu));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
