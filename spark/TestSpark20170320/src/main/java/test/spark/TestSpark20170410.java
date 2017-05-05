package test.spark;

import static org.apache.spark.sql.functions.col;

import java.util.List;

import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;

import net.arnx.jsonic.JSON;

public class TestSpark20170410 {

	public static void main(String... args) {
		try {
			// 物認識用のモデルをロードする
			MultilayerPerceptronClassificationModel model = MultilayerPerceptronClassificationModel
					.load("/home/fujii/spark-2.1.0-bin-hadoop2.7/app/model/Model_20170401");

			// ストリーミングの設定をする
			SparkSession session = SparkSession.builder().appName("myStreaming").getOrCreate();
			Dataset<Row> lines = session.readStream().format("socket").option("host", "localhost").option("port", 25)
					.load();
			
			lines.as(Encoders.STRING()).foreach(new ForeachFunction<String>(){

				private static final long serialVersionUID = 1L;

				@Override
				public void call(String name) throws Exception {
					// ファイルを読み込む
					Dataset<Row> dataset = session.read().format("libsvm").load(name);

					// 予測値毎の件数を取得して表示する
					Dataset<Row> predictionCount = model.transform(dataset).groupBy(col("prediction")).count();

					predictionCount.show();
				}
				
			});


			// ストリーミングバッチを開始する
			StreamingQuery query = lines.writeStream().outputMode("complete").format("console").start();

			query.awaitTermination();
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}
}