package net.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Listener;

public class SampleListener extends Listener{
	Queue <Float> grab_queue;
	List<Float> grabList  = new ArrayList<Float>();

	public SampleListener(Queue <Float> a_queue){
		super();
		grab_queue = a_queue;
	}
	
	public void onConnect(Controller controller){
		System.out.println("Connected");
	}
	
	private static final int LIMIT = 200;
	
	public void onFrame(Controller controller){
		try{
				Frame frame = controller.frame();
				float grabStrength = frame.hands().get(0).grabStrength();
				grabList.add(grabStrength);
				if(grabList.size() == LIMIT){
					grab_queue.add(getTargetBend(grabList));
					grabList.clear();
				}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	private float getTargetBend(List<Float> dataList){
		return (float) dataList.stream().mapToDouble(a->a).average().getAsDouble();
	}
	
}
