package sensor.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import application.server.ApplicationServer;

public class SensorClient {

	Socket socket;
	BufferedInputStream is;

	Executor executor;
	ApplicationServer applicationServer;

	public SensorClient() throws IOException{
		executor = Executors.newSingleThreadExecutor();
		applicationServer = new ApplicationServer();
		executor.execute(applicationServer);
	}
	
	public static void main(String [] args){
		try{
				SensorClient sensorClient = new SensorClient();
				String de0ip = null;
				if(args == null || args.length == 0){
					de0ip = "192.168.1.8";
				}else{
					de0ip = args[0];
				}
				sensorClient.readData(de0ip);
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
	
	/**
	 * センサデータを読み込んでアプリクライアントに送信する
	 */
	private void readData(String de0ip){
		byte [] buff = new byte[4*1000];		
		try{
			socket = new Socket(de0ip, 24);
			is = new BufferedInputStream((socket.getInputStream()));

			while(true){
				int result = is.read(buff);
				if(result == -1){
					System.out.println("stream reach end");
				}else{
					ByteBuffer bb = ByteBuffer.wrap(buff);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					List<Integer> dataList = new ArrayList<Integer>();					
					for(int i = 0; i < 1000; i++){
						dataList.add(bb.getInt(i * 4));
					}
					applicationServer.receiveSensorData(dataList);
				}
			}
		}catch(Throwable th){
			th.printStackTrace();
		}finally{
			try {
				if(is != null){is.close();}
				if(socket != null){socket.close();}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
//	/**
//	 * センサデータの表示
//	 * @param sensorData
//	 */
//	private void showSensorData(int [] sensorData){
//		int temp, sensor1, sensor2;
//		for(int data : sensorData){
//			temp = 0x00ffffff & data;
//			sensor1 = 0x00000fff & temp;
//			sensor2 = ((0x00fff000 & temp) >> 12);
//			//System.out.println("sensor1 = " + sensor1 + " sensor2 = " + sensor2);
//			System.out.println("sensor_data = " + data);
//		}
//	}
}
