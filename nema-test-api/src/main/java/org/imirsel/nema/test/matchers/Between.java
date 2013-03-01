package org.imirsel.nema.test.matchers;


import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;



public class Between <T> extends BaseMatcher<T>  {
	
	private double start;
	private double end ;

	public  Between(double start, double end){
		this.start = start;
		this.end = end;
	}
	

	public boolean matches(Object object) {
	Number number = (Number) object;
	if(number.doubleValue() >= this.start && number.doubleValue() <= this.end){
		return true;
	}
	return false;
	}


	public void describeTo(Description description) {
		description.appendText("a valid value between start and end ");
	}

}
