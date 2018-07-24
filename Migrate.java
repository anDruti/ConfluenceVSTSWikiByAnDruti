import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Migrate {
	public static void justACheck() {
		String s= "asdsad ::: {.vf-progress-placeholder} dsa";
		s = s.replaceAll(":::(.)*\\{(.)*\\}", "");
	}
	
	public int startHeaderIndex = 0;
	public int endTableIndex = 0;
	//contains {.table-wrap}
	public String simpleTablesTransformation( String content ) {
		String wrapText = "{.table-wrap}";
		Integer startIndex = content.indexOf(wrapText);
		String contentToAnalyse=content.substring( startIndex + wrapText.length() );
		Scanner scanner = new Scanner(contentToAnalyse);
		boolean headerRead = false;
		String header = "", subHeader = "";
		Integer subHeaderCount = 0;
		boolean subHeaderFlag = false;
		boolean subHeaderRead = false;
		boolean bodyRead = false;
		Integer generalOffset = startIndex;
		String Output = "";
		List<String> subHeaders = new ArrayList<String>();
		boolean subHeaderAdded = false;
		Integer columnsCounter = 0;
		Integer subHeaderRowCounter = 0;
    	while (scanner.hasNextLine()) {    		
    		String line = scanner.nextLine();
    		generalOffset += line.length();
    		if(headerRead == false) {
    			if(line.contains("-------") && !line.contains("+--")) {
    				continue;
    			} else if (line.contains(":::") && bodyRead == true) {
    				//end of table;
    				
    				break;
    			} else if (line.contains("+---")) {
    				// not that tabel
    				return  content.substring(0, startIndex) + contentToAnalyse;
    			} else if (line.startsWith("| ")|| line.startsWith("+==")) {
    				
    			} else if (line.length() == 0){
    				continue;
    			} else {
    				
    				header = line;
    				headerRead = true;
    				//do smth with Header

    				header = transformToStandardHeader(header);

    				String[] headerArray = header.split("\n");
    				if(headerArray.length > 1) {
    				   columnsCounter = (headerArray[1].split("\\|")).length;
    				} 
    				
    				
    				

    				Output +=header;
    			//	System.out.println(" normal header: " + header);
    			}
    		//body read	
    		} else {
    			if (line.equals(":::") && headerRead == true && bodyRead == false) {
    				subHeaderCount ++;
    				subHeaderFlag = true;
    				continue;
    			} else if(line.equals(":::") && headerRead == true && bodyRead == true) {
    				//end of reading
    				break;
    			}
    			if(subHeaderCount > 0 && subHeaderFlag == true && line.length() > 0){
    			    //smth with subHeader and then set to false

    				
    				subHeaders.add(line);
    				
    				subHeaderCount --;
    				if(subHeaderCount == 0) {
    					subHeaderFlag = false;
    					subHeaderRead = true;
    				}
    			   continue;
    			} else if(subHeaderCount == 0 || subHeaderFlag == false) {
    				//normal body
    			     bodyRead = true;
    			     
    			     if (subHeaders.size() > 0 && subHeaderAdded == false) {
    			    	 //was some subHeaders, need to take care
    			    	 Output = addSubHeaders(Output,subHeaders);
    			    	
    	    			 columnsCounter = subHeaders.size();
    			    	 subHeaderAdded = true;
    			     }
    			     

    			     if(line.contains("-----")) {
    			    	 continue;
    			     }
    			     if(line.trim().isEmpty()){
    			    	 continue;
    			     }
    			     
    			     if(line.length() > 2 && line.substring(0,2).equals("  ") && line.length() > 2 && !(line.substring(3,4).equals(" "))){
    			    	 line = line.substring(2);
    			     }
    			    
    			     line = line.replaceAll("[\\s]{3,}", "|");
    			     line += "|";
    			     Integer tempColumnsSize = Stream.of(line.split("\\|"))
    			    		 						 .map(elem -> new String(elem))
    			    		 						 .collect(Collectors.toList()).size();
	      			if(line.startsWith("|")){
	      				tempColumnsSize ++;
	      			}	

	      			
    			     if(subHeaderAdded == false) {
    			    	if(tempColumnsSize < columnsCounter) { 
    			    	 
    			    
    			    	 Integer loopCounter = columnsCounter - tempColumnsSize;
    			    	 for(int j=0; j < loopCounter; j++) {
    			    		line+="|"; 
    			    	 }
    			    	}
    			    	if(line.startsWith("|")){
    			    		line = "X" + line;
    			    	}
    			    	
    			    	 Output +="\n" + line;	 
    			     } 
    			     else if(subHeaderAdded == true ) {
    			    	   

    			    	 if(subHeaderRowCounter % (columnsCounter+1) == 0) {
    			    		 Output+="\n";
    			    	 }
    			    	 subHeaderRowCounter ++;
    			    	 Output +=line;
    			     } 
    			     

    			}
    		}
    	}
		
		content = content.substring(0, startIndex) + Output + content.substring(generalOffset);
		return content;
	}
	
	String addSubHeaders(String output, List<String> subHeaders) {
		String tempOutput = "\n";
		
		if(subHeaders.size() > 0) {
		   
		   for(String s:subHeaders) {
			   tempOutput +=s + "|";
		   }
		}
		if(tempOutput.length() > 1) {
			tempOutput = tempOutput.substring(0, tempOutput.length() -1);
			tempOutput += "\n";
			String addString = "";
			for(int k=0; k<subHeaders.size();k++) {
				addString +="-|";
			}
			if(addString.length()>0) {
				addString = addString.substring(0,addString.length()-1);
				tempOutput +=addString;
			}
			
			output +=tempOutput;
		}
		
		return output;
	}
	
	public String tablesTransformation(String content) {
		//recognize the tables inside the String and make the transition of it
        Pattern r = Pattern.compile("\\+\\-\\-(.*)\\-\\-\\+");
        Matcher m = r.matcher(content);
        List<String> headerTitles = new ArrayList<String>();
        Map<String,List<String>> tableBody = new HashMap<String,List<String>>();
        endTableIndex = 0;
        int phraseLength = 0;
        startHeaderIndex = 0;
        if (m.find()) {
            startHeaderIndex = content.indexOf("+--");
            String contentToAnalyse=content.substring(startHeaderIndex);
         
        	Scanner scanner = new Scanner(contentToAnalyse);
      	    boolean headerFlag = true;
      	    int plusIndicator = -2;
      	    int rowIndicator = 0;
      	    phraseLength = 0;
        	while (scanner.hasNextLine()) {
        	  String line = scanner.nextLine();
        	  phraseLength += (line.length()+2);
        	 
        	  if(line.startsWith("|") || line.startsWith("+")) {
         		  if(line.startsWith("+")) {
        			  rowIndicator=0;
        			  plusIndicator ++;
        		  }
         		  //case when we have two or more lines of header
         		  if(headerFlag == false && plusIndicator < 0) {
         			  int titlesSize = headerTitles.size();
         			  if(titlesSize > 0) {
         				 line = line.replace("\\s*","").substring(1, line.length()-1); 
         				  List<String> newLine = Stream.of(line.split("\\|"))
         	        		      .map (elem -> new String(elem))
         	        		      .collect(Collectors.toList());
         				  if(newLine.size() == titlesSize){
         				  for(int i=0; i<titlesSize -1 ; i++){
         					 String item = headerTitles.get(i) + newLine.get(i);
         					headerTitles.set(i, item);
         				  }
         				  }
         			  }
         		  }
         		  
        		  // if header
        		  if(line.startsWith("|") && headerFlag == true){
        			  line = line.replace("\\s*","").substring(1, line.length()-1);
        			  headerTitles = Stream.of(line.split("\\|"))
        		      .map (elem -> new String(elem))
        		      .collect(Collectors.toList());
        			  
        			  headerFlag = false;
        		  }
        		  //body
        		  if(plusIndicator >=0 && line.startsWith("|")){
        			  
        			  line = line.replace("\\s*","").substring(1, line.length()-1);
        			  List<String> row = Stream.of(line.split("\\|"))
                		      .map (elem -> new String(elem))
                		      .collect(Collectors.toList());
        			  int length = row.size();
        			  
        				  if(!tableBody.containsKey(String.valueOf(plusIndicator))){
        					  List<String> tempList = new ArrayList<String>();
        					  for(int k = 0; k<=row.size()-1; k ++ ){
        						  String rowString = row.get(k);
        						  String existingListString = "";
    							  String rowGetString =  row.get(k);
    							  String out = checkBracketsAndjoinTwoStrings(existingListString, rowGetString);
        						  tempList.add(out);
        					  }
        					      
        					  
        					  tableBody.put(String.valueOf(plusIndicator), tempList);
        				  } else {
        					  List<String> existingList = tableBody.get(String.valueOf(plusIndicator)); 
        					  	for(int j=0; j <= row.size() - 1;j++){
        					  
        						  if(existingList.size()>0 && j < existingList.size() ) {
        							  String existingListString = existingList.get(j);
        							  String rowGetString =  row.get(j);
        							  
        							  String out = checkBracketsAndjoinTwoStrings(existingListString, rowGetString);
        							  
        							//  System.out.println(out);
        							  existingList.set(j,out);
        						  }	
        					  }
        		              tableBody.put(String.valueOf(plusIndicator), existingList);
        				  }
        		  }
        	  
        	  } else {
        				  
        		  break;
        	  }
        	}
        }
        
        String contentGeneral = buildHeader(headerTitles);
        contentGeneral +="\n";
        int columnAmount = 0;
		for(String s:headerTitles){
			contentGeneral +="-|";
			columnAmount ++;
	    }

		if( contentGeneral.length() > 0 )
		contentGeneral = contentGeneral.substring(0,contentGeneral.length()-1);
        String body = buildBody(tableBody, columnAmount);
        int endHeaderIndex = startHeaderIndex + phraseLength;
        String contentBefore = content.substring(0, startHeaderIndex);
        String contentAfter = content.substring(endHeaderIndex);
        String ccontent = contentBefore + contentGeneral + body + contentAfter; 
        return ccontent;
	}
	
	public  String body="";
	
	public  String buildHeader(List<String> headerTitles) {
		String header = "";
			for(String s:headerTitles){
				if(((s.replaceAll("[\\s]{2}", "")).trim()).isEmpty()) {
				    	s="X";
				}
				header +=s + "|";
			}

			if(header.length() > 2) {
		       return header.substring(0,header.length()-2);
			} else {
				return header;
			}
		
	}
	
	public String transformToStandardHeader(String headerTitles) {
		
		headerTitles = headerTitles.replaceAll("[^a-zA-Z\\s]", "");
		
		headerTitles = headerTitles.replaceAll("[\\s]{2}", ";");
		
		List<String> titles = Stream.of(headerTitles.split(";"))
				.map (elem -> new String(elem))
  		        .collect(Collectors.toList());
		String out ="";
		Integer items = 0;
		for (String s: titles) {
			//if(!s.contains("http") || !s.contains("confluence")){
				if(!s.isEmpty()) {
				    s = s.replaceAll("http(.)*[\\s]", " ");
				    s = s.replaceAll("confluence[a-zA-Z]*", " ");
				    if(s.trim().isEmpty()) {
				    	s="X";
				    }
					out += s + " | ";
					items ++;
				}
			//}
		}
		if(out.length() > 2)
		{	
		 headerTitles = out.substring(0, out.length() - 2);
		}
		headerTitles += "\n";
		String cols = "";
		for (int i=0; i < items; i++) {
			cols += "-|";
		}
		if(!cols.isEmpty()) {
			cols = cols.substring(0, cols.length()-1);
		}
		headerTitles +=cols;
		
		return headerTitles;
	}
	
	public static String checkBracketsAndjoinTwoStrings(String string1, String string2){
		
		Matcher matchPattern = Pattern.compile("\\{.*[^}]").matcher(string1);
		Matcher matchPattern2 =   Pattern.compile("\\(.*[^)]").matcher(string1);
		  if(matchPattern.find() || matchPattern2.find() ) {
			//  System.out.println(existingListString + "  Not Closed!");
			  string1 =string1.trim();
			  string2 = string2.trim();
		  }
		  
		String out = string1 + string2;
		return out;
	}
	
	public static String buildBody( Map<String,List<String>> tableBody, int colA ) {
		String body= "";
		

		Iterator<Entry<String, List<String>>> it = tableBody.entrySet().iterator();
		List<String> list;
		int i=0;
		while (it.hasNext()) {
		    Map.Entry<String, List<String>> pair = it.next();
		    list = (List<String>) pair.getValue();
		    for (String s: list){
		    	if((i)%(colA) == 0) {
		    		body+="\n";
		    	}
		    	
		    	body += "|" + s ;
		    	i++;
		    };
		    

		}
		body += "|";
		return body;
	}
	
	public static void main(String args[]) {

		try {
			System.out.println("START");
			Migrate app = new Migrate();
			List<File> filesInFolder = Files.walk(Paths.get("C:\\tmp\\Confluence-space-export-121905-14.html\\EXPORT"))
					.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
			
			for (File f : filesInFolder) {
				System.out.println(f.getPath());
				System.out.println(f.getName() + " changing");
				String content = new String(Files.readAllBytes(Paths.get(f.getPath())));

				if (content.contains("(attachments")) {
					content = content.replaceAll("\\(attachments", "\\(\\.attachments");
				};
				if (content.contains("(images/icons/")) {
				   content = content.replaceAll("\\(images\\/icons\\/", "\\(\\.attachments\\/images\\/icons\\/");
				}
				if (content.contains("(download/temp/")) {
				   content = content.replaceAll("\\(download\\/temp\\/", "\\(\\.attachments\\/download\\/temp\\/");
				}
				content = content.replaceAll("(\\()([A-Za-z0-9_-]*)(.html)(\\))", "$1$2$4");

				
				
                while (content.contains("{.table-wrap}")) {
					
					content = app.simpleTablesTransformation(content);
					
				}		

				
                while (content.contains("+--")) {
                	content = app.tablesTransformation(content);
                }
                

                
				content = content.replaceAll("\\{\\#(.*)\\}","");
				content = content.replaceAll("\\{\\.author\\}", "");
				content = content.replaceAll("\\{\\.editor\\}", "");
				content = content.replaceAll("\\{\\.external-link\\}", "");
				content = content.replaceAll("\\{\\.form(.*)\\}", "");
				content = content.replaceAll("\\{style(.*)\\}", "");
				content = content.replaceAll("\\{\\.mw(.*)\\}", "");
				content = content.replaceAll("\\{\\.confluence(.*)\\}", "");
				//content = content.replaceAll("\\{\\.table(.*)\\}","");
				content = content.replaceAll(":::(.)*\\{(.)*\\}", "").replaceAll(":::","");
				content = content.replaceAll("\\(image(.*)\\)", "");
				content = content.replaceAll("\\{width(.*)\\}", "");
				content = content.replaceAll("\\(\\.application\\/(.*)\\)", "");
				content = content.replaceAll("\\(application\\/(.*)\\)", "");
				content = content.replaceAll("\\[\\]\\{\\.aui(.*)\\}", "");
				content = content.replaceAll("\\{\\.expand(.*)\\}", "");
				content = content.replaceAll("\\{\\.jira(.*)\\}", "");
				content = content.replaceAll("\\{\\.emoticon(.*)\\}", "");
                
                
                content = content.replaceAll("\\{\\.confluence(.*)\\}", "");

				 Files.write(Paths.get(f.getParent() + "\\output\\" + f.getName()), content.getBytes());
			}
			
			System.out.println("END");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
}
