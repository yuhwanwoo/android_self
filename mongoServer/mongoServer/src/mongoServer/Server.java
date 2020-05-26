package mongoServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	ServerSocket server;
	
	public void connect() {
		try {
			server=new ServerSocket(12345);
			
			System.out.println("사용자 접속 대기중");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		Thread th=new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
				try {
					Socket client=server.accept();
					String ip=client.getInetAddress().getHostAddress();
					System.out.println(ip+"접속!\n");
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				}
			}
		});
		th.start();
	}

	public static void main(String[] args) {
		new Server().connect();
	}

}
