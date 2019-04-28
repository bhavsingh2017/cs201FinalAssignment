import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.util.Pair;
import java.lang.NullPointerException;
import java.io.*;
import java.io.Serializable;


public class Board implements Serializable {
	
	public int width=1;
	public int height=1;
	public boolean isEmpty;
	public int wordsPlaced = 0;
	public int tries = 0;
	public int original_arraySize;
	public int actual_left=100000;
	public int actual_right=0;
    public int actual_top=1000000;
    public int actual_bottom=0;

	public ArrayList<Pair<ArrayList<Integer>, String> > locations_of_numbers =
            new ArrayList<Pair<ArrayList<Integer>, String> >();

	
	private Map<Integer, ArrayList<String> > across = new HashMap<Integer, ArrayList<String> >();
	private Map<Integer, ArrayList<String> > down = new HashMap<Integer, ArrayList<String> >();
	public ArrayList<ArrayList<String> > boardLayout = new ArrayList<ArrayList<String> >();

    private ArrayList<String> acrossWords = new ArrayList<String>();
	private ArrayList<String> downWords = new ArrayList<String>();
    private ArrayList<String> acrossVisited = new ArrayList<String>();
    private ArrayList<String> downVisited = new ArrayList<String>();

	public ArrayList<String> getDownWords(){
		return this.downWords;
	}
	public ArrayList<String> getAcrossWords(){
		return this.acrossWords;
	}

	public ArrayList<String> intersections = new ArrayList<String>();

	//returns a list of words that have intersections
	public ArrayList<String> findIntersections(){
        Iterator<Map.Entry<Integer, ArrayList<String> >> itr_A = this.across.entrySet().iterator();
        Iterator<Map.Entry<Integer, ArrayList<String> >> itr_D = this.down.entrySet().iterator();
        ArrayList<String> returnThis = new ArrayList<String>();
        while(itr_A.hasNext()) {
            Map.Entry<Integer, ArrayList<String> > entry = itr_A.next();
            int place_number = entry.getKey();
            ///if both of them have the key then they intersect
            if(across.containsKey(place_number) && down.containsKey(place_number)){
                returnThis.add(across.get(place_number).get(0));
                returnThis.add(down.get(place_number).get(0));
//                System.out.println(across.get(place_number).get(0)+" and "+down.get(place_number).get(0)+" intersect!");
            }
        }
        return returnThis;
    }


    public int getWordNumber(String word, boolean down){
        Iterator<Map.Entry<Integer, ArrayList<String> >> itr_A = across.entrySet().iterator();
        Iterator<Map.Entry<Integer, ArrayList<String> >> itr_D = this.down.entrySet().iterator();

        ///for an across word
        if(!down){
            while(itr_A.hasNext()){
                Map.Entry<Integer, ArrayList<String> > entry = itr_A.next();
                ArrayList<String> value = entry.getValue();
                if(value.get(0).equals(word)){
                    return entry.getKey();
                }
            }
        }
        else{
            while(itr_D.hasNext()){
                Map.Entry<Integer, ArrayList<String> > entry = itr_D.next();
                ArrayList<String> value = entry.getValue();
                if(value.get(0).equals(word)){
                    return entry.getKey();
                }
            }
        }
        return 0;
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
//            System.out.println("Added " + array.get(0).length()+"from "+array.get(0));
		 } 
		 while(itr_D.hasNext()){ 
            Map.Entry<Integer, ArrayList<String> > entry = itr_D.next();
            ArrayList<String> array = entry.getValue();
            height += array.get(0).length();
			//array.get(0) is the word
			downWords.add(array.get(0));
//		    System.out.println("Added " + array.get(0).length() + "from "+array.get(0));
		 }

//		 System.out.println("DownWords Size: "+downWords.size());
//		 System.out.println("AcrossWords Size: "+acrossWords.size());

		 width = width*2;
		 height = height*2;

