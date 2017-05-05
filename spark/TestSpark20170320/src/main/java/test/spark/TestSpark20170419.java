package test.spark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class TestSpark20170419 {

	public static void main(String ... args){
		try{
			Path path1 = Paths.get("./data/20170505/0_libsvm");
			Path path2 = Paths.get("./data/20170505/1_libsvm");
			Path path3 = Paths.get("./data/20170505/2_libsvm");
			Path allPath = Paths.get("./data/20170505/20170505_libsvm");
			if(Files.exists(allPath)){
				Files.delete(allPath);
			}
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
			return new Container(label, 0, sensor1, sensor2);
		}).filter(data->{
			return data.sensor1 > 0;
		}).map(data->{
			return data.label + " 1:" +data.sensor1 + " 2:" + data.sensor2;
		}).collect(Collectors.toList());
	}
	
	private static class Container {
		String label;
		int order;
		int sensor1;
		int sensor2;
		
		Container(String a, int b, int c, int d){
			label = a;
			order = b;
			sensor1 = c;
			sensor2 = d;
		}
	}
}
