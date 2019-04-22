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
	
	public gameChatRoom(int port) {
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
	
	public void broadcast(String message, ServerThread st) {
		if (message != null) {
			System.out.println(message);
			for(ServerThread threads : serverThreads) {
				if (st != threads) {
					threads.sendMessage(message);
				}
			}
		}
	}
	
	public void goNextClient(){
		ClientCount +=1;
		if(ClientCount == locks.size()){
			ClientCount=0;
		}
		locks.get(ClientCount).lock();
		conditions.get(ClientCount).signal();
		locks.get(ClientCount).unlock();
	}
	
	public static void main(String [] args) {
		gameChatRoom cr = new gameChatRoom(6789);
	}
}