        for(int i=0; i<height; i++){
            ArrayList<String> myArray = new ArrayList<String>();
            boardLayout.add(myArray);
        }

//		 System.out.println("Board size is: "+width+" by "+height);
	}
	
	public Board(){
		reader r = new reader();
		try{
			r.readRandomFile();
			this.across = r.getAcrossMap();
			this.down = r.getDownMap();
		}catch(Exception e){
//			System.out.println(e.getMessage());
		}
		intersections = findIntersections();
		setBoardDimensions(this.across, this.down);
        isEmpty=true;
        initializeBoardLayout();
        original_arraySize=this.acrossWords.size()+this.downWords.size();

        if(configureBoard(this.acrossWords, this.downWords)){
//		    System.out.println("Successfully made board");

        }else{
//		    System.out.println("Could not make board");
        }
        makeBoardWithNumbers();
        //removeLetters();
        //printPretty();


	}
	
	

	/*
	idea for the recursive function

	if(it is empty) try placing the first word and try to see if you can find combinations for the
	across word and all the down words.

	if there were no location matches then you need to recursively try to try the next word in across
	if there is a match you can just repeat this
	 */
	
	public boolean configureBoard(ArrayList<String> acrossWords, ArrayList<String> downWords){


        String red = "\u001B[35m";
//        //System.out.println(red+"Calling configure board!");

//        System.out.println(red+"AcrossWordList: ");
        //for(String word : acrossWords) System.out.print(word+",");
//        System.out.println();
//        System.out.println(red+"DownWordList: ");
        //for(String word : downWords) System.out.print(word+",");
        //System.out.print("\u001B[37m");
//        System.out.println();

		//this will configure the board that needs to be printed
        String myWord = acrossWords.get(0);
        boolean placeableValidity;

        if(acrossWords.size()==0 && downWords.size()==0){
            //String Red = "\u001B[32m";
//            System.out.println(Red+"Hit the base case!");
           // String White = "\u001B[37m";
           // System.out.print(White);
            return true;
        }

        if(isEmpty){
            //you can always add the first word because it is empty
            placeableValidity = placeWordValidity(myWord,width/2,height/2, false);
            acrossVisited.add(myWord);
            acrossWords.remove(myWord);
            isEmpty=false;
//            System.out.println("Placing word!!!!!!!!");
            boolean config = configureBoard(acrossWords, downWords);
            if(config) return true;
        }else{
            boolean noLocations = true;
            List<String> found = new ArrayList<String>();
            String word;


            while (!downWords.isEmpty() && ! acrossWords.isEmpty() &&
                    downVisited.size()+acrossVisited.size()!=original_arraySize) {

//                System.out.println("gonna check down words first!");
                for (int i = 0; i < downWords.size(); i++) {
                    word = downWords.get(i);

//                    System.out.println();
//                    System.out.println("*************************** " + word + " *************************** ");
//                    System.out.println();
//                    System.out.println("It_down has next");
                    boolean word_placed = false;
                    ArrayList<Pair<Integer, Integer>> myList = getPossibleLocations(word, true);
                    for (Pair<Integer, Integer> myPair : myList) {
                        boolean valid = placeWordValidity(word, myPair.getKey(), myPair.getValue(), true);
                        if (valid) {
                            //the word has been placed and is in a valid position
                            word_placed = true;
                            // found.add(word);
                            downWords.remove(word);
                            downVisited.add(word);
                            noLocations = false;
//                            System.out.println();
//                            System.out.println(red + "Not going to try and place " + word + " anymore" + "\u001B[37m");
                            break;
                            //we dont want to place more words if that one already worked
                        }
                    }

////                    System.out.println(red + "AcrossWordList: ");
//                    for (String word1 : acrossWords) //System.out.print(word1 + ",");
////                    System.out.println();
////                    System.out.println(red + "DownWordList: ");
//                    for (String word1 : downWords) //System.out.print(word1 + ",");
////                    System.out.println();
////                    System.out.println(red + "AcrossVisitedList: ");
//                    for (String word1 : acrossVisited) //System.out.print(word1 + ",");
////                    System.out.println();
////                    System.out.println(red + "DownVisitedList: ");
//                    for (String word1 : downVisited) //System.out.print(word1 + ",");

                    if (noLocations || !word_placed) {
                        //add back the down words to restart

//                        System.out.println("\u001B[34m" + "There were no locations");
//                        System.out.println("I COULD NOT FIND A SPOT FOR " + word.toUpperCase());
                        clearBoard();
                        isEmpty = true;
//                        System.out.println(red + "AcrossWordList: ");

                        acrossWords.addAll(acrossVisited);
                        downWords.addAll(downVisited);

//                        for (String word1 : acrossWords) System.out.print(word1 + ",");
////                        System.out.println();
////                        System.out.println(red + "DownWordList: ");
//                        for (String word1 : downWords) System.out.print(word1 + ",");
                        //System.out.print("\n");
                        downVisited.clear();
                        acrossVisited.clear();
                        configureBoard(acrossWords, downWords);
                    }

                }

                //if none of the down worked try the across//
                // found.clear();
//                System.out.println("gonna check across words first!");
                noLocations = true;
                for (int i = 0; i < acrossWords.size(); i++) {
                    word = acrossWords.get(i);
//                    System.out.println();
//                    System.out.println("*************************** " + word + " *************************** ");
//                    System.out.println();
//                    System.out.println("It_across has next");
                    boolean word_placed = false;
//                    System.out.println("IM ON WORD: " + word);
                    ArrayList<Pair<Integer, Integer>> myList = getPossibleLocations(word, false);
                    for (Pair<Integer, Integer> myPair : myList) {
                        boolean valid = placeWordValidity(word, myPair.getKey(), myPair.getValue(), false);
                        if (valid) {
                            //the word has been placed and is in a valid position
                            word_placed = true;
                            //found.add(word);
                            acrossVisited.add(word);
                            acrossWords.remove(word);
                            noLocations = false;
//                            System.out.println();
//                            System.out.println(red + "Not going to try and place '" + word + "' anymore" + "\u001B[37m");
                            break;
                            //we dont want to place more words if that one already worked
                        }
                    }
//                    System.out.println(red + "AcrossWordList: ");
//                    for (String word1 : acrossWords) System.out.print(word1 + ",");
////                    System.out.println();
////                    System.out.println(red + "DownWordList: ");
//                    for (String word1 : downWords) System.out.print(word1 + ",");
////                    System.out.println();
////                    System.out.println(red + "AcrossVisitedList: ");
//                    for (String word1 : acrossVisited) System.out.print(word1 + ",");
////                    System.out.println();
////                    System.out.println(red + "DownVisitedList: ");
//                    for (String word1 : downVisited) System.out.print(word1 + ",");

                    if (noLocations || !word_placed) {
                        //add back the down words to restart

//                        System.out.println("\u001B[34m" + "There were no locations");
                        //for(String reword1 : downVisited) downWords.add(reword1);
                        //for(String reword1 : acrossVisited) acrossWords.add(reword1);
                        //acrossWords.remove(acrossVisited.get(0));
                        //acrossWords.add(word);
                        clearBoard();
                        isEmpty = true;
                        acrossWords.addAll(acrossVisited);
                        downWords.addAll(downVisited);
//                        System.out.println(red + "AcrossWordList: ");
                        for (String word1 : acrossWords) System.out.print(word1 + ",");
//                        System.out.println();
//                        System.out.println(red + "DownWordList: ");
                        for (String word1 : downWords) System.out.print(word1 + ",");
                        System.out.print("\n");

                        downVisited.clear();
                        acrossVisited.clear();
                        configureBoard(acrossWords, downWords);
                    }
                    ///if there were no locations for that word, then we need to move to a different word
                }
            }
            //found.clear();
        }
        //printBoard();

        return acrossWords.size()==0 && downWords.size()==0;
	}


	public void initializeBoardLayout(){
	    for(int i=0; i<this.height; i++){
	        for(int j=0; j<this.width; j++){
	            boardLayout.get(i).add(".");
	            //boardLayoutValidity.get(i).add(false);
            }
        }
    }
    public void clearBoard(){
        for(int i=0; i<this.height; i++){
            for(int j=0; j<this.width; j++){
                boardLayout.get(i).set(j,".");

            }
        }
    }


    //returns the word with that key
    public String findWordWithKey(int key, boolean down){
        Iterator<Map.Entry<Integer, ArrayList<String> >> itr_A = across.entrySet().iterator();
        Iterator<Map.Entry<Integer, ArrayList<String> >> itr_D = this.down.entrySet().iterator();

        if(!down) {
            while (itr_A.hasNext()) {
                Map.Entry<Integer, ArrayList<String>> entry = itr_A.next();
                int num = entry.getKey();
                if(num==key){
                    return entry.getValue().get(0);
                }
                //array.get(0) is the word
            }
        }else{
            while (itr_D.hasNext()) {
                Map.Entry<Integer, ArrayList<String>> entry = itr_D.next();
                int num = entry.getKey();
                if(num==key){
                    return entry.getValue().get(0);
                }
                //array.get(0) is the word
            }
        }
        return " ";
    }

    public int keyOfWord(String word, boolean down){
        Iterator<Map.Entry<Integer, ArrayList<String> >> itr_A = across.entrySet().iterator();
        Iterator<Map.Entry<Integer, ArrayList<String> >> itr_D = this.down.entrySet().iterator();

        if(!down) {
            while (itr_A.hasNext()) {
                Map.Entry<Integer, ArrayList<String>> entry = itr_A.next();
                ArrayList<String> num = entry.getValue();
                if(num.get(0).equals(word)){
                    return entry.getKey();
                }
                //array.get(0) is the word
            }
        }else{
            while (itr_D.hasNext()) {
                Map.Entry<Integer, ArrayList<String>> entry = itr_D.next();
                ArrayList<String> num = entry.getValue();
                if(num.get(0).equals(word)){
                    return entry.getKey();
                }
                //array.get(0) is the word
            }
        }
        return 0;
    }




	public void printArray(ArrayList<String> a){
		for(int i=0; i<a.size(); i++){
//			System.out.println(a.get(i));
		}
	}
	//put in the word and  return the locations of where you can put the location
	public ArrayList<Pair<Integer, Integer> > getPossibleLocations(String word, boolean down){
        String Green = "\u001B[33m";
        String White = "\u001B[37m";
//	    System.out.println("Going to find possible locations for: "+word);
//	    System.out.println("Needs to be placed downwards--->"+down);
        ArrayList<Pair<Integer, Integer> > returnMe = new ArrayList<Pair<Integer, Integer> >();
        for(int k=0; k<word.length(); k++) {
            String letter = word.substring(k, k+1);
//            System.out.println("Getting possible locations for '" + letter + "' in word '" + word + "'...");
            for (int i = 0; i < this.boardLayout.size(); i++) {
                ArrayList<String> row = boardLayout.get(i);
                //only if there is a need to go through it
                if (row.contains(letter)) {
                    for (int j = 0; j < row.size(); j++) {
                        if (row.get(j).equals(letter)) {
//                            System.out.println(Green+"Found "+letter);
                            System.out.print(White);
                            if(down){
                                if(!intersections.contains(word) && !isEmpty) {
                                    if (boardLayout.get(i - 1).get(j).equals(".") && boardLayout.get(i + 1).get(j).equals(".")) {
                                        Pair<Integer, Integer> ans = new Pair<Integer, Integer>(j, i);
                                        returnMe.add(ans);
//                                        System.out.println(Green + "//Possible position at (" + j + "," + i + ")//");
                                        System.out.print(White);
                                    }
                                }else{
                                    //if it is in the intersections array, get its number
                                    int location = getWordNumber(word, down);
                                    String wordMustBeginWith = findWordWithKey(location, !down).substring(0,1);
//                                    System.out.println("THE WORD MUST BEGIN WITH -->"+wordMustBeginWith);

                                    boolean valid_spot = (boardLayout.get(i).get(j).equals(wordMustBeginWith));

//                                    System.out.println("VALID_SPOT: "+valid_spot);
//                                    System.out.println("wordMustBeginWith: '"+wordMustBeginWith+"'");
//                                    System.out.println("wordHereIs: '"+boardLayout.get(i).get(j)+"'");

                                    if (valid_spot && boardLayout.get(i - 1).get(j).equals(".") && boardLayout.get(i + 1).get(j).equals(".")) {
                                        Pair<Integer, Integer> ans = new Pair<Integer, Integer>(j, i);
                                        returnMe.add(ans);
//                                        System.out.println(Green + "//Possible position at (" + j + "," + i + ")//");
                                        System.out.print(White);
                                    }
                                }
                            }else{
                                if(!intersections.contains(word) && !isEmpty) {
                                    if (boardLayout.get(i).get(j + 1).equals(".") && boardLayout.get(i).get(j - 1).equals(".")) {
                                        Pair<Integer, Integer> ans = new Pair<Integer, Integer>(j, i);
                                        returnMe.add(ans);
//                                        System.out.println(Green + "//Possible position at (" + j + "," + i + ")//");
                                        System.out.print(White);
                                    }
                                }else{
                                    int location = getWordNumber(word, down);
                                    String wordMustBeginWith = findWordWithKey(location, !down).substring(0,1);
//                                    System.out.println("THE WORD MUST BEGIN WITH -->"+wordMustBeginWith);

                                    boolean valid_spot = (boardLayout.get(i).get(j).equals(wordMustBeginWith));

//                                    System.out.println("VALID_SPOT: "+valid_spot);
//                                    System.out.println("wordMustBeginWith: '"+wordMustBeginWith+"'");
//                                    System.out.println("wordHereIs: '"+boardLayout.get(i).get(j)+"'");

                                    if (valid_spot && boardLayout.get(i - 1).get(j).equals(".") && boardLayout.get(i + 1).get(j).equals(".")) {
                                        Pair<Integer, Integer> ans = new Pair<Integer, Integer>(j, i);
                                        returnMe.add(ans);
//                                        System.out.println(Green + "//Possible position at (" + j + "," + i + ")//");
                                        System.out.print(White);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

//        System.out.println(word+" has "+returnMe.size() +" potential spots!");
        return returnMe;
    }

    public void putWord(String word, int x, int y, boolean down){
	    //System.out.print("Word was valid going to place-->"+word);
	    //System.out.print("-->at("+(x)+","+(y)+")");

        //x=x+1;
        //y=y+1;
	    String letter_at_pos = boardLayout.get(y).get(x);
//	    System.out.println("letter-->"+letter_at_pos);


	    //this is if the first letters are not the same//
        if(word.substring(word.length()-1,word.length()).equals(letter_at_pos) && !isEmpty){
//            System.out.println("HERE1");
            if(down) {
                y = y - word.length() - 1;
            }else{
                x = x - word.length() - 1;
            }
        }

        //if the position is not the first and last word
	    if(!word.substring(0,1).equals(letter_at_pos) &&
                !word.substring(word.length()-1,word.length()).equals(letter_at_pos) && !isEmpty){
//            System.out.println("HERE2");
            int loc = word.indexOf(letter_at_pos);
//            System.out.println("indexOf-->"+loc);
            if(down){
                y = y-loc;
            }else{
                x=x-loc;
            }
        }

        //^^this method will shift the word eith up or down^^//

//        System.out.println("X!->"+x+" Y!->"+y);
        ArrayList<Integer> l = new ArrayList<Integer>();

//        public int actual_left=100000;
//        public int actual_right=0;
//        public int actual_top=1000000;
//        public int actual_bottom=0;


        if(x<actual_left){
            actual_left=x;
        }

        if(x>actual_right){
            actual_right=x+word.length();
        }

        if(y>actual_bottom){
            actual_bottom=y+word.length();
        }

        if(y<actual_top){
            actual_top=y;
        }



       l.add(x);
       l.add(y);

        String mine = String.valueOf(keyOfWord(word, down));
        Pair<ArrayList<Integer>, String> myPair = new Pair<ArrayList<Integer>, String>(l, mine);
        if(!locations_of_numbers.contains(myPair)){
            //if it doesnt contain it
            locations_of_numbers.add(myPair);
        }else{
            locations_of_numbers.remove(myPair);
            locations_of_numbers.add(myPair);
        }


        if(down){
//            System.out.println("HERE3");
            if(isEmpty) isEmpty=false;
            wordsPlaced++;
            for(int i=y; i<y+word.length(); i++){
                boardLayout.get(i).set(x, word.substring(i - y, i - y + 1));
            }
        }
        if(!down){
            wordsPlaced++;
//            System.out.println("HERE4");
            if(isEmpty) isEmpty=false;
            for(int i=x; i<x+word.length(); i++){
                    boardLayout.get(y).set(i, word.substring(i-x,i-x+1));
            }
        }
//        printBoard();
//	    if(word.equals("dodgers")){
//	        System.exit(0);
//        }
    }



	public boolean placeWordValidity(String word, int x, int y, boolean down){
		//if it is empty
        String Green = "\u001B[33m";
        String White = "\u001B[37m";
//        System.out.println("Checking validity for "+word);
        int original_y = y;
        int original_x =x;
        int size_of_word = word.length();
        int gap = (this.width-size_of_word)/2;
//        System.out.println("IM AT "+x+","+y+"-->"+boardLayout.get(y).get(x));
        String last_letter = word.substring(word.length()-1,word.length());
        String first_letter = word.substring(0,1);
//        System.out.println("first letter: "+first_letter);
//        System.out.println("last letter: "+last_letter);

		if(isEmpty) {
//		    System.out.println("First Word");
            putWord(word,x,y, false);
            return true;
		}else{
            ////ACROSS////
            if(!down) {
                ArrayList<String> row = boardLayout.get(y);
                String aboveValue = "";
                String belowValue = "";
                boolean hasValidBelow = true;
                boolean hasValidAbove = true;
                if (y == 0) {
                    hasValidAbove = false;
                }
                if (y == boardLayout.size() - 1) {
                    hasValidBelow = false;
                }


                ///added this ///

                int loc=0;
                String wordAT = boardLayout.get(y).get(x);
                if(!first_letter.equals(wordAT) && !last_letter.equals(wordAT)){
                    //if its not
                    loc = word.indexOf(wordAT);
                    size_of_word = size_of_word-loc-1;
                }

                ///added this///

                for (int i = 1; i < size_of_word; i++) {
                    ///ignore if the location we found is the character
                    if (hasValidBelow) {
                        belowValue = boardLayout.get(y + 1).get(x + i);
                    }
                    if (hasValidAbove) {
                        aboveValue = boardLayout.get(y - 1).get(x + i);
                    }
                    if (!belowValue.equals(".") || !aboveValue.equals(".")) {
//                        System.out.println("B:"+belowValue+"---U:"+aboveValue);
//                        System.out.println("Cannot insert between words");
                        return false;
                    }
                }
                x=original_x-1;

                //added this//
                if(!first_letter.equals(wordAT) && !last_letter.equals(wordAT)){
                    //if its not
                    loc = word.indexOf(wordAT);
                    size_of_word = loc+1;
                }
                //added this//

                for (int i = 1; i < size_of_word; i++) {
                    //the value under it

                    if (hasValidBelow) {
                        try {
                            belowValue = boardLayout.get(y + 1).get(x - i);
                        }catch(NullPointerException nle){
                            return false;
                        }
                    }
                    if (hasValidAbove) {
                        try{
                            aboveValue = boardLayout.get(y - 1).get(x - i);
                        }catch(NullPointerException nle){
                            return false;
                        }

                    }

                    if (!belowValue.equals(".") || !aboveValue.equals(".")) {
//                        System.out.println("B:"+belowValue+"---U:"+aboveValue);

//                        System.out.println("Cannot insert between words");
                        return false;
                    }
                }
//                System.out.println("I want to put the word at "+original_x+","+original_y+" at "+boardLayout.get(y).get(x));
                putWord(word, original_x , original_y , false);
            }else{
                ////DOWN////


                String leftValue = "";
                String rightValue = "";

                y=y+1;

                boolean hasValidRight = true;
                boolean hasValidLeft = true;
                if (x == 0) {
                    hasValidLeft = false;
                }
                if (y == width - 1) {
                    hasValidRight = false;
                }

//                System.out.println("Current x: "+x);
//                System.out.println("Current y: "+y);


                for (int i = 0; i < size_of_word; i++) {
                    //the value under it


                    if (hasValidLeft) {
                        try{
                            leftValue = boardLayout.get(y + i).get(x-1);
                            System.out.print(leftValue);
                        }catch(Exception e){
                            return false;
                        }
                    }
                    if (hasValidRight) {
                        try {
                            rightValue = boardLayout.get(y + i).get(x + 1);
                            System.out.print(rightValue);
                        }catch(Exception e){
                            return false;
                        }
                    }

//                    System.out.println();

                    if (!rightValue.equals(".") || !leftValue.equals(".")) {
//                        System.out.println("First");
//                        System.out.println("L:"+leftValue+"---R:"+rightValue);
//                        System.out.println("\u001B[32m"+"Bottom Check Failed");
                        System.out.print(White);
                        return false;
                    }
                }
                y=original_y-1;
                for (int i = 0; i < size_of_word; i++) {

                    if (hasValidRight) {
                        try {
                            rightValue = boardLayout.get(y - i).get(x + 1);
                        }catch(NullPointerException nle){
                            return false;
                        }
                    }
                    if (hasValidLeft) {
                        try {
                            leftValue = boardLayout.get(y - i).get(x - 1);
                        }catch(NullPointerException nle){
                            return false;
                        }
                    }

                    if (!rightValue.equals(".") || !leftValue.equals(".")) {
//                        System.out.println("L:"+leftValue+"....R:"+rightValue);
//                        System.out.println("\u001B[32m"+"Upper Check Failed");
                        System.out.print(White);
//                        System.out.println("Cannot insert between words");
                        return false;
                    }
                }

//                System.out.println("I want to put the word at "+original_x+","+original_y+" at "+boardLayout.get(y).get(x));
                putWord(word, original_x , original_y , true);
            }
            return true;
        }
	}

	public void removeWord(String word, int x, int y){
//	    System.out.println("Removing: "+word);
	    for(int i=0; i<word.length(); i++){
	        boardLayout.get(y).set(x+i, ".");
        }
	}
	public void printBoard(){
        String Green = "\u001B[33m";
        String White = "\u001B[37m";
        String Other = "\u001B[34m";

//        System.out.println();

        for(int i=0; i<this.width; i++){
            System.out.print(Other+" _");
        }
        System.out.print(White);
//        System.out.println();

        int length = this.boardLayout.size();
		for(int i=0; i<length; i++){
            System.out.print(" ");
			for(int j=0; j<boardLayout.get(i).size(); j++){
				String value = this.boardLayout.get(i).get(j);
				if(value!=(".")){
                    System.out.print(Green+value);
                }else{
                    System.out.print(White+value);
                }
				System.out.print(" ");
			}
			System.out.print("\n");
		}
//        System.out.println();
        for(int i=0; i<this.width; i++){
            System.out.print(Other+" _");
        }
        System.out.print(White);
    }

    public void makeBoardWithNumbers(){
//	    System.out.println("These many numbers: "+locations_of_numbers);
	    for(int i=0; i<locations_of_numbers.size(); i++){
	        //get the pairs
	        Pair<ArrayList<Integer>, String > myPair = locations_of_numbers.get(i);
	        int x = myPair.getKey().get(0);
	        int y = myPair.getKey().get(1);
	        boardLayout.get(y).set(x, myPair.getValue()+boardLayout.get(y).get(x));

        }

        fixSpaces();
//        printBoard();
    }


    public void printPretty(){
        String Green = "\u001B[33m";
        String White = "\u001B[37m";
        String Other = "\u001B[34m";

//        System.out.println();

        for(int i=0; i<actual_right; i++){
            System.out.print(Other+" _");
        }
        System.out.print(White);
//        System.out.println();

        int length = this.boardLayout.size();
        for(int i=actual_top; i<=actual_bottom; i++){
            System.out.print(" ");
            for(int j=actual_left; j<=actual_right; j++){
                String value = this.boardLayout.get(i).get(j);
                if(value!=(".")){
                    System.out.print(Green+value);
                }else{
                    System.out.print(White+value);
                }
                System.out.print(" ");
            }
            System.out.print("\n");
        }
//        System.out.println();
        for(int i=0; i<actual_right; i++){
            System.out.print(Other+" _");
        }
        System.out.print(White);
    }

    public void fixSpaces(){
        for(int i=0; i<this.width; i++){
            for(int j=0; j<this.height; j++){
                if(boardLayout.get(j).get(i).length()!=2){
                    boardLayout.get(j).set(i, " "+boardLayout.get(j).get(i));
                }
            }
        }

    }

    public void removeLetters(){
        for(int i=0; i<this.width; i++){
            for(int j=0; j<this.height; j++){
                if(!boardLayout.get(j).get(i).substring(1,2).equals(".")){
                    if(!boardLayout.get(j).get(i).substring(0,1).equals(" ")) {
                        boardLayout.get(j).set(i, boardLayout.get(j).get(i).substring(0,1)+"_");
                    }else{
                        boardLayout.get(j).set(i, " _");
                    }
                }else{
                    boardLayout.get(j).set(i, "  ");
                }
            }
        }
        fixSpaces();
//        printBoard();

//        System.out.println("AL: "+actual_left);
//        System.out.println("AR: "+actual_right);
//        System.out.println("AT: "+actual_top);
//        System.out.println("AB: "+actual_bottom);

//        printPretty();
    }


	
}
