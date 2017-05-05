import java.io.IOException;
import java.util.Queue;

public class ResultSpeaker implements Runnable{
	Queue <Integer> queue;

	public ResultSpeaker(Queue <Integer> queue){
		this.queue = queue;
	}

	@Override
	public void run() {
		while(true){
			Integer result = queue.poll();
			if(result != null){
				String arg = null;
				if(result == 0){
					arg = "big ball";
				}else if(result == 1){
					arg = "small ball";
				}else if(result == 2){
					arg = "sponji";
				}
				if(arg != null){
					try {
						new ProcessBuilder("espeak", arg).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
