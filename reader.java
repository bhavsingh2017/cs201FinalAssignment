import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
 
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;


public class reader {
	//this is a class to read the files
	private Map<Integer, ArrayList<String> > across = new HashMap<Integer, ArrayList<String> >();
	private Map<Integer, ArrayList<String> > down = new HashMap<Integer, ArrayList<String> >();
	
	
	public Map<Integer, ArrayList<String> > getDownMap(){
		return this.down;
	}
	public Map<Integer, ArrayList<String> > getAcrossMap(){
		return this.across;
	}
	
	public boolean checkMalformed(File file) throws IOException{
		int acrossCount = 0;
		int downCount = 0;
		String st2;
		BufferedReader duplicateChecker = new BufferedReader(new FileReader(file));
		while ((st2 = duplicateChecker.readLine()) != null){
			
			if(acrossCount == 2 || downCount ==2){
				//System.out.println("there is a duplicate of 'ACROSS' or 'DOWN'!");
				return false;
			}
			if(st2.equals("ACROSS")){
				acrossCount++;
			}
			if(st2.equals("DOWN")){
				downCount++;
			}
		
		}
		return true;
	}
	
	//this will find a random filename and return 
	public String getRandomFile(List<String> results){
		Random rand = new Random();
		int n = rand.nextInt(results.size());
		String filename = results.get(n);
		return filename;
	}
	
	public void readRandomFile() throws IOException{		
		List<String> results = new ArrayList<String>();
		File[] files = new File("./gamedata").listFiles();

		for (File file : files) {
		    if (file.isFile()) {
		        results.add(file.getName());
		    }
		}
		
		String filename= "";
		///results is the name 
		
		int index = 0;
		if(results.size()==0){
			//System.out.println("There are no files");
		}else if(results.size()==1){
			//System.out.println("There is one file, it is "+files[0].getName());
			filename = results.get(0);
		}else{
			//System.out.println("Going to randomly choose a file");
			filename = getRandomFile(results);
			//System.out.println("Chose "+filename);
		}
		
		File file = new File("./gamedata/"+filename);
		
		
		/*
		 * this is where we will find out if the file is ready to be used
		 * if the file is not ready to be used then we can get delete it from the
		 * results array and then pick a new file
		*/
		
		while(true){
			boolean checkMalformed = checkMalformed(file);
			if(!checkMalformed){
				//if it is not good to be read then get a new file
				for(int i=0; i<results.size(); i++){
					//remove the index if it i not able to be read from the result array 
					if(results.get(i).equals(filename)){
						results.remove(i);
						String file_replacement = getRandomFile(results);
						file = new File("./gamedata/"+file_replacement);
						//System.out.println("The file is malformed so I'm gonna remove it from my list ");
						//System.out.println("And then try to find a new file for you!");
						continue;
					}
				}
			}else{
				//System.out.println(file.getName()+" is a healthy file!");
				break;
			}
		}
		
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		
		/*
		 * By now we should have a proper file to read 
		 * Should be able to read the whole file by now 
		 */
		
		
		String st; 
		//System.out.println("About to read!");
		//System.out.println("|||||||||||||||||||||||");
		
		
		while ((st = br.readLine()) != null){
			
			//System.out.println("Reading: "+st);
			if(st.equals("ACROSS")){
					st=br.readLine();
					while(!(st.equals("DOWN"))){
						StringTokenizer myST = new StringTokenizer(st, "|");
						String number = myST.nextToken();
						int number_int = Integer.parseInt(number);
						String answer = myST.nextToken();
						String question = myST.nextToken();
						ArrayList<String> ansQ = new ArrayList<String>();
						ansQ.add(answer);
						ansQ.add(question);
						across.put(number_int, ansQ);
						st=br.readLine();
						//System.out.println("Adding "+number+", "+answer+", "+question+"...to across!");
					}
					//System.out.println("Reading: "+st);
					st=br.readLine();
					while(st!=null){
						StringTokenizer myST = new StringTokenizer(st, "|");
						String number = myST.nextToken();
						int number_int = Integer.parseInt(number);
						String answer = myST.nextToken();
						String question = myST.nextToken();
						ArrayList<String> ansQ = new ArrayList<String>();
						ansQ.add(answer);
						ansQ.add(question);
						down.put(number_int, ansQ);
						st=br.readLine();
						//System.out.println("Adding "+number+", "+answer+", "+question+"...to down!");
					}
					break;
			}
			if(st.equals("DOWN")){
				//System.out.println("Reading: "+st);
				st=br.readLine();
				while(!st.equals("ACROSS")){
					StringTokenizer myST = new StringTokenizer(st, "|");
					String number = myST.nextToken();
					int number_int = Integer.parseInt(number);
					String answer = myST.nextToken();
					String question = myST.nextToken();
					ArrayList<String> ansQ = new ArrayList<String>();
					ansQ.add(answer);
					ansQ.add(question);
					down.put(number_int, ansQ);
					st=br.readLine();
					//System.out.println("Adding "+number+", "+answer+", "+question+"...to down!");
				}
				//System.out.println("Reading: "+st);
				st=br.readLine();
				
				while(st!=null){
					StringTokenizer myST = new StringTokenizer(st, "|");
					String number = myST.nextToken();
					int number_int = Integer.parseInt(number);
					String answer = myST.nextToken();
					String question = myST.nextToken();
					ArrayList<String> ansQ = new ArrayList<String>();
					ansQ.add(answer);
					ansQ.add(question);
					across.put(number_int, ansQ);
					st=br.readLine();
					//System.out.println("Adding "+number+", "+answer+", "+question+"...to down!");
				}
				break;
			}
		  
		}
		//System.out.println("|||||||||||||||||||||||");
	} 
}
