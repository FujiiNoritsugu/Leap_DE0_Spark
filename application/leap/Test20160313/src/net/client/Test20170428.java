package net.client;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.leapmotion.leap.Controller;

public class Test20170428 {

	public static void main(String [] arg){
		Queue <Float> a_queue = new ConcurrentLinkedQueue<Float>();
		SampleListener listener = new SampleListener(a_queue);
		Controller controller = new Controller();
		controller.addListener(listener);

		System.out.println("Press Enter");
		try{
			System.in.read();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		controller.removeListener(listener);
	}
}
