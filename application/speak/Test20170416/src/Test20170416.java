import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Test20170416 {

	Queue <Integer> queue;

	public Test20170416(Queue <Integer> queue){
		this.queue = queue;
	}
	
	public static void main(String ... args){
		try{

			Queue <Integer> queue = new ConcurrentLinkedQueue<Integer>();
	
			new Thread(new ResultSpeaker(queue)).start();
			
			new Test20170416(queue).receiveResult();
			
		}catch(Throwable th){
			th.printStackTrace();
			
		}
	}
	
	private void receiveResult() throws IOException{

		ServerSocket server = new ServerSocket(26);
		Socket socket = server.accept();
System.out.println("accept");
		Map<Integer, Integer>countMap = new HashMap<Integer, Integer>();
		int dataCount = 0;
		try(InputStream is = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));){
			
			while(true){
				String prediction = reader.readLine();
				if(prediction != null){
					dataCount ++;
					int label = (int) Double.parseDouble(prediction);
					Integer count = countMap.get(label);
					if(count == null){
						count = 0;
					}
					count ++;
					countMap.put(label, count);
					if(dataCount >= 1000){
						System.out.println("ball1 = " + countMap.get(0) + " ball2 = " + countMap.get(1) + " sponji = " + countMap.get(2));
						
						countMap.entrySet().stream().max((entry1, entry2) ->{
							return entry1.getValue() - entry2.getValue();
						}).ifPresent(target ->{
							queue.add(target.getKey());
						});
						
						dataCount = 0;
						countMap.clear();
					}
				}
			}

		}

	}
}
