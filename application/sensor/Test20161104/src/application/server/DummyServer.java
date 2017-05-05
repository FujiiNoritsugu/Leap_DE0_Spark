package application.server;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.arnx.jsonic.JSON;

public class DummyServer {
	
	public static void main(String ... args){
//			
//			ServerSocket serverSocket = null;
//			Socket socket = null;
//			BufferedWriter writer = null;
			List<Integer> dummyList = new ArrayList<Integer>(){
				private static final long serialVersionUID = 1L;
				{
					add(1);
					add(2);
					add(3);
				}
			};
			
		try (
				ServerSocket serverSocket = new ServerSocket(25);
				Socket socket = serverSocket.accept();
				BufferedWriter writer = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
			){
				System.out.println("connect Monitor");
				// インスタンスを残しておく
				while(!Thread.interrupted()){
					writer.write("dummy" + System.currentTimeMillis());
					Thread.sleep(1000);
				}

		}catch(Throwable th){
			th.printStackTrace();
		}
	}
}
