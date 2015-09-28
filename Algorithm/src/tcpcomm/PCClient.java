package tcpcomm;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class PCClient {
	
	private static final String RPI_IP_ADDRESS = "192.168.2.1";
	private static final int RPI_PORT = 3000;
	
	private static PCClient _instance;
	private Socket _clientSocket;
	
	private PCClient() {
		
	}
	
	public static PCClient getInstance() {
        if (_instance == null) {
            _instance = new PCClient();
        }
        return _instance;
    }
	
	public static void main (String[] args) throws UnknownHostException, IOException {
		
		PCClient pcClient = PCClient.getInstance();
		pcClient.setUpConnection(RPI_IP_ADDRESS, RPI_PORT);
		
		Scanner sc = new Scanner(System.in);
		while (true) {
			String msgSent = sc.nextLine();
			pcClient.sendMessage(msgSent);
			String msgReceived = pcClient.readMessage();
			System.out.println("Message received:"+ msgReceived);
		}
		
	}
	
	public void setUpConnection (String IPAddress, int portNumber) throws UnknownHostException, IOException{
		_clientSocket = new Socket(RPI_IP_ADDRESS, RPI_PORT);
	}
	
	public void closeConnection() throws IOException {
		if (!_clientSocket.isClosed()) {
			_clientSocket.close();
		}
	}

	public void sendMessage(String msg) throws IOException{
		PrintWriter toRPi = new PrintWriter(_clientSocket.getOutputStream(), true);
		toRPi.print(msg);
	}

	public String readMessage() throws IOException{
		Scanner fromRPi = new Scanner(_clientSocket.getInputStream());
		String messageReceived = fromRPi.nextLine();	
		return messageReceived;
	}
         
}
