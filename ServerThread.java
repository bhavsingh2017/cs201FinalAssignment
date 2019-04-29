import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.Scanner;
import java.io.Serializable;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import javafx.util.Pair;

public class ServerThread extends Thread {

	private PrintWriter pw;
	private BufferedReader br;
	private gameChatRoom cr;
	private Socket socket;
	int score =0;
	int playerNumber=0;

	
	private Lock lock;
	private Condition canMessage;
	private boolean isFirst=false;
	private int playerCount=0;

	public Socket getSocket(){
		return socket;
	}
	
	public ServerThread(Socket s, gameChatRoom cr, Lock lock, Condition canMessage, boolean isFirst) {
		try {
			this.playerNumber = cr.currentGameSize()+1;
			this.cr = cr;
			this.lock = lock;
			this.socket = s;
			this.canMessage = canMessage;
			this.isFirst = isFirst;
			pw = new PrintWriter(s.getOutputStream());
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.start();
		} catch (IOException ioe) {
			System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
		}
	}

	public void printBoardAndQuestions(){

		cr.printMessage("");
		cr.printMessage("");
		cr.printMessage("Sending game board.");

		for(int i=cr.copy.actual_top; i<=cr.copy.actual_bottom; i++){
			System.out.print(" ");
			String x =" ";
			for(int j=cr.copy.actual_left; j<=cr.copy.actual_right; j++){
				String value = cr.copy.boardLayout.get(i).get(j);
				x+=value+" ";
			}
			cr.broadcast(x);
		}

		Iterator<Map.Entry<Integer, ArrayList<String> >> itr_A = cr.across.entrySet().iterator();
		Iterator<Map.Entry<Integer, ArrayList<String> >> itr_D = cr.down.entrySet().iterator();

		cr.broadcast("ACROSS");
		while(itr_A.hasNext()) {
			Map.Entry<Integer, ArrayList<String> > entry = itr_A.next();
			String place_number =String.valueOf(entry.getKey());
			String q = entry.getValue().get(1);
			cr.broadcast(place_number+" "+q);
			///if both of them have the key then they intersect
		}
		cr.broadcast("DOWN");
		while(itr_D.hasNext()) {
			Map.Entry<Integer, ArrayList<String> > entry = itr_D.next();
			String place_number =String.valueOf(entry.getKey());
			String q = entry.getValue().get(1);
			cr.broadcast(place_number+" "+q);
		}
	}


	public void sendMessage(String message) {
		//System.out.println("In SERVER, going to send: "+message);
		pw.println(message);
		pw.flush();
	}

	public boolean checkCorrect(int guessedNumber, String word, boolean down){
		if(down){
			//check the down map
			ArrayList<String> list = cr.down.get(guessedNumber);
			if(word.equals(list.get(0))){
				//need to remove this as well//
				cr.down.remove(guessedNumber);
				return true;
			}else{
				return false;
			}

		}else{
			//check the across map
			ArrayList<String> list = cr.across.get(guessedNumber);
			if(word.equals(list.get(0))){
				//need to remove this listing as well//
				cr.across.remove(guessedNumber);
				return true;
			}else{
				return false;
			}
		}
	}
	
