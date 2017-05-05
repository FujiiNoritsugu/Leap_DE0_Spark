package test.spark;

import java.util.List;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import net.arnx.jsonic.JSON;

public class TestSpark20170329 {

	public static void main(String ... args){
		// ストリーミングの設定をする
		SparkSession session = SparkSession.builder().appName("myStreaming").getOrCreate();
		Dataset<Row> lines = session.readStream().format("socket").option("host", "localhost").option("port", 25).load();
		Dataset<Integer> values = lines.as(Encoders.STRING()).flatMap(value ->{
			return ((List<Integer>)JSON.decode(value)).iterator();
		}, Encoders.INT());
		
		// 圧力、曲げ、ラベルのデータセットに変換する
		Dataset<TrainData> trainData = values.map(new MapFunction<Integer, TrainData>(){

			@Override
			public TrainData call(Integer value) throws Exception {
				int temp = value.intValue();
				int sensor1 = 0x00000fff & temp;
				int sensor2 = ((0x00fff000 & temp) >> 12);
				String label = args[0];
				return new TrainData(sensor1, sensor2, label);
			}
			
		}, Encoders.bean(TrainData.class));
		
		// 多層パーセプトロンで学習を行う
		MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier();
		trainer.setLayers(new int[]{2, 4, 4, 3})
		.setBlockSize(10)
		.setSeed(1234L)
		.setMaxIter(100);
		
	}
	
	private static class TrainData{
		public TrainData(int sensor1, int sensor2, String name){
			this.pressure = sensor1;
			this.bend = sensor2;
			this.label = name;
		}
		private int pressure;
		private int bend;
		private String label;
		public int getPressure() {
			return pressure;
		}
		public void setPressure(int pressure) {
			this.pressure = pressure;
		}
		public int getBend() {
			return bend;
		}
		public void setBend(int bend) {
			this.bend = bend;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
	}
}
