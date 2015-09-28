package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class PC {
	private ServerSocket _server;
	private int _port = 9876;
	
	//Hostname: Harvey-PC; Port: 9876
	
	public void startListening() throws IOException, ClassNotFoundException {
		
		_server = new ServerSocket(_port);
		
		while (true) {
			Socket socket = _server.accept();
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			String message = (String) ois.readObject();
			//TODO pass the message to other classes and return cmd
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject("some cmd here");
			ois.close();
	        oos.close();
	        socket.close();
			if(message.equalsIgnoreCase("exit")) 
				break;
		}
		
		_server.close();
	}
}
