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

public class ServerThread extends Thread {

	private PrintWriter pw;
	private BufferedReader br;
	private gameChatRoom cr;
	private Socket socket;

	
	private Lock lock;
	private Condition canMessage;
	private boolean isFirst=false;
	private int playerCount=0;

	public Socket getSocket(){
		return socket;
	}
	
	public ServerThread(Socket s, gameChatRoom cr, Lock lock, Condition canMessage, boolean isFirst) {
		try {
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

	public void sendMessage(String message) {
		//System.out.println("In SERVER, going to send: "+message);
		pw.println(message);
		pw.flush();
	}
	
	public void run() {
		boolean once = false;
		try {
			String line = "";
			while (true) {
				//lock.lock();
				if (!isFirst) {
					if (!once) {
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
						once = true;
					}
					lock.lock();
					canMessage.await();
				} else {
					//if they are first//
					sendMessage("How many players will be playing?");
					isFirst=false;
					line=br.readLine();
					if(line.contains("Num:")) {
						//the number of players
						line = line.substring(4, line.length());
						int num = Integer.parseInt(line);
						cr.setMaxCount(num);
						//canSendFirstRound=true;
						sendMessage(cr.getWaitingList());
					}
					lock.lock();
					canMessage.await();
				}

				Board legitBoard = cr.b;
				Board copiedBoard = cr.copy;
				///this is to read in messages from the client////

				boolean need_to_rotate=false;
				boolean canSendFirstRound = false;

				//while you do not need to rotate//
				while (!need_to_rotate) {
					//cr.broadcast(line, this);
					if(cr.gameLoaded){
						System.out.println("HELLO1");
						canSendFirstRound = false;
						sendMessage("Would you like to answer a question across(a) or down(d)?");
					}

					if(line.contains("ANS:")){
						line = line.substring(4, line.length());
						line = line.toLowerCase();
						if(line.equals("a")){
							sendMessage("Which Number?");
						}else if(line.equals("d")){
							sendMessage("Which Number?");
						}else{
							sendMessage("That is not a valid option");
						}
					}
					if(line.contains("COMPLETE ANSWER:")){
						sendMessage("What is yout guess for "+line.substring(15, line.length()));
					}
					if(line.contains("END")){
						break;
					}
					line = br.readLine();
				}

				///this is to read in messages from the client////
				System.out.println("HELLOOOOOOO");
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
