package test.spark;

import static org.apache.spark.sql.functions.col;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class ObjectRecogniator {

	public static void main(String ... args){
		try{
				Logger log = Logger.getLogger(ObjectRecogniator.class);
				
				// ストリーミングの設定をする
				SparkSession session = SparkSession.builder().appName("ObjectRecogniator").master("local").getOrCreate();
				
				// 物認識用のモデルをロードする
				MultilayerPerceptronClassificationModel model = MultilayerPerceptronClassificationModel.load("/home/fujii/spark-2.1.0-bin-hadoop2.7/app/model/Model_20170401");

				// ファイルを読み込む
				Dataset<Row> dataset = session.read().format("libsvm").load(args[0]);

				// 予測値毎の件数を取得して表示する
				model.transform(dataset).groupBy(col("prediction")).count().show(false);
				
		}catch(Throwable th){
			th.printStackTrace();
		}
	}
	
	public static class Container implements Serializable{
		/**
		 * シリアルバージョンID
		 */
		private static final long serialVersionUID = 1L;
		
		private String label;
		private int sensor1;
		private int sensor2;
		
		public Container(String a, int b, int c){
			label = a;
			sensor1 = b;
			sensor2 = c;
		}

		public Container(){
			
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public int getSensor1() {
			return sensor1;
		}

		public void setSensor1(int sensor1) {
			this.sensor1 = sensor1;
		}

		public int getSensor2() {
			return sensor2;
		}

		public void setSensor2(int sensor2) {
			this.sensor2 = sensor2;
		}
	}
}

