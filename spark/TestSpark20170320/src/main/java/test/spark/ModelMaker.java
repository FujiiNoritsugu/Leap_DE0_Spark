package test.spark;

import java.io.IOException;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.StandardScaler;
import org.apache.spark.ml.feature.StandardScalerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class ModelMaker {

	public static void main(String ... args) {
		SparkSession session = SparkSession.builder().appName("modelMaker").master("local").getOrCreate();
		String path = "./data/20170505/20170505_libsvm";
		Dataset<Row> dataset = session.read().format("libsvm").load(path);

		// Split the data into train and test
		Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.6, 0.4}, 1234L);
		Dataset<Row> train = splits[0];
		Dataset<Row> test = splits[1];

		StandardScaler standard = new StandardScaler().setWithMean(true).setInputCol("features").setOutputCol("scaler");
		
		// specify layers for the neural network:
		// input layer of size 4 (features), two intermediate of size 5 and 4
		// and output of size 3 (classes)
		//int[] layers = new int[] {2, 5, 4, 3};
		int[] layers = new int[] {2, 5, 5, 3};

		// create the trainer and set its parameters
		MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
		  .setLayers(layers)
		  .setBlockSize(128)
		  .setSeed(1234L)
		  .setMaxIter(100)
		  .setFeaturesCol("scaler");

		Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{standard, trainer});
		// train the model
		//MultilayerPerceptronClassificationModel model = trainer.fit(train);
		PipelineModel model = pipeline.fit(train);

		// モデルをセーブする
		try{
			model.save("model/Model_20170505");
		}catch(Throwable th){
			th.printStackTrace();
		}
		// compute accuracy on the test set
		Dataset<Row> result = model.transform(test);
		Dataset<Row> predictionAndLabels = result.select("prediction", "label");
		MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
		  .setMetricName("accuracy");

		System.out.println("Test set accuracy = " + evaluator.evaluate(predictionAndLabels));

	}
}
