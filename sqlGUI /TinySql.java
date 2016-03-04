import java.io.*;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.lang.Math;
import java.lang.String;
import java.util.Vector;


public class TinySql{

	private String replacement_char;
	public TinySql(){
		replacement_char="\t";
	}
	
	/*
	 * returns the general error statement
	 *
	*/	
	public String generateErrorforInvalid(String command){
			return "Error: Invalid statement";
	}
	
	/*
	 * creates the new table if the syntx is correct, returns error message otherwise
	 *
	*/	
	public String runCreate(String command){
		
		String output = "";
		
		boolean b = Pattern.matches("(^CREATE\\s+[a-z][a-z0-9]*\\s*\\((\\s*[a-z][a-z0-9]*\\s+(INT|STRING)\\s*,\\s*)+(\\s*[a-z][a-z0-9]*\\s+(INT|STRING)\\s*)\\)\\s*$)"
					,command);
		if(!b){
			return generateErrorforInvalid(command);
		}
		
		//retrieve the table name
		//check if the table already exists
		String tableName = command.substring(0, command.indexOf("(")).replace("CREATE","").trim();
		
		File table = new File ("tbl_" + tableName);
		if(table.exists()){
			return  "Error: "+"Table " + tableName + " already exists";
		}
		
		//retrieve the field names and their respective types
		String x = command.trim().substring(command.indexOf("(")+1, command.indexOf(")"));
		
		String [] field_with_types = x.split(",");
		
		String [] fields = new String[field_with_types.length];
		String [] field_types = new String[field_with_types.length];
		
		
					
		for(int i = 0; i < field_with_types.length; i++){
			int index = field_with_types[i].trim().indexOf(" ");
			fields[i] = field_with_types[i].trim().substring(0, index).trim();
			field_types[i] = field_with_types[i].trim().substring(index).trim();
			/*
			String field = field_with_types[i].substring(0, field_with_types[i].indexOf("\\s+")).trim();
			String field_type = field_with_types[i].substring(field_with_types[i].indexOf("\\s+")).trim();
			if(field != field.toLower()){
				output += "Invalid field name: "+field;
				flag = false;
			}
			if(field_type != "INT" && field_type != "STRING"){
				output += "Invalid field type: "+field_type;
				flag = false;
			}
			*/
		}
		
		try{
			table.createNewFile();
			
			File dictionary = new File ("dictionary");
			
			if(!dictionary.exists()){
				dictionary.createNewFile();
			}
			
			FileWriter fileWritter = 
					new FileWriter("dictionary",true);
					
			BufferedWriter bufferWritter = 
					new BufferedWriter(fileWritter);
					
			for(int i = 0; i < fields.length; i++){
				bufferWritter.write( tableName + "|" + fields[i] + "|" + field_types[i] + "|" + table.getName() + "\n");
			}
			
			bufferWritter.close();
			output = "Success: table " + tableName + " created successfully";
			
		}
		catch(Exception e){
			output = "Error: table " + tableName + " could not be created";
		}
		finally{
			
		}
		
		
		return output;
	}
	