	public void run() {

		try {
			String line = "";
			while (true) {

				//lock.lock();
				if (!isFirst) {
					if (!cr.once) {
						int count = cr.currentGameSize();
						if(count-1==0){
							sendMessage("Game is full.");
							//sendMessage("Would you like to answer a question across(a) or down(d)?");
						}
						if (count-1 == 1) {
							sendMessage("There is a game waiting for you. Player 1 has already joined.");
						}
						if(count-1 == 2){
							sendMessage("There is a game waiting for you. Player 1  & 2 have already joined.");
						}
						cr.once = true;

					}

					lock.lock();
					canMessage.await();
				} else {
					//if they are first//
					isFirst=false;
					line=br.readLine();
					if(line.contains("Num:")) {
						//the number of players
						line = line.substring(4, line.length());
						int num = Integer.parseInt(line);
						cr.setMaxCount(num);
						//sendMessage(cr.getWaitingList());
						cr.start();
						//canSendFirstRound=true;
						//
					}
					//System.out.println("FIRST PERSON HERE");
					lock.lock();
					canMessage.await();
				}


				///this is to read in messages from the client////
				Board legitBoard = cr.b;
				Board copiedBoard = cr.copy;

				boolean need_to_rotate=false;
				boolean canSendFirstRound = false;

				//while you do not need to rotate//
				//line=br.readLine();
				boolean down = false;
				int location = 0;

				System.out.println("WAITING FOR THE RIGHT INPUT FOR CLIENT");

				while (!need_to_rotate) {
					//cr.broadcast(line, this);

					if(cr.down.size()==0 && cr.across.size()==0){
						cr.printMessage("The game has concluded.");
						Pair<Integer, Integer> myPair = new Pair<Integer, Integer>(this.playerNumber, this.score);
						cr.pairScores.add(myPair);
						sendMessage("GAME_OVER");
						cr.gameOver();
						cr.closeConnections();
						//this.socket.close();
					}

					if(cr.gameLoaded){
						canSendFirstRound = false;
						sendMessage("Q1:Would you like to answer a question across(a) or down(d)?");
						cr.gameLoaded=false;
					}
					if(line.equals("ASKAGAIN")){
						sendMessage("Q1:Would you like to answer a question across(a) or down(d)?");
					}

					if(line.contains("FINAL_GUESS:")){
						//check to see if there submission makes sense
						//stored in down and in location
						String guessed_word = line.substring(12, line.length());
						System.out.println("THE GUESSED WORD IS "+guessed_word);
						String full_message = "guessed '"+guessed_word+"' for "+location+" ";
						if(down){
							full_message+="down";
						}else{
							full_message+="across";
						}
						guessed_word=guessed_word.toLowerCase();
						boolean validWordGuess = checkCorrect(location, guessed_word, down);
						cr.broadcast(this, full_message, true);

						if(validWordGuess){
							score++;
							sendMessage("COR:That was correct.");
							cr.broadcast(this, "That was correct.", false);
							cr.copy.revealWordAt(location, guessed_word, down);
							printBoardAndQuestions();
							sendMessage("ASKMEAGAIN");
							cr.gameLoaded=true;
						}else{
							sendMessage("X:That was incorrect.");
							cr.broadcast(this, "That was incorrect.", false);
							need_to_rotate=true;
							printBoardAndQuestions();
							cr.gameLoaded=true;
							break;
						}


						////need to check if that word is right
					}
					else if(line.contains("FINAL_A:")){
						///check this
						line = line.substring(8, line.length());
						System.out.println("Checking for across "+line);
						boolean l = cr.across.containsKey(Integer.valueOf(line));
						location = Integer.valueOf(line);
						down=false;
						if(l){
							sendMessage("WORD_GUESS:What is your guess for "+line+" across?");
						}else{
							sendMessage("QA:Not valid, enter a different number?");
						}
						///perform the check operations
					}else if(line.contains("FINAL_D:")){
						System.out.println("Checking for down "+line);

						line = line.substring(8, line.length());
						System.out.println("Checking for down "+line);
						boolean l = cr.down.containsKey(Integer.valueOf(line));
						location = Integer.valueOf(line);
						down=true;
						if(l){
							sendMessage("WORD_GUESS:What is your guess for "+line+" down?");
						}else{
							sendMessage("QA:Not valid, enter a different number?");
						}
						///perform the check operations
					}

					else if(line.contains("ANS:")){
						System.out.println("IN THE SERVER GOT THE ANS");
						line = line.substring(4, line.length());
						line = line.toLowerCase();

						if(line.equals("a")){
							sendMessage("QA:Which Number?");
						}else if(line.equals("d")){
							sendMessage("QD:Which Number?");
						}else{
							sendMessage("Q1:That is not a valid option, try again.");
						}
					}
					else if(line.contains("COMPLETE ANSWER:")){
						sendMessage("What is yout guess for "+line.substring(15, line.length()));
					}
					else if(line.contains("END")){
						break;
					}else{

					}
					line=br.readLine();
				}

				///this is to read in messages from the client////
				//System.out.println("I WANT TO GO TO NEXT PERSON");
				//lock.unlock();
				cr.goNextClient();
				//canMessage.await();
				line = "";
			}
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		} catch (InterruptedException ie) {
			System.out.println(ie.getMessage());
		}
	}
}
