package test.spark;

import static org.apache.spark.sql.functions.col;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class TestSpark20170412 {

	public static Logger log = Logger.getLogger(TestSpark20170411.class);

	public static void main(String ... args){
		try{
			// 物認識用のモデルをロードする
			MultilayerPerceptronClassificationModel model = MultilayerPerceptronClassificationModel
					.load("/home/fujii/spark-2.1.0-bin-hadoop2.7/app/model/Model_20170401");

			SparkSession session = SparkSession.builder().appName("myStreaming").getOrCreate();

			Socket socket = new Socket("localhost", 25);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while(true){
				String fileName = reader.readLine();

log.info("fileName = " + fileName);
				// ファイルを読み込む
				Dataset<Row> dataset = session.read().format("libsvm").load(fileName);

				// 予測値毎の件数を取得して表示する
				Dataset<Row> predictions = model.transform(dataset).groupBy(col("prediction")).count();

				predictions.show();
			}

		}catch(Throwable th){
			th.printStackTrace();
		}
	}
}