	/*
	 * inserts the record into the table if the syntax is correct, return error message otherwise 
	 *
	*/	
	public String runInsert(String command){
	   String output = "";
	   // Checks the format of the insert query using pattern matching
	   boolean c = Pattern.matches("(^INSERT\\s+INTO\\s+[a-z][a-z0-9]*\\s*\\(\\s*(([0-9]+)|('([^']*)'))"
			+"((\\s*,\\s*(('([^']*)')|([0-9]+)))*)\\s*\\)\\s*$)", command);


		if(c==false)
			return generateErrorforInvalid(command);
			
		String cmd = (command.substring(6).trim()).substring(4).trim();
		String tableName = cmd.substring(0,cmd.indexOf("(")).trim();
		String Record = new String();
		String [] field;
		boolean t = false;
   
		String prefix = "tbl_";
		String FileName = (prefix.concat(tableName));
		int index1 = cmd.indexOf('(');
		int index2 = cmd.indexOf(')');
		String row = cmd.substring(index1+1, index2);
		String [] values = row.split(",");//values are separetd by commas and stored in an array
		
		int fields = values.length;
		boolean v1 = false;
		boolean v2 = false;
		int len;
		int ind1;
		int ind2;
		String st, st1;
		for (int i=0; i<fields; i++)//To check the length of the String
		{
			values[i] = values[i].trim();
			if(values[i].startsWith("'") && values[i].endsWith("'"))
			{						 
				ind1 = values[i].indexOf("'");
				ind2 = values[i].lastIndexOf("'");					  
				if((len= (st=values[i].substring(ind1+1,ind2)).length()) > 64)
				{
					output = " STRING vaues can't exceed more than 64 char " + i +st+len ;
					return output;
				}
				values[i] = values[i].replaceAll("\\|",replacement_char);
				//System.out.println(values[i]);
			}

		}		 
		  
		for (int i=0; i<fields; i++)//To append into the file
		{
			Record = Record.concat(((values[i]).replaceAll("'", "")));
			if(i < fields-1)
				Record=Record.concat("|");								   
		}
								 
											
		try// to read from data dictinary to compare for data types no.of fields and table
		{
			File file = new File(FileName);
			FileReader fileReader = new FileReader("dictionary");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			
			boolean d=false;
			int count=0;
			while ((line = bufferedReader.readLine()) != null)
			{
				
				if (line.startsWith(tableName) && line.endsWith("tbl_"+tableName))
				{
					if(count >= values.length)
						return "Error: Insufficient number of values to insert";
						
					String [] ty = line.split("\\|");
					if(Pattern.matches("(INT|int)", ty[2]) && !Pattern.matches("(^[0-9]+$)", values[count])){
						return "Error: Data type does not match according to data dictionary";
					}
					else if (Pattern.matches("('(.*)'(.*)')", values[count])){
						return "Error: Invalid character (') for STRING";
					}
					else if(!Pattern.matches("(INT|int)", ty[2]) && (!values[count].startsWith("'") || !values[count].endsWith("'")))
					{
						System.out.println(values[count]+values[count].startsWith("'")+values[count].endsWith("'"));
						return "Error: type STRING not quoted by the character (')";
					}
					count ++ ;
						 
				   }// end of if
			  }//end of while
			//Looks for the file in the current folder and an entry in the dictionary 
			//file before inserting a row, if both of them are false it will return
			if( !file.exists() && !t )
			  {
				 output = "There is no Table to insert, Error!";
				 return output + t; 
				 
			   }  
			if (fields != count)
			   {
				 output = "No.of fields do not agree with table you are trying to insert";
				 return output;
				}						 
			
			else
			   {				
					BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
					PrintWriter out = new PrintWriter(bw); 
					out.println(Record);
					out.close();
					
				}
		  }catch ( IOException e ) 
		 {
			e.printStackTrace();
		 }	
		 
		 output  = "Inserted a row successfully";//+ Record+c+t;
		 return output;
	}
	
	/*
	 * inserts the fiel names and field types into the respective ArrayList for a given table name
	 *
	*/	
	public void getDictionary(String table_name, ArrayList <String> fields, ArrayList <String> field_types){
		File fileName = new File("dictionary");
		
		try{
			String line = null;
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			while((line = bufferedReader.readLine()) != null) {
				if(line.length() > 0 && line.startsWith( table_name) && line.endsWith( "tbl_"+table_name))
				{

					String[] x = line.split("\\|");
					fields.add(x[1].trim());
					field_types.add(x[2].trim());
					
				}
			}
		}
		catch(Exception exp){
			System.out.println(exp.getMessage());
		}
	}
	
	/*
	 * verifies if all the fields are in the table
	 * if there are fields, indexes contains the respective index of fields in the fields1 arraylist 
	 *
	*/	
	public String verifyFieldSingleTable(String[] to_select_fields, ArrayList<String> fields1, int[] indexes){
		String missed_field = "";
		for(int i = 0; i < to_select_fields.length; i++){
			if(!fields1.contains(to_select_fields[i].trim())){
				if(missed_field.length() > 0)
					missed_field += ", ";
				missed_field += to_select_fields[i].trim();
			}
			else{
				indexes[i] = fields1.indexOf(to_select_fields[i].trim());
			}
		}
		return missed_field;
	}
	
