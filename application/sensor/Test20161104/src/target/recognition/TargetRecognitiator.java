package target.recognition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ���F���N���X
 * @author fujii
 *
 */
public class TargetRecognitiator {

	/**
	 * ���F���p�e���v���[�g�}�b�v
	 */
	Map<String, Map<String, Double>> templateMap = null;

	/**
	 * �R���X�g���N�^
	 * @throws IOException 
	 */
	public TargetRecognitiator() throws IOException{
	}
	
	/**
	 * 物認識を行う
	 * @param dataList
	 * @return
	 * @throws IOException 
	 */
	public List<String> judgeTarget(List<Integer> dataList) throws IOException {
		List<String> resultList = null;
		// ファイルにlibsvm形式で出力する
		String fileName = "/home/fujii/spark-2.1.0-bin-hadoop2.7/app/input_data/check_data" + System.currentTimeMillis();
		Path path = Paths.get(fileName);
		if(!Files.exists(path)){
			Files.createFile(path);
		}
		// ファイルの出力が1000件を超えると学習を実施する
		if (writeLibsvm(dataList, path) > 0) {

			// Sparkに処理を渡す
			ProcessBuilder pb = new ProcessBuilder()
					.command("/home/fujii/spark-2.1.0-bin-hadoop2.7/bin/spark-submit", "--class",
							"test.spark.ObjectRecogniator",
							"/home/fujii/spark-2.1.0-bin-hadoop2.7/app/jar/TestSpark20170320.jar", fileName)
					.inheritIO();

			try (InputStream is = pb.start().getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				resultList = br.lines().collect(Collectors.toList());
			}
			// 学習が完了すればファイルを削除する
			//Files.delete(path);
		}
		return resultList;
	}
	
	/**
	 * ファイルにlibsvm形式で出力する
	 * @param dataList
	 * @param fileName
	 * @throws IOException
	 */
	private int writeLibsvm(List<Integer> dataList, Path path) throws IOException{
		
		List<String> recordList = new ArrayList<String>();
		for(Integer data : dataList){
			int temp = data.intValue();
			int sensor1 = 0x00000fff & temp;
			int sensor2 = ((0x00fff000 & temp) >> 12);
			sensor1 = sensor1 - 2000;
			sensor2 = sensor2 - 1600;
			//センサ１の値が負値であればファイルに出力しない
			if(sensor1 < 0){
				continue;
			}
			recordList.add("0 1:" + sensor1 + " 2:" + sensor2);
		}

		Files.write(path, recordList, StandardOpenOption.APPEND);

		return Files.readAllLines(path).size();
	}
}
