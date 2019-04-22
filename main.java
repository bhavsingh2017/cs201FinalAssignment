import java.io.IOException;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			reader r = new reader();
			r.readRandomFile();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
		Board gameBoard = new Board();
		gameBoard.printBoard();
	}

}
