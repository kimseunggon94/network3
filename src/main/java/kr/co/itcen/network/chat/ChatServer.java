package kr.co.itcen.network.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
	public static final String SERVER_IP = "192.168.1.17";
	private static ServerSocket serverSocket;
	private static int PORT = 7000;
	private static PrintWriter printWriter;
	private static List<Writer> listWriters;
	
	public static void main(String[] args) {
		try {
			serverSocket = new ServerSocket();
			listWriters = new ArrayList<Writer>();
			
			// 2. 바인딩
			serverSocket.bind( new InetSocketAddress( SERVER_IP, PORT ) );			
			log( "연결 기다림 " + SERVER_IP + ":" + PORT );
			
	
			// 3. 요청 대기 
			while( true ) {
			   Socket socket = serverSocket.accept();
			   new ChatServerThread(socket, listWriters).start();
			}		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(serverSocket!=null&&serverSocket.isClosed()==false) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	static void log(String log) {
		System.out.println("[Server#"+Thread.currentThread().getId()+"]"+log);	
	}
}
