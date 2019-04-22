import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ServerThread extends Thread {

	private PrintWriter pw;
	private BufferedReader br;
	private gameChatRoom cr;
	
	private Lock lock;
	private Condition canMessage;
	private boolean isFirst=false;
	
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
		pw.println(message);
		pw.flush();
	}
	
	public void run() {
		try {
			String line = "";
			while (true) {
				lock.lock();
				if (!isFirst) {
					canMessage.await();
				} else {
					isFirst = false;
				}
				
				while (!line.contains("END_OF_MESSAGE")) {
					cr.broadcast(line, this);
					line = br.readLine();
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
