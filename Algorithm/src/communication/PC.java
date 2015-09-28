package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PC {
	
	private static final String RPI_IP_ADDRESS = "192.168.2.1";
	private static final int RPI_PORT = 3000;
	private static final int PC_PORT = 2000;
	
	private DatagramSocket _socket;
	private DatagramPacket _packet;
	
	
	public static void main(String[] args) throws InterruptedException, IOException  {
		PC pc = new PC();
		pc.startComm();
	}
	
	public void startComm() throws InterruptedException, IOException {
		String command;
		
		_socket = new DatagramSocket(PC_PORT);
		command = "connect with RPi";
		
		byte[] sentPacketData = command.getBytes();
		_packet = new DatagramPacket(sentPacketData, sentPacketData.length, InetAddress.getByName(RPI_IP_ADDRESS), RPI_PORT);
		_socket.send(_packet);
		Thread.sleep(100);
		
	    byte[] rcvBuffer = new byte[100];
        _packet= new DatagramPacket(rcvBuffer, rcvBuffer.length);
        _socket.receive(_packet);
        byte[] receivedPacketData = _packet.getData();
        String receivedMessage = new String(receivedPacketData);
        System.out.println(receivedMessage);
		
        _socket.close();
	}
}
