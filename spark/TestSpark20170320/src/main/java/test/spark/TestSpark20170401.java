package test.spark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class TestSpark20170401 {

	public static void main(String ... args){
		try{
			Path path1 = Paths.get("./data/ball1_libsvm");
			Path path2 = Paths.get("./data/ball2_libsvm");
			Path path3 = Paths.get("./data/sponji_libsvm");
			Path allPath = Paths.get("./data/all_libsvm");
			Files.delete(allPath);
			Files.createFile(allPath);
			Files.write(allPath, changeData(path1), StandardOpenOption.APPEND);
			Files.write(allPath, changeData(path2), StandardOpenOption.APPEND);
			Files.write(allPath, changeData(path3), StandardOpenOption.APPEND);
		}catch(Throwable th){
			th.printStackTrace();
		}
	}
	
	private static List<String> changeData(Path path) throws IOException{
		return Files.readAllLines(path).stream().map(data->{
			String [] tempArray = data.split(" ");
			String label = tempArray[0];
			int sensor1 = Integer.parseInt(tempArray[1].split(":")[1]);
			int sensor2 = Integer.parseInt(tempArray[2].split(":")[1]);
			return new Container(label, sensor1, sensor2);
		}).filter(data->{
			return data.sensor1 > 0;
		}).map(data->{
			return getLabelNum(data.label) + " 1:" + data.sensor1 + " 2:" +data.sensor2;
		}).collect(Collectors.toList());
	}
	
	private static int getLabelNum(String label){
		if(label.equals("ball1_libsvm")){
			return 0;
		}else if(label.equals("ball2_libsvm")){
			return 1;
		}else{
			return 2;
		}
	}
	private static class Container {
		String label;
		int sensor1;
		int sensor2;
		
		Container(String a, int b, int c){
			label = a;
			sensor1 = b;
			sensor2 = c;
		}
	}
}
