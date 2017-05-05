package test.spark;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.max;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class TestSpark20170411 {
	
public static Logger log = Logger.getLogger(TestSpark20170411.class);

	public static void main(String... args) {
		try {
			// 物認識用のモデルをロードする
			MultilayerPerceptronClassificationModel model = MultilayerPerceptronClassificationModel
					.load("/home/fujii/spark-2.1.0-bin-hadoop2.7/app/model/Model_20170401");

			// ストリーミングの設定をする
			SparkSession session = SparkSession.builder().appName("myStreaming").getOrCreate();
			Dataset<Row> lines = session.readStream().format("socket").option("host", "localhost").option("port", 25)
					.load();
//log.info("lines show = " );
//lines.show();
			  StructType structType = new StructType();
			    structType = structType.add("prediction", DataTypes.StringType, false);
			    structType = structType.add("count", DataTypes.IntegerType, false);

			    ExpressionEncoder<Row> encoder = RowEncoder.apply(structType);

			Dataset<Row> datasets = lines.as(Encoders.STRING()).flatMap(new FlatMapFunction<String, Row>(){

				private static final long serialVersionUID = 1L;

				@Override
				public Iterator<Row> call(String name) throws Exception {
log.info("name = " + name);
					// ファイルを読み込む
					Dataset<Row> dataset = session.read().format("libsvm").load(name);

					// 予測値毎の件数を取得して表示する
					Dataset<Row> predictions = model.transform(dataset).groupBy(col("prediction")).count();

					return predictions.toLocalIterator();
				}
				
			}, encoder);

//			lines.as(Encoders.STRING()).foreach(new ForeachFunction<String>(){
//
//				private static final long serialVersionUID = 1L;
//
//				@Override
//				public void call(String name) throws Exception {
//					// ファイルを読み込む
//					Dataset<Row> dataset = session.read().format("libsvm").load(name);
//
//					// 予測値毎の件数を取得して表示する
//					Dataset<Row> predictionCount = model.transform(dataset).groupBy(col("prediction")).count();
//
//					predictionCount.show();
//				}
//				
//			});

			Dataset<Row> predictions = model.transform(datasets).groupBy(col("prediction")).count();


			// ストリーミングバッチを開始する
			//StreamingQuery query = datasets.agg(max("count")).writeStream().outputMode("complete").format("console").start();
			StreamingQuery query = predictions.writeStream().outputMode("complete").format("console").start();

			query.awaitTermination();
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}


}