	/*
	 * verifies the fields in the two tables
	 * if there are fields, indexes contains the respective index of fields in fields1 and fields2 arraylist
	 * for fields2 arraylist, the fields2 has indexes fields1+index
	 * missed field and mismatch field are shown as errors
	 *
	*/	
	public String verifyFieldJoinTable(String[] to_select_fields, ArrayList<String> fields1, ArrayList<String> fields2, int[] indexes){
		String missed_field = "";
		String mismatch_field = "";
		for(int i = 0; i < to_select_fields.length; i++){
			if(fields1.contains(to_select_fields[i].trim()) && fields2.contains(to_select_fields[i].trim())){
				if(mismatch_field.length() > 0)
					mismatch_field += ", ";
				mismatch_field += to_select_fields[i].trim();
			}
			else if(!fields1.contains(to_select_fields[i].trim()) && !fields2.contains(to_select_fields[i].trim())){
				if(missed_field.length() > 0)
					missed_field += ", ";
				missed_field += to_select_fields[i].trim();
			}
			else{
				if(fields1.contains(to_select_fields[i].trim()))
					indexes[i] = fields1.indexOf(to_select_fields[i].trim());
				else
					indexes[i] = fields1.size()+fields2.indexOf(to_select_fields[i].trim());
			}
		}
		if(missed_field.length() > 0 && mismatch_field.length() > 0)
			return "Error: Missing fields -> "+missed_field+" and Mismatch field: "+mismatch_field;
		else if(missed_field.length() > 0 )
			return "Error: Missing fields -> "+missed_field;
		else if( mismatch_field.length() > 0)
			return "Error: Mismatch field-> "+mismatch_field;
		else
			return "";
	}
	
	/*
	 * Validity of where clause is checked
	 * the indexes of the fields in the where clause are checked
	 *
	*/	
	public String verifyWhereSingleTable(String table_name, String where_clause, int [] where_field_indexes, String [] constants){
		int equal_index = where_clause.indexOf("=");
		if(equal_index < 0)
			return "Error: No comaparision(equal) operator)";
		ArrayList <String> fields = new ArrayList<String>();
		ArrayList <String> field_types = new ArrayList<String>();
		
		String field_name = where_clause.substring(0, equal_index).trim();
		if(field_name.length() == 0)
			return "Error: No field name to compare";
		
		String constant = where_clause.substring(equal_index+1).trim();
		
		if(constant.length() == 0)
			return "Error: No constant to compare to";
		
		getDictionary(table_name, fields, field_types);
		if(!fields.contains(field_name))
			return "Error: table "+ table_name+" does not have field "+field_name;
			
		if(field_types.get(fields.indexOf(field_name)).equals("STRING")){
			if(!constant.substring(0,1).equals( "'") || !constant.substring(constant.length()-1).equals( "'"))
				return "Error: STRING field "+field_name +" is not quoted by '";
			else
				constant = constant.substring(1, constant.length()-1);
		}
		else if(!Pattern.matches("(^[0-9]+$)", constant)){
			return "Error: INT field "+field_name +" has not been given valid INT "+constant;
		}
		where_field_indexes[0] = fields.indexOf(field_name);
		constants[0] = constant;
		
		return "";
	}
	
	/*
	 * Join condition is check
	 * type mismatch is checked
	 * the error in the join condition is checked
	 *
	*/	
	public String checkJoinCondition(String where_clause, ArrayList <String> fields1, ArrayList <String> field_types1, ArrayList <String> fields2, ArrayList <String> field_types2, int [] indexes){
				
		int equal_index = where_clause.indexOf("=");
		if(equal_index < 0)
			return "Error: No comaparision(equal) operator)";
		
		String first_field = where_clause.substring(0, equal_index).trim();
		String second_field = where_clause.substring(equal_index+1).trim();
		
		if(first_field.length() == 0 || second_field.length() == 0)
			return "Error: Invalid comparison as one field is missing";
			
		indexes[0] = -1;
		indexes[1] = -1;
		
		if(fields1.contains(first_field) && fields2.contains(second_field) && !first_field.equals(second_field)){
			if(!fields1.contains(second_field) && fields2.contains(second_field)){
				indexes[0] = fields1.indexOf(first_field);
				indexes[1] = fields2.indexOf(second_field);
			}
			else if(fields1.contains(second_field) && !fields2.contains(second_field)){
				indexes[0] = fields1.indexOf(second_field);
				indexes[1] = fields2.indexOf(first_field);
			}
			else{
				return "Error: Field mismatch";
			}
		}
		else if (fields1.contains(first_field)){
			if(fields2.contains(second_field)){
				indexes[0] = fields1.indexOf(first_field);
				indexes[1] = fields2.indexOf(second_field);
			}
			else {
				return "Error: Field "+second_field+" does not exist";
			}
		}
		else if (fields2.contains(first_field)){
			if(fields1.contains(second_field)){
				indexes[0] = fields1.indexOf(second_field);
				indexes[1] = fields2.indexOf(first_field);
			}
			else {
				return "Error: Field "+second_field+" does not exist";
			}
		}
		else{
			return "Error: Field "+first_field+" does not exist";
		}
		
		//check for the type
		if(!field_types1.get(indexes[0]).equals(field_types2.get(indexes[1])))
			return "Error: Type mismatch";
		return "";
	}
	
