import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class gameClient extends Thread {

	private BufferedReader br;
	private PrintWriter pw;
	public int score;


	public void sendMessage(String message) {
		//System.out.println("In Client: " + message);
		pw.println(message);
		pw.flush();
	}



	public gameClient(String hostname, int port) {
	    score=0;
		try {
			System.out.println("Trying to connect to " + hostname + ":" + port);
			Socket s = new Socket(hostname, port);
			System.out.println("Connected to " + hostname + ":" + port);
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
			this.start();
			Scanner scan = new Scanner(System.in);
//			while(true) {
//				String line = scan.nextLine();
//				pw.println("CS: "+ line);
//				pw.flush();
//			}
			
		} catch (IOException ioe) {
			throw new ArithmeticException("Improper Format");
		}
	}
	public void run() {
		try {
			String question = br.readLine();
			System.out.print(question);
            Scanner in3 = new Scanner(System.in);
            while(!question.contains("GAME_OVER")) {
				if(question.equals("How many players will be playing?")) {
					String response = in3.next();
					sendMessage("Num:" + response);
				}
				else if(question.equals("ASKMEAGAIN")){
				    sendMessage("ASKMEAGAIN");
                }
				else if(question.contains("WORD_GUESS:")){
				    System.out.println(question.substring(11,question.length()));
                    String response = in3.next();
                    sendMessage("FINAL_GUESS:" + response);
                }
				else if(question.contains("Q1:")){
				    System.out.println(question.substring(3, question.length()));
                    String response = in3.next();
                    sendMessage("ANS:" + response);
                }else if(question.contains("QA:")) {
                    System.out.println(question.substring(3, question.length()));
                    int response = in3.nextInt();
                    sendMessage("FINAL_A:" + response);
                }
                else if(question.contains("QD:")) {
                    System.out.println(question.substring(3, question.length()));
                    int response = in3.nextInt();
                    sendMessage("FINAL_D:" + response);
                }else if(question.contains("COR:")){
				    score++;
				    question = question.substring(4, question.length());
				    System.out.println(question);
                }else if(question.contains("X:")){
                    question = question.substring(2, question.length());
                    System.out.println(question);
                }
                else{
                    System.out.println(question);
                }
                question = br.readLine();
			}

//            question=question.substring(9,question.length());
//            String mine = String.valueOf(question);
//            System.out.println("Player "+mine+" - "+question+" correct answers.")
			///this means that GAME_OVER was sent if it goes here////


		} catch (IOException ioe) {
			System.out.println("ioe in ChatClient.run(): " + ioe.getMessage());
		}
	}
	public static void main(String[] args) {
		//TODO Auto-generated method stub

		System.out.println("Welcome to 201 Crossword!");
		System.out.print("Enter the server hostname:");
		Scanner in = new Scanner(System.in);
		String hostname = in.nextLine();
		System.out.print("Enter the server port:");
		int port = in.nextInt();
		System.out.println("Going to try and connect to "+hostname+"-> @port: "+port);

		boolean valid_connection = true;

		//try to connect to the gameChatRoom at the port/hostname provided
		try{
			gameClient cr = new gameClient(hostname, port);
		}catch(Exception e){
			valid_connection=false;
		}

		//keep asking until they get a valid connection

		while(!valid_connection){
			if(valid_connection){
				break;
			}
			//if it is not a valid connection prompt the user again

			System.out.println("Bad Hostname/Port...Try Again");

			System.out.print("Enter the server hostname:");
			Scanner in2 = new Scanner(System.in);
			hostname = in2.nextLine();
			System.out.print("Enter the server port:");
			port = in2.nextInt();
			System.out.println("Going to try and connect to "+hostname+"-> @port: "+port);
			try{
				gameClient cr = new gameClient(hostname, port);
				valid_connection=true;
				break;
			}catch(Exception e){
				valid_connection=false;
			}
		}


		/*
		try{
			reader r = new reader();
			r.readRandomFile();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
		Board gameBoard = new Board();
		gameBoard.printBoard();
		*/
	}
}