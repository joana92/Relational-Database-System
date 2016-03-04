import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Vector;
import java.io.*;

public class Sql extends JPanel {
	JTextArea input;
	JTextArea output;
	JPanel bottom;
	JPanel top;


	/*
	 * Constructor which creates the GUI components
	 *
	*/	
	public Sql() { 
		super(new BorderLayout());
		input = new JTextArea(10, 100);
		output = new JTextArea(100, 300);
		output.setEditable(false);
			
		
		top = new JPanel();
		bottom = new JPanel();
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
		add(splitPane, BorderLayout.CENTER);	

		top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
		top.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		top.setBorder(BorderFactory.createTitledBorder("Enter your query here"));
		
		top.add(input);

		JButton b1 = new JButton("Submit");
		top.add(b1);
				   
		JButton b2 = new JButton("Clear");
		top.add(b2);

		top.setMinimumSize(new Dimension(200, 50));
		top.setPreferredSize(new Dimension(200, 150));


		bottom.setLayout(new BoxLayout(bottom, BoxLayout.PAGE_AXIS));

		bottom.add(output);
		bottom.setMinimumSize(new Dimension(400, 50));
		bottom.setPreferredSize(new Dimension(450, 200));
		     		
		   	  
		   
		b1.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				String s1 = input.getText();
				run_query(s1);
			}
		});

		b2.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				input.setText(" ");
				bottom.removeAll();
				bottom.updateUI();
				bottom.add(output);
			}
		});
	}

	public void displaySelectResult(String output_file_name, Vector<Vector<String>> select_output, Vector<String> headers){
		
		String print_line = "--------------------------------------------------------------------------------------------------------";
		String line="";


		for(int i = 0; i < headers.size(); i++){
			line += headers.get(i)+"\t|\t"; 
		}
		if(output_file_name.length()==0)
			System.out.println(print_line+"\n"+line+"\n"+print_line);
		else{
				try {
					FileWriter fileWritter = 
							new FileWriter(output_file_name,true);
					BufferedWriter bufferWritter = 
							new BufferedWriter(fileWritter);
					bufferWritter.write( print_line + "\n" + line+"\n" + print_line + "\n");
					bufferWritter.close();
				}
				catch (Exception e){
					System.err.println(e.getMessage());
				}
		}


		for(int i = 0; i < select_output.size(); i++){
			line = "";
			for(int j = 0; j < select_output.get(i).size(); j++){
				line += select_output.get(i).get(j)+"\t|\t";
			}

			if(output_file_name.length() == 0)
				System.out.println(line+"\n" + print_line);
			else {
				try {
					FileWriter fileWritter = 
							new FileWriter(output_file_name,true);
					BufferedWriter bufferWritter = 
							new BufferedWriter(fileWritter);
					bufferWritter.write( line+"\n" + print_line + "\n");
					bufferWritter.close();
				}
				catch (Exception e){
					System.err.println(e.getMessage());
				}
			}

		}
	}

	/*
	 * executes one query at a time using the TinySql clss instance
	 *
	*/	
	public boolean run_single_query(String command, boolean flag, String output_file_name, int line_count){
		String message;
		TinySql a = new TinySql();
		String print_line = "--------------------------------------------------------------------------------------------------------";
		

		if(command.length() < 6){
			message = "Error: Invalid command";
		}
		else {
			switch(command.substring(0,6)){
				case "CREATE":
					message = a.runCreate(command);
					break;
				case "INSERT":
					//output += "insert";
					message = a.runInsert(command);
					break;
				case "SELECT":
					
					Vector <Vector<String>> select_output = new Vector<Vector<String>>();
					Vector <String> headers = new Vector<String>();
					message = a.runSelect(command, select_output, headers);
					if(!message.startsWith("Error")){
						if(flag) {	
						    JTable table = new JTable(select_output, headers);
					        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
					        table.setFillsViewportHeight(true);	
					        JScrollPane scrollPane = new JScrollPane(table);

					        //Add the scroll pane to this panel.
					        bottom.add(scrollPane);
					    }
					    else {
					    	displaySelectResult(output_file_name, select_output, headers);
					    }
					}
					
					break;
				default:
					message = "Error: Invalid command\n";
					break;
			}
		}

		if(message.length() > 0){
			if(flag)
				bottom.add(new JTextArea("Line no. "+line_count+": "+message, 1, 1));
			else if(output_file_name.length() == 0){
				System.out.println(print_line+"\n"+"Line no. "+line_count+": "+message);
			}
			else{
				try {
					FileWriter fileWritter = 
							new FileWriter(output_file_name,true);
					BufferedWriter bufferWritter = 
							new BufferedWriter(fileWritter);
					bufferWritter.write( print_line+"\n" + "Line no. "+line_count+": "+ message + "\n");
					bufferWritter.close();
				}
				catch (Exception e){
					System.err.println(e.getMessage());
				}
			}
		}
		if(message.startsWith("Error"))
			return true;
		else
			return false;
	}

	/*
	 * runs query for the GUI
	 *
	*/	
	public void run_query(String commandSet){
		
		String[] commandArray = commandSet.split("\\n+");
		TinySql a = new TinySql();		   
		bottom.removeAll();
		bottom.updateUI();

		String message;
		boolean isValid = false;

		for( int i = 0; i < commandArray.length; i++){
			commandArray[i] = commandArray[i].trim();
				if(commandArray[i].trim().length() > 0) {
				boolean flag = run_single_query(commandArray[i], true, "", i+1);
				
				if(flag)
					break;
			}
		}
	}

	/*
	 * runs the GUI
	 *
	*/
	private static void showGUI() {
		JFrame frame = new JFrame();		   
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Database Project");
		Sql demo = new Sql();
		demo.setOpaque(true);
		frame.setContentPane(demo);
		frame.pack();
		frame.setVisible(true);
	}

		
	/*
	 * main function 
	 *
	*/	
	public static void main(String[] args) {
		
		if(args.length == 0){
			SwingUtilities.invokeLater(new Runnable() { 
				@Override
				public void run() {
					showGUI();
				}
			});
		}
		else if (args.length <= 2){

			Sql bb = new Sql();

			String input_file_name = args[0].trim();
			String output_file_name = "";
			if(args.length == 2 && args[1].trim().length() > 0){
				output_file_name = args[1].trim();
				
				try{
					File output = new File(output_file_name);
					if(output.exists()){
						output.delete();
					}
					output.createNewFile();
				}
				catch (Exception e){
					System.err.println(e.getMessage());
				}
			}

			try {
				String line = null;
				FileReader fileReader = new FileReader(input_file_name);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				int line_count = 0;

				while((line = bufferedReader.readLine()) != null) {
					line_count++;
					if(line.trim().length() > 0){
						boolean flag = bb.run_single_query(line.trim(), false, output_file_name, line_count);
						if(flag)
							return;
							

						
					}
				}
			}
		    catch(FileNotFoundException ex) {
		      	System.out.println("Unable to open file '" + input_file_name + "'");          
		    }
		    catch(IOException ex) {
		      	System.out.println("Error reading file '" + input_file_name + "'");
		    }

		}
		else{
			System.out.println("The structure is: java Sql input-file-name[Optional] output-file-name[Optional]");

		}
	}
}		

			  
   				  
				   
