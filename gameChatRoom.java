import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import javafx.util.Pair;




public class gameChatRoom {
	public boolean gameLoaded = false;
	private Vector<ServerThread> serverThreads;
	private Vector<Lock> locks;
	private Vector<Condition> conditions;
	private int ClientCount;
	private gameClient firstClient;
	private int MaxCount = -1;
	private ArrayList<String> acrossWords;
	private ArrayList<String> downWords;
	public Map<Integer, ArrayList<String> > across = new HashMap<Integer, ArrayList<String> >();
	public Map<Integer, ArrayList<String> > down = new HashMap<Integer, ArrayList<String> >();
	public Board b;
	public Board copy;
	public boolean once = false;
	public ArrayList<Pair<Integer, Integer> > pairScores = new ArrayList<Pair<Integer, Integer> >();

	public int currentGameSize(){
		return serverThreads.size();
	}

	public String getWaitingList(){
		String myString = "Everyone is in the game";
		if(serverThreads.size()==2 && MaxCount==3){
			myString = "Waiting for players 3";
		}
		if(serverThreads.size()==1 && MaxCount==2){
			myString = "Waiting for players 2";
		}
		if(serverThreads.size()==1 && MaxCount==3){
			myString = "Waiting for players 2 & 3";
		}
		if(serverThreads.size()==1 && MaxCount==1){
			myString = "Everyone is in the game";
		}
		return myString;
	}

	public void setMaxCount(int max){
		MaxCount = max;
		System.out.println("Number of players "+max);
		System.out.println(getWaitingList());
	}
	
	public gameChatRoom(int port) {

		firstClient = null;
		ClientCount=0;
		ServerSocket ss = null;
		try {
			System.out.println("Listening on port... " + port);
			ss = new ServerSocket(port);
			//System.out.println("Bound to port " + port);
			serverThreads = new Vector<ServerThread>();
			this.locks = new Vector<Lock>();
			this.conditions = new Vector<Condition>();
			
			while(true) {
				if(serverThreads.size()==0){
					System.out.println("Waiting for players...");
				}
				Socket s = ss.accept(); // blocking
				String xms = "from: " + s.getInetAddress();
				System.out.println("Connection "+xms);

				///create a new lock and a new connection 
				Lock newLock = new ReentrantLock();
				Condition myCondition = newLock.newCondition();
				ServerThread st;
				if(serverThreads.size()==0){
					 st = new ServerThread(s, this, newLock, myCondition, true);
					 st.sendMessage("How many players will be playing?");
				}else{
					 st = new ServerThread(s, this, newLock, myCondition, false);
				}

				serverThreads.add(st);
				broadcast(st, "Player "+serverThreads.size()+" has joined "+xms, false);
				System.out.println("server array size: "+serverThreads.size());
				System.out.println("Maxcount: "+MaxCount);

				start();

				locks.add(newLock);
				conditions.add(myCondition);
				
			}
		} catch (IOException ioe) {
			System.out.println("ioe in ChatRoom constructor: " + ioe.getMessage());
		} finally{
			try {
				if ( ss != null ) { ss.close(); }
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}
	}

	public void start() {
		//if it is full///
		if(serverThreads.size()==MaxCount){
			System.out.println("Game Now Begin");
			System.out.println();
			System.out.println("Sending Game Board.");

			try{
				b = new Board();
				across = b.across;
				down = b.down;
				copy = b;
				copy.removeLetters();
				broadcast("The game is beginning.");
				for(int j=copy.actual_top; j<=copy.actual_bottom; j++){
					String x = " ";
					for(int i=copy.actual_left; i<=copy.actual_right; i++){
						x=x+copy.boardLayout.get(j).get(i)+" ";
					}
					//System.out.println(x);
					broadcast(x);
				}
				printQuestions();

				//serverThreads.get(0).sendMessage("Q1:Would you like to answer a question across(a) or down(d)?");
				gameLoaded=true;
				ClientCount=-1;
				goNextClient();

			}catch(Exception e){
				broadcast("There was an issue making the board");
			}

			//broadcast("INSERT THE BOARD HERE");
		}
	}
	public void gameOver(){
		System.out.println("Sending scores");
		broadcast("Final Score");

		int max =0;
		int num=0;
		for(int i=0; i<pairScores.size(); i++){
			if(pairScores.get(i).getValue()>num){
				num = i;
			}
			String x = "Player "+pairScores.get(i).getKey()+" - "+pairScores.get(i).getValue()+" correct answers.";
			broadcast(x);
		}
		broadcast("Player "+(num+1)+" is the winnner");
	}

	public void closeConnections(){
		for(int i=0; i<serverThreads.size(); i++){
			try{
				serverThreads.get(i).getSocket().close();
				serverThreads.remove(serverThreads.get(i));
			}catch(Exception e) {
				//do something
			}

		}

	}


	public void printMessage(String x){
		System.out.println(x);
	}

	public void printQuestions(){
		Iterator<Map.Entry<Integer, ArrayList<String> >> itr_A = this.across.entrySet().iterator();
		Iterator<Map.Entry<Integer, ArrayList<String> >> itr_D = this.down.entrySet().iterator();

		broadcast("ACROSS");
		while(itr_A.hasNext()) {
			Map.Entry<Integer, ArrayList<String> > entry = itr_A.next();
			String place_number =String.valueOf(entry.getKey());
			String q = entry.getValue().get(1);
			broadcast(place_number+" "+q);
			///if both of them have the key then they intersect
		}
		broadcast("DOWN");
		while(itr_D.hasNext()) {
			Map.Entry<Integer, ArrayList<String> > entry = itr_D.next();
			String place_number =String.valueOf(entry.getKey());
			String q = entry.getValue().get(1);
			broadcast(place_number+" "+q);
		}
	}



	//this will send a message to all the people that are playing


	public void broadcast(String message) {
		if (message != null) {
			//System.out.println(message + " size of serverthreads: " +  serverThreads.size());
			for(int i = 0; i < serverThreads.size(); i++) {
				//System.out.println("Serverthread: " + i);
					serverThreads.get(i).sendMessage(message);
			}
		}
	}

	//this will send a message to all the people that are playing, along with gameBoard
	public void broadcast(ServerThread st, String b, boolean option) {
		//make the message here by using the Board 'b'
		if (b != null) {
			for(int i = 0; i < serverThreads.size(); i++) {
				String message="";
				if(option){
					message = "Player "+(ClientCount+1);
				}
				ServerThread thread = serverThreads.get(i);
				//System.out.println("");
				if (st != thread) {
					if(option){
						thread.sendMessage(message+" "+b);
					}else{
						thread.sendMessage(b);
					}
				}
				System.out.println(message+" "+b);
			}
		}
	}

	//this will move onto the next client
	public void goNextClient(){

		ClientCount +=1;
		if(ClientCount >= locks.size()){
			//then the client is the only one left
			ClientCount=0;
		}
		//System.out.println("ROTATING TO CLIENT: "+ClientCount);
		locks.get(ClientCount).lock();
		conditions.get(ClientCount).signal();
		locks.get(ClientCount).unlock();
	}
	
	public static void main(String [] args) {
		gameChatRoom cr = new gameChatRoom(3456);
	}
}