	/*
	 * reads the whole file and and saves in an arraylist 
	 *
	*/	
	public String readTableFile(String table_name, ArrayList<String []> table_data){
		File table = new File("tbl_"+table_name);
		try{
			String line = null;
			FileReader fileReader = new FileReader(table);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			while((line = bufferedReader.readLine()) != null) {
				if(line.length() > 0){
					String[] x = line.split("\\|");
					table_data.add(x);
				}
			}
		}
		catch(Exception exp){
			System.out.println(exp.getMessage());
			return "Error: problem with reading from the file";
		}
		return "";
	}
	
	/*
	 * runs a select command and return the result or an error message depending upon the syntax
	 *
	*/	
	public String runSelect(String command, Vector<Vector<String>> select_output, Vector<String> headers){
		boolean b = Pattern.matches("(^SELECT\\s+(\\*|(((\\s*[a-z][a-z0-9]*\\s*)*)|[\\s*,\\s*[a-z][a-z0-9]*]*))\\s+FROM"+
		"\\s+([a-z][a-z0-9]*)((\\s*)|(\\s*[,]\\s*[a-z][a-z0-9]*))"+
		"((\\s*)|((\\s+WHERE\\s+)([a-z][a-z0-9]*)\\s*=\\s*(([a-z][a-z0-9]*)|([0-9]+)|(['](.*)[']))))"+
		"\\s*$)",command);
		
		if(b == false){
			return generateErrorforInvalid(command);
		}
		
		String output = "";
		String [] to_select_fields = command.substring(0, command.indexOf("FROM")).replace("SELECT","").trim().split(",");
		
		int where_index = command.indexOf("WHERE");
		int y = where_index;
		if(where_index < 0)
			y = Math.max(command.length(), where_index);
			
		String [] tables = command.substring(command.indexOf("FROM"), y).replace("FROM","").trim().split(",");
		
		String where_clause = "";
		if(where_index >= 0)
			where_clause = command.substring(where_index+5).trim();
		
		if(where_index >= 0 && where_clause.length() == 0)
			return "Error: No fields in the WHERE clause";
		

		ArrayList <String> fields1 = new ArrayList<String>();
		ArrayList <String> field_types1 = new ArrayList<String>();
		int [] indexes = new int[to_select_fields.length];
		
		

		//single table selection	
		if(tables.length == 1){
			getDictionary(tables[0], fields1, field_types1);
			if(fields1.size() == 0 ){
				return "Error: table "+ tables[0]+" does not exist\n";
			}

			//check for the fields existence in the dictionary
			if(to_select_fields[0].trim().equals("*") == false)
			{
				String errorMessage = verifyFieldSingleTable(to_select_fields, fields1, indexes);
				
				if(errorMessage.length() > 0){
					return "Error: table "+tables[0]+" does not have field: "+errorMessage;
				}
			}

			//check the validity of where clause
			int [] where_field_indexes = new int[1];
			String [] constants = new String[1];
			
			if(where_clause.length() > 0){
				String errorMessage = verifyWhereSingleTable(tables[0], where_clause, where_field_indexes, constants);
				if(errorMessage.length() > 0)
					return errorMessage;
			}
			
			if(to_select_fields[0].trim().equals( "*")==false){
				for(int i = 0; i < indexes.length; i++){
					headers.add(fields1.get(indexes[i]));
				}
			}
			else {

				for(int i = 0; i < fields1.size(); i++){
					headers.add(fields1.get(i));
				}
			}

			File table1 = new File("tbl_"+tables[0]);
			try{
				String line = null;
				FileReader fileReader = new FileReader(table1);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				


				while((line = bufferedReader.readLine()) != null) {
					String[] x = line.split("\\|");
					if(where_clause.length() > 0 &&
						 !(x[where_field_indexes[0]].compareTo( constants[0]) == 0 ||
						(field_types1.get(where_field_indexes[0]).equals("INT") && Integer.parseInt(x[where_field_indexes[0]]) == Integer.parseInt(constants[0]))
						))
						continue;

					Vector <String> temp = new Vector<String>();
					if(to_select_fields[0].trim().equals( "*")==false){
						for(int i = 0; i < indexes.length; i++){
							temp.add(x[indexes[i]].replaceAll(replacement_char,"\\|"));
						}
					}
					else{
						for(int i = 0; i < x.length; i++){
							temp.add(x[i].replaceAll(replacement_char,"\\|"));
						}
					}
					select_output.add(temp);
				}
			}
			catch(Exception exp){

				output += "Error: problem with reading from the file";
			}
		}
		
		//join of two tables
		else if(tables.length == 2){
			

			if(where_clause.length() == 0){
				return "Error: No WHERE clause for the JOIN condition";
			}
			ArrayList <String> fields2 = new ArrayList<String>();
			ArrayList <String> field_types2 = new ArrayList<String>();
			

			File table1 = new File("tbl_"+tables[0].trim());
			File table2 = new File("tbl_"+tables[1].trim());
			
			if(table1.length() > table2.length()){
				String tempString = tables[0];
				tables[0] = tables[1];
				tables[1] = tempString;
			}
			
			getDictionary(tables[0].trim(), fields1, field_types1);
			getDictionary(tables[1].trim(), fields2, field_types2);
			
			if(fields1.size() == 0 && fields2.size() == 0){
				return "Error: table "+ tables[0]+" and "+tables[1]+" do not exist\n";
			}
			else if(fields1.size() == 0 ){
				return "Error: first table "+ tables[0]+" does not exist\n";
			}
			else if(fields2.size() == 0 ){
				return "Error: second table "+ tables[1]+" does not exist\n";
			}
			
			int [] join_field_indexes = new int[2];
			String errorMessage = checkJoinCondition(where_clause, fields1, field_types1, fields2, field_types2, join_field_indexes);
			if(errorMessage.length() > 0)
				return errorMessage;
				
			//retrieve the fields of the two tables
			//check for the fields existence in the dictionary
			if(to_select_fields[0].trim().equals("*") == false)
			{
				errorMessage = verifyFieldJoinTable(to_select_fields, fields1, fields2, indexes);
				if(errorMessage.length() > 0){
					return errorMessage;
				}
			}
			
			ArrayList <String[]> table_data1 = new ArrayList<String[]>();
			ArrayList <String[]> table_data2 = new ArrayList<String[]>();
			
			errorMessage = readTableFile(tables[0].trim(), table_data1);
			
			if(errorMessage.length() > 0){
				return errorMessage;
			}
			
			/*
			errorMessage = readTableFile(tables[1].trim(), table_data2);
			
			if(errorMessage.length() > 0){
				return errorMessage;
			}
			*/

			if(to_select_fields[0].trim().equals( "*")==false){
				for(int i = 0; i < indexes.length; i++){
					if (indexes[i] < fields1.size())
						headers.add(fields1.get(indexes[i]));
					else 
						headers.add(fields2.get(indexes[i]-fields1.size()));
				}
			}
			else {
				for(int i = 0; i < fields1.size(); i++){
					headers.add(fields1.get(i));
				}
				
				for(int i = 0; i < fields2.size(); i++){
					headers.add(fields2.get(i));
				}
			}
			
			for(int i = 0; i < table_data1.size(); i++){
				File table = new File("tbl_"+tables[1].trim());
				try{
					String line = null;
					FileReader fileReader = new FileReader(table);
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					
					while((line = bufferedReader.readLine()) != null) {
						if(line.length() > 0){
							String[] table_data = line.split("\\|");

							if( table_data1.get(i)[join_field_indexes[0]].equals(table_data[join_field_indexes[1]])){
								Vector <String> temp = new Vector<String>();
								if(to_select_fields[0].trim().equals( "*")==false){
									for(int k = 0; k < indexes.length; k++){
										if(indexes[k] < fields1.size())
											temp.add(table_data1.get(i)[indexes[k]].replaceAll(replacement_char,"\\|"));
										else
											temp.add(table_data[indexes[k]-fields1.size()].replaceAll(replacement_char,"\\|"));
									}
								}
								else {
									for(int k = 0; k < table_data1.get(i).length; k++){
										temp.add(table_data1.get(i)[k].replaceAll(replacement_char,"|"));
									}
									for(int k = 0; k < table_data.length; k++){
										temp.add(table_data[k].replaceAll(replacement_char,"|"));
									}
								}
								select_output.add(temp);
							}

						}
					}
				}
				catch(Exception exp){
					System.out.println(exp.getMessage());
					return "Error: problem with reading from the file";
				}
			}
		}
		return output;
	}
}
