package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物認識用ユーティリティクラス
 * @author fujii
 *
 */
public class JudgeUtil {
	/**
	 * テンプレートデータをグリッドデータに変換
	 * @param paramList
	 * @return
	 */
	public static Map<String, Double> convertGrid(List<String> paramList){
		Map<String, Double> resultMap = new HashMap<String, Double>();
		for(int i = 0; i <= 10; i++){
			for(int j = 0; j <= 10; j++){
				String key = i + "_" + j;
				int index = 11 * i  + j;
				resultMap.put(key, Double.parseDouble(paramList.get(index)) * 1000);
			}
		}
		return resultMap;
	}
	
	/**
	 * グリッドマップの作成
	 * @param paramList
	 * @return
	 */
	public static Map<String, Integer> makeGridMap(List<Integer> paramList){
		// グリッドマップの初期化
		Map<String, Integer> gridMap = initializeGridMap();
		
		for(Integer record : paramList){
				int data = record.intValue();
				int sensor1 = 0x00000fff & data;
				int sensor2 = ((0x00fff000 & data) >> 12);
				int sensor1Grid = (sensor1 / 200) - 5;
				int sensor2Grid = (sensor2 / 50) - 10;
				// 範囲外データを省く
				if(sensor1Grid < 0 || sensor1Grid > 10) continue;
				if(sensor2Grid < 0 || sensor2Grid > 10) continue;
				String key = sensor1Grid + "_" + sensor2Grid;
				Integer count = gridMap.get(key);
				if(count == null){
					count = 1;
				}else{
					count ++;
				}
				gridMap.put(key, count);
		}

		return gridMap;
	}

	//グリッドマップの初期化
	private static Map<String, Integer> initializeGridMap(){
		Map<String, Integer> resultMap = new HashMap<String, Integer>();
		for(int i = 0; i <= 10; i++){
			for(int j = 0; j <= 10; j++){
				String key = i + "_" + j;
				resultMap.put(key, 0);
			}
		}
		
		return resultMap;
	}

	/**
	 * 平方和の計算
	 * @param templateMap
	 * @param targetMap
	 * @return
	 */
	public static Double calcSquareSum(Map<String, Double>templateMap, Map<String, Integer> targetMap){
		Double result = 0.0;
		
		for(String key : targetMap.keySet()){
			result += Math.pow(targetMap.get(key).doubleValue() - templateMap.get(key).doubleValue(), 2);
		}
		
		return result;
	}

}
