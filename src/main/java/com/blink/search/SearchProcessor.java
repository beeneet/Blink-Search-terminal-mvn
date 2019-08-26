package com.blink.search;
import com.blink.search.Query;
import java.util.*;
import java.io.IOException;
import org.mariuszgromada.math.mxparser.*;

public class SearchProcessor {

	private String termQuery;
	private List<String> result;

	private static List<Integer> findCommons(List<Integer> list1, List<Integer> list2){
    Integer list1_pointer = 0;
    Integer list2_pointer = 0;
    List<Integer> conjunctive_pages = new ArrayList<Integer>();
    if (list2.size()==0){
      return list1;
    }

    while ((list1_pointer < list1.size()) && (list2_pointer < list2.size())){
      if (list1.get(list1_pointer).equals(list2.get(list2_pointer))){
        list1_pointer++;
        list2_pointer++;
      } else if (list1.get(list1_pointer).compareTo(list2.get(list2_pointer)) < 0){
        conjunctive_pages.add(list1.get(list1_pointer));
        list1_pointer++;
      } else {
        list2_pointer++;
      }
    }

    while (list1_pointer < list1.size()){
      conjunctive_pages.add(list1.get(list1_pointer));
      list1_pointer++;
    }
    
    return conjunctive_pages;
  }

  private static List<Integer> mergePages(List<Integer> list1, List<Integer> list2) {
    Integer list1_pointer = 0;
    Integer list2_pointer = 0;
    List<Integer> merged_pages = new ArrayList<Integer>();
    if (list2.size()==0){
       System.out.println(list1);
    }

    while ((list1_pointer < list1.size()) && (list2_pointer < list2.size())){
      if (list1.get(list1_pointer).equals(list2.get(list2_pointer))){
        merged_pages.add(list1.get(list1_pointer));
        list1_pointer++;
        list2_pointer++;
      } else if (list1.get(list1_pointer).compareTo(list2.get(list2_pointer)) < 0){
        merged_pages.add(list1.get(list1_pointer));
        list1_pointer++;
      } else {
        merged_pages.add(list2.get(list2_pointer));
        list2_pointer++;
      }
    }

    while (list1_pointer < list1.size()){
      merged_pages.add(list1.get(list1_pointer));
      list1_pointer++;
    }

     while (list2_pointer < list2.size()){
      merged_pages.add(list2.get(list2_pointer));
      list2_pointer++;
    }
    return merged_pages;
  }

  private static boolean mathOrNot(String query1){
    if ((query1.length()>5)&&(query1.substring(0,5).equals("math:"))){
      return true;
    }
    return false;
  }

  private static String doMath(String query1){
    Expression e = new Expression(query1);
    String a = String.valueOf(e.calculate());
    return (a);
  }

  private static String convertAsMath(String mathoutput){
    return "Math result is: " + String.valueOf(mathoutput);
  }

  private static List<Integer> getORList(List<Integer> list1, List<Integer> list2){
    List<Integer> merged_pages = new ArrayList<Integer>();
    merged_pages = mergePages(list1, list2);
    return merged_pages;
  }

  private static List<String> getORresults(String oldTerm, Query query1) throws IOException{
    List<Integer> resultsForOr = new ArrayList<Integer>();
    List<Integer> resultsforCurrent = new ArrayList<Integer>();
    String[] keyTerms = oldTerm.split(" OR ");
    System.out.println(Arrays.toString(keyTerms));
    for (int k = 0; k < keyTerms.length; k++){
      resultsforCurrent = query1.getQuery(keyTerms[k].toLowerCase());
      resultsForOr = getORList(resultsforCurrent, resultsForOr);
    }
    return query1.getFilenameList(resultsForOr);
  }

  private static List<String> getNormalOrConjunctiveResults(String term, Query query1) throws IOException{
    List<Integer> resultsString = new ArrayList<Integer>();
    List<Integer> resultsString1 = new ArrayList<Integer>();
    boolean found = false;
    String new_string_after_minus = "";
    String new_string_before_minus = "";
    for (int i = 0; i<term.length(); i++){
      char chara = term.charAt(i);
      if (chara == '-'){
        found = true;
        continue;
      }
      if (found){
        if (chara!=' '){
        new_string_after_minus+= chara;
      }
      } else {
        new_string_before_minus+= chara;
      }
    }
    resultsString = query1.getQuery(new_string_before_minus);
    resultsString1 = query1.getQuery(new_string_after_minus);
    List<String> finalList = query1.getFilenameList(findCommons(resultsString, resultsString1));
    return finalList;
  }

  public static String mathProcessor( String term) throws IOException{
    String mathResult = String.valueOf(doMath(term.substring(5, term.length())));
    // List<String> mathResList = new ArrayList<String>();
    mathResult = convertAsMath(mathResult);
    return mathResult;
  }


  public static List<String> mockReturn( String term) throws IOException{
    Query query1 = new Query();
    
    String oldTerm = term;
    term = term.replace("%2B", "+").toLowerCase();
    List<String> splittedString = new ArrayList<String>();
    splittedString = Arrays.asList(oldTerm.split(" "));
    if (splittedString.contains("OR")){
      return getORresults(oldTerm, query1);
    }
    return getNormalOrConjunctiveResults(term, query1);
	}

}