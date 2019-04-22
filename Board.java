import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Board {
	
	public int width=1;
	public int height=1;
	
	private Map<Integer, ArrayList<String> > across = new HashMap<Integer, ArrayList<String> >();
	private Map<Integer, ArrayList<String> > down = new HashMap<Integer, ArrayList<String> >();
	private ArrayList<ArrayList<String> > boardLayout = new ArrayList<ArrayList<String> >();
	
	
	public void setBoardDimensions(Map<Integer, ArrayList<String> > across, Map<Integer, ArrayList<String> > down){
		//this is where we go through to get the max lengths 
		
		Iterator<Map.Entry<Integer, ArrayList<String> >> itr_A = across.entrySet().iterator(); 
		Iterator<Map.Entry<Integer, ArrayList<String> >> itr_D = down.entrySet().iterator(); 
		
		
		 while(itr_A.hasNext()){ 
		         Map.Entry<Integer, ArrayList<String> > entry = itr_A.next(); 
		         ArrayList<String> array = entry.getValue();
		         width += array.get(0).length(); 
			     System.out.println("Added " + array.get(0).length()+"from "+array.get(0));

		 } 
		 while(itr_D.hasNext()){ 
		     Map.Entry<Integer, ArrayList<String> > entry = itr_D.next(); 
		     ArrayList<String> array = entry.getValue();
		     height += array.get(0).length(); 
		     System.out.println("Added " + array.get(0).length() + "from "+array.get(0));
		 }
		 
		 width = width*2;
		 height = height*2;
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
		setBoardDimensions(this.across, this.down);
	}
	
	
	
	
	public void configureBoard(){
		//this will configure the board that needs to be printed 
		
		
		
		
		return;
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
