package application.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ApplicationServer implements Runnable{

	PrintWriter writer = null;
	
	/**
	 * コンストラクタ
	 * @throws IOException 
	 */
	public ApplicationServer() throws IOException{
	}

	/**
	 * スレッド実行
	 */
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		
		try {
			serverSocket = new ServerSocket(25);
			socket = serverSocket.accept();
			writer = new PrintWriter(socket.getOutputStream());
			System.out.println("accept");
			// 待機しておく
			while(!Thread.interrupted()){
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				writer.close();
				socket.close();
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * センサデータを受信する
	 */
	public void receiveSensorData(List<Integer> dataList) {
		try {
			
			// ソケットが接続されていない状態
			if(writer == null){
				System.out.println("not connected !!");
				return;
			}
			
			for(Integer data : dataList){

				int temp = data.intValue();
				int sensor1 = 0x00000fff & temp;
				int sensor2 = ((0x00fff000 & temp) >> 12);

				//センサ１の値が0であればファイルに出力しない
				if(sensor1 <= 0){
					System.out.println("sensor1 is negative = " + sensor1);
					continue;
				}

				writer.println(sensor1 + "," + sensor2);
				writer.flush();
				
			}

		} catch (Throwable e1) {
			e1.printStackTrace();
		}
	}
}
