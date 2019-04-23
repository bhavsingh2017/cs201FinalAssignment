import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class gameChatRoom {
	private Vector<ServerThread> serverThreads;
	private Vector<Lock> locks;
	private Vector<Condition> conditions;
	private int ClientCount;
	private gameClient firstClient;
	private int MaxCount;
	public name;

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
		return myString;
	}

	public void setMaxCount(int max){
		MaxCount = max;
		System.out.println("Number of players "+max);
		System.out.println(getWaitingList());
	}
	
	public gameChatRoom(int port) {
		System.out.println("Server...");
		firstClient = null;
		ClientCount=0;
		ServerSocket ss = null;
		try {
			System.out.println("Binding to port " + port);
			ss = new ServerSocket(port);
			System.out.println("Bound to port " + port);
			serverThreads = new Vector<ServerThread>();
			this.locks = new Vector<Lock>();
			this.conditions = new Vector<Condition>();
			
			while(true) {
				Socket s = ss.accept(); // blocking
				System.out.println("Connection from: " + s.getInetAddress());
				///create a new lock and a new connection 
				Lock newLock = new ReentrantLock();
				Condition myCondition = newLock.newCondition();
				ServerThread st;
				if(serverThreads.size()==0){
					 st = new ServerThread(s, this, newLock, myCondition, true);
				}else{
					 st = new ServerThread(s, this, newLock, myCondition, false);

				}
				serverThreads.add(st);

				if(serverThreads.size()==MaxCount){
					System.out.println("Game Now Begin");
					System.out.println();
					System.out.println("Sending Game Board.");

					broadcast("INSERT THE BOARD HERE");
				}

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


	//this will send a message to all the people that are playing
	public void broadcast(String message) {
		if (message != null) {
			System.out.println(message + " size of serverthreads: " +  serverThreads.size());
			for(int i = 0; i < serverThreads.size(); i++) {
				System.out.println("Serverthread: " + i);
					serverThreads.get(i).sendMessage(message);
			}
		}
	}

	//this will send a message to all the people that are playing, along with gameBoard
	public void broadcast(ServerThread st, Board b) {
		//make the message here by using the Board 'b'
		String message = "";
		if (message != null) {
			System.out.println(message);
			for(ServerThread threads : serverThreads) {
				if (st != threads) {
					threads.sendMessage(message);
				}
			}
		}
	}

	//this will move onto the next client
	public void goNextClient(){
		ClientCount +=1;
		if(ClientCount == locks.size()){
			//then the client is the only one left
			ClientCount=0;
		}
		locks.get(ClientCount).lock();
		conditions.get(ClientCount).signal();
		locks.get(ClientCount).unlock();
	}
	
	public static void main(String [] args) {
		gameChatRoom cr = new gameChatRoom(3456);
	}
}