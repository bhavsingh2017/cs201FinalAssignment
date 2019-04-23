import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.Scanner;

public class ServerThread extends Thread {

	private PrintWriter pw;
	private BufferedReader br;
	private gameChatRoom cr;
	
	private Lock lock;
	private Condition canMessage;
	private boolean isFirst=false;
	private int playerCount=0;
	
	public ServerThread(Socket s, gameChatRoom cr, Lock lock, Condition canMessage, boolean isFirst) {
		try {
			this.cr = cr;
			this.lock = lock;
			this.canMessage = canMessage;
			this.isFirst = isFirst;
			pw = new PrintWriter(s.getOutputStream());
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.start();
		} catch (IOException ioe) {
			System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
		}
	}

	public void sendMessage(String message) {
		System.out.println("In SERVER, going to send: "+message);
		pw.println(message);
		pw.flush();
	}
	
	public void run() {
		boolean once = false;
		try {
			String line = "";
			while (true) {
				lock.lock();
				if (!isFirst) {
					if (!once) {
						int count = cr.currentGameSize();
						if (count-1 == 1) {
							sendMessage("There is a game waiting for you. Player 1 has already joined.");
						}
						if(count-1 == 2){
							sendMessage("There is a game waiting for you. Player 1  & 2 have already joined.");
						}
						once = true;
					}
					canMessage.await();
				} else {
					//if they are first
					sendMessage("How many players will be playing?");
					isFirst = false;
				}
				
				while (!line.contains("END_OF_MESSAGE")) {
					//cr.broadcast(line, this);
					line = br.readLine();
					if(line.contains("Num:")) {
						//the number of players
						line = line.substring(4, line.length());
						int num = Integer.parseInt(line);
						cr.setMaxCount(num);
						sendMessage(cr.getWaitingList());
					}
				}
				lock.unlock();
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
