package com.blink.search;
import com.blink.search.SearchProcessor;
import java.util.*;
import java.io.IOException;

public class Search {
	private static List<String> result;
	private static String mathResult;

	public static void main(String[] args) throws IOException {
			/* Parse command line */
		if (args.length < 1) {
			System.err.println("Usage: java Search term");
			return;
		}
		/* Get term */
		 String input=args[0];
		 for (int i=1; i<args.length; i++){
		 	input = input + " " + args[i];
		 }
		 System.out.println(input);
		 SearchProcessor sp = new SearchProcessor();
		 if (mathOrNot(input)!=true){
			 long startTime = System.currentTimeMillis();
			 result = sp.mockReturn(input);
			 long endTime = System.currentTimeMillis();
	         long difference = endTime - startTime;
			 System.out.println(result);
			 System.out.println("Seconds: " + difference/1000.0);
			 System.out.println(result.size() + " results found");
			} else {
			mathResult = sp.mathProcessor(input);
			System.out.println(mathResult);
		}

	}

	private static boolean mathOrNot(String query1){
	    if ((query1.length()>5)&&(query1.substring(0,5).equals("math:"))){
	      return true;
	    }
    	return false;
  }
}