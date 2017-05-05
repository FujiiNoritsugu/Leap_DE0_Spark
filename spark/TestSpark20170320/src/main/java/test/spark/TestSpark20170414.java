package test.spark;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.max;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.ForeachWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import test.spark.encoder.LibsvmEncoder;
import test.spark.mapper.LibsvmMapper;
import test.spark.writer.PredictionWriter;

public class TestSpark20170414 {
	
public static Logger log = Logger.getLogger(TestSpark20170414.class);

	public static void main(String... args) {
		try {
			// 物認識用のモデルをロードする
//			MultilayerPerceptronClassificationModel model = MultilayerPerceptronClassificationModel
//					.load("/home/fujii/spark-2.1.0-bin-hadoop2.7/app/model/Model_20170419");

			PipelineModel model = PipelineModel
					.load("/home/fujii/spark-2.1.0-bin-hadoop2.7/app/model/Model_20170505");

			// ストリーミングの設定をする
			SparkSession session = SparkSession.builder().appName("myStreaming").getOrCreate();
			
			Dataset<Row> lines = session.readStream().format("socket").option("host", "localhost").option("port", 25)
					.load();

			// 文字列のデータセットをlibsvm形式のデータセットに変換する
			Dataset<Row> libsvms = lines.as(Encoders.STRING()).map(new LibsvmMapper(), LibsvmEncoder.getLibsvmEncoder());

			// モデルを適用する
			//Dataset<Row> predictions = model.transform(libsvms).groupBy(col("prediction")).count();
			Dataset<Row> predictions = model.transform(libsvms);

			// ストリーミングバッチを開始する
			StreamingQuery query = predictions
					.writeStream()
					.outputMode(OutputMode.Append())
					.foreach(new PredictionWriter())
					.start();

			
			query.awaitTermination();

		} catch (Throwable th) {
			th.printStackTrace();
		}
	}


}
