package net.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.TimerTask;

public class ClientSocket extends TimerTask{

	Socket socket;
	BufferedReader stream;
	PrintWriter out;
	
	Queue <Float> index_queue;

	public ClientSocket(Queue<Float> a_queue) {
		index_queue = a_queue;
		
		try {
			socket = new Socket("192.168.1.8", 23);
			stream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void run(){
		Float index = index_queue.poll();
		if(index != null){
System.out.println("index = " + index);
			int indexValue = getRangeValueForMiddle(index.doubleValue(), 0.0, 1.0);
			System.out.println("value = " + indexValue);
			int value = 0xffffffff & (indexValue | (indexValue << 8));
			String data = String.format("d%05d", value);
			out.print(data);
			out.flush();
		}
	}
	
	private static double RANGE_MAX = 150f;
	private static double RANGE_MIN = 100f;

	private int getRangeValueForMiddle(double value, double min, double max){
		if(value < min){value = (int)min;}
		if(value > max){value = (int)max;}
		double range = (double)((RANGE_MAX - RANGE_MIN)/(max - min));
		int result = (int) (range * value + RANGE_MIN);
		return result;
	}
	
}
