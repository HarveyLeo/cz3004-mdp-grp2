package udpcomm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RPiServer {

	
	private static final String RPI_IP_ADDRESS = "192.168.2.1";
	private static final int RPI_PORT = 3000;
	private static final int PC_PORT = 2000;
	
	private DatagramSocket _socket;
	private DatagramPacket _packet;
	
	
	public static void main(String[] args) throws InterruptedException, IOException  {
		RPiServer rpi = new RPiServer();
		rpi.startComm();
	}
	
	public void startComm() throws InterruptedException, IOException {
		String command;
		
		_socket = new DatagramSocket(RPI_PORT);
		
		byte[] rcvBuffer = new byte[100];
	    _packet= new DatagramPacket(rcvBuffer, rcvBuffer.length);
	    _socket.receive(_packet);
	    byte[] receivedPacketData = _packet.getData();
	    String receivedMessage = new String(receivedPacketData);
	    System.out.println(receivedMessage);
	        
		command = "packet received";
		byte[] sentPacketData = command.getBytes();
		_packet = new DatagramPacket(sentPacketData, sentPacketData.length, InetAddress.getByName("Harvey-PC"), PC_PORT);
		_socket.send(_packet);
		Thread.sleep(100);
		
	  
		
        _socket.close();
	}


}
