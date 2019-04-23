import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Board {
	
	public int width=1;
	public int height=1;
	public boolean isEmpty;
	
	private Map<Integer, ArrayList<String> > across = new HashMap<Integer, ArrayList<String> >();
	private Map<Integer, ArrayList<String> > down = new HashMap<Integer, ArrayList<String> >();
	private ArrayList<ArrayList<String> > boardLayout = new ArrayList<ArrayList<String> >();
	private ArrayList<String> acrossWords = new ArrayList<String>();
	private ArrayList<String> downWords = new ArrayList<String>();


	public ArrayList<String> getDownWords(){
		return this.downWords;
	}
	public ArrayList<String> getAcrossWords(){
		return this.acrossWords;
	}
	
	public void setBoardDimensions(Map<Integer, ArrayList<String> > across, Map<Integer, ArrayList<String> > down){
		//this is where we go through to get the max lengths 
		
		Iterator<Map.Entry<Integer, ArrayList<String> >> itr_A = across.entrySet().iterator(); 
		Iterator<Map.Entry<Integer, ArrayList<String> >> itr_D = down.entrySet().iterator();



		while(itr_A.hasNext()){
		         Map.Entry<Integer, ArrayList<String> > entry = itr_A.next(); 
		         ArrayList<String> array = entry.getValue();
				 width += array.get(0).length(); 
				 //array.get(0) is the word
				acrossWords.add(array.get(0));
				System.out.println("Added " + array.get(0).length()+"from "+array.get(0));
		 } 
		 while(itr_D.hasNext()){ 
		     Map.Entry<Integer, ArrayList<String> > entry = itr_D.next(); 
		     ArrayList<String> array = entry.getValue();
			 height += array.get(0).length(); 
			//array.get(0) is the word
			downWords.add(array.get(0));
		    System.out.println("Added " + array.get(0).length() + "from "+array.get(0));
		 }

		 System.out.println("DownWords Size: "+downWords.size());
		 System.out.println("AcrossWords Size: "+acrossWords.size());

		 width = width*2;
		 height = height*2;

        for(int i=0; i<height; i++){
            ArrayList<String> myArray = new ArrayList();
            boardLayout.add(myArray);
        }

		 System.out.println("Board size is: "+width+" by "+height);
	}
	
	public Board(){
		reader r = new reader();
		try{
			r.readRandomFile();
			this.across = r.getAcrossMap();
			this.down = r.getDownMap();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		isEmpty=true;
		setBoardDimensions(this.across, this.down);
		configureBoard();
	}
	
	
	
	
	public boolean configureBoard(){
		//this will configure the board that needs to be printed
		placeWord(acrossWords.get(0));
		return false;
	}

	public void printArray(ArrayList<String> a){
		for(int i=0; i<a.size(); i++){
			System.out.println(a.get(i));
		}
	}


	public void placeWord(String word){
		//if it is empty
		if(isEmpty){
			ArrayList<String> myArray = boardLayout.get(this.height/2);
			int size_of_word = word.length();
			int gap = (this.width-size_of_word)/2;
			//fill the first half

			for(int i=0; i<width; i++){
				myArray.add("--");
				if(i==gap-1) {
					//if we are right before the gap place the word
					for (int j=1; j<=size_of_word; i++){
						myArray.add(word.substring(1, j));
					}
				}
			}
			isEmpty=false;
			printArray(myArray);
		}

	}

	public void removeWord(String word){

	}





	public void printBoard(){
		int length = this.boardLayout.get(0).size();
		for(int i=0; i<length; i++){
			for(int j=0; j<length; j++){
				String value = this.boardLayout.get(i).get(j);
				if(value.equals("")) value="_";
				System.out.print(value+" ");
			}
			System.out.println("\n");
		}
	}
	
}
