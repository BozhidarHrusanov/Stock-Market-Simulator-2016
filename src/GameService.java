import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameService implements Runnable {

	private Socket socket;
	private Scanner networkingIN;
	private PrintWriter networkingOUT;
	private boolean readyForNextRound = false;
	private boolean connected = true;
	private String clientInput = "empty";
	private String buffer = "";

	public GameService(Socket socket) {
		this.socket = socket;
		try {
			networkingIN = new Scanner(socket.getInputStream());
			networkingOUT = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (connected) {
			if (readyForNextRound) {
				// shte ima switch lanes vutre za otdelnite rundove
				clientInput = networkingIN.nextLine().trim();
				buffer = "";
				readyForNextRound = false;
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// control input at client total
	public int askGameMode() {
		networkingOUT.println("Select the number of total players to play"
				+ " the game and press ENTER.<br>2, 3 or 4");
		String result = networkingIN.nextLine().trim();
		for (int i = 2; i <= 4; i++){
			if (result.contains(String.valueOf(i))){
				return i;
			}
		}
		return 2;
	}
	
	public boolean askBots() {
		networkingOUT.println("Do you want bots - choose \"yes\" or \"no\" and press ENTER.");
		return networkingIN.nextLine().trim().contains("yes") ? true : false;
	}
	
	public void sendMessageToClient(String msg) {
		networkingOUT.println(msg);
	}
	
	/* The frontMsg is appended to the front of the next message to
	 * be sent to a client. The buffer is emptied after the
	 * message has been sent.
	 */
	public void addToMessageBufferToClient(String frontMsg) {
		buffer = frontMsg;
	}
	
	public String getClientInput() {
		return clientInput;
	}

	public synchronized void setClientInput(String clientInput) {
		this.clientInput = clientInput;
	}

	public synchronized void setReadyForNextRound(boolean readyForNextRound) {
		this.readyForNextRound = readyForNextRound;
	}
	
	public synchronized void setConnected(boolean connected) {
		this.connected = connected;
	}
}
