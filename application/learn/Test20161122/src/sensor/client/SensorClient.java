package sensor.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学習用プログラム
 * @author fujii
 *
 */
public class SensorClient {

	Socket socket;
	BufferedInputStream is;

	Path targetPath;
	String dataName;
	
	public SensorClient(String dataName){
		this.dataName = dataName;
		targetPath = Paths.get(dataName+"_" + System.currentTimeMillis());
		try {
			Files.createFile(targetPath);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static void main(String [] args){
		SensorClient sensorClient = new SensorClient(args[0]);
		String de0ip = null;
		de0ip = "192.168.1.8";
		sensorClient.readData(de0ip);
	}

	private int count = 0;
	
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

System.out.println(String.format(" count =  %d", ++ count));

					ByteBuffer bb = ByteBuffer.wrap(buff);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					List<Integer> dataList = new ArrayList<Integer>();					
					for(int i = 0; i < 1000; i++){
						dataList.add(bb.getInt(i * 4));
					}
					
//					List <String> outputList = dataList.stream().map(data->{
//						int value = data.intValue();
//						int sensor1 = 0x00000fff & value;
//						int sensor2 = ((0x00fff000 & value) >> 12);
//						return dataName + " 1:" + sensor1 + " 2:" + sensor2;
//					}).collect(Collectors.toList());

					List <String> outputList = new ArrayList<String>();
					for(int i = 0; i < dataList.size(); i++){
						int value = dataList.get(i);
						int sensor1 = 0x00000fff & value;
						int sensor2 = ((0x00fff000 & value) >> 12);
						outputList.add(dataName + " 1:" + sensor1 + " 2:" + sensor2);
					}

					Files.write(targetPath, outputList, StandardOpenOption.APPEND);
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
}
