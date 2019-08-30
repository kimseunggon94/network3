package kr.co.itcen.network.chat;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChatServerThread extends Thread {
	private String nickname;
	private Socket socket;
	private List<Writer> listWriters;

	public ChatServerThread( Socket socket, List<Writer> listWriters ) {
		this.socket = socket;
		this.listWriters = listWriters;
	}


	public void run() {

		try {
			//1. Remote Host Information
			InetSocketAddress inetSocketAddress = ( InetSocketAddress )socket.getRemoteSocketAddress();
			ChatServer.log( "connected from " + inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort() );


			//2. 스트림 얻기
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );		//전역변수로 잡으면 다 묶여버림
			PrintWriter printWriter = new PrintWriter( new OutputStreamWriter( socket.getOutputStream(), StandardCharsets.UTF_8 ), true );

			//3. 요청 처리 			
			while( true ) {
				String request = bufferedReader.readLine();

				if( request == null ) {
					ChatServer.log( "클라이언트로 부터 연결 끊김" );
					doQuit(printWriter);
					break;
				}

				// 4. 프로토콜 분석
				String[] tokens = request.split( ":" );

				if( "join".equals( tokens[0] ) ) {
					doJoin( tokens[1], printWriter );
				} else if( "message".equals( tokens[0] ) ){
					doMessage(tokens[1]);
				} else if( "quit".equals( tokens[0] ) ) {
					doQuit(printWriter);
					break;
				}  else {
					ChatServer.log( "에러:알수 없는 요청(" + tokens[0] + ")" );
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(socket!=null&&socket.isClosed()==false) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	private void doJoin( String nickName, PrintWriter printwriter ) {
		this.nickname = nickName;

		addWriter( printwriter );

		String data = nickName + "님이 참여하였습니다.";
		broadcast( data );

		printwriter.println( "환영합니다." );
		printwriter.flush();
	}

	private void addWriter( Writer writer ) {
		synchronized( listWriters ) {
			listWriters.add( writer );
		}
	}

	private void broadcast( String data ) {
		synchronized( listWriters ) {
			for( Writer writer : listWriters ) {
				PrintWriter printWriter = (PrintWriter)writer;
				printWriter.println( data );
				printWriter.flush();
				System.out.println(data+ "   "+ listWriters.size());
			}
		}
	}

	private void doMessage( String message ) {
		broadcast(nickname +" : "+message);
	}

	private void doQuit(  PrintWriter writer ) {
		removeWriter( writer );
		String data = nickname + "님이 퇴장 하였습니다."; 
		broadcast( data );
	}

	private void removeWriter( PrintWriter printwriter ) {
		synchronized (this.listWriters) {
			listWriters.remove(printwriter);
		}
	}
}
