package test.spark.mapper;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

public class LibsvmMapper implements MapFunction<String, Row>{

	/**
	 * シリアルバージョンID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Row call(String data) throws Exception {
		String [] tempArray = data.split(",");
		//double order = Double.parseDouble(tempArray[0]);
		double sensor1Value = Double.parseDouble(tempArray[0]);
		double sensor2Value = Double.parseDouble(tempArray[1]);
		return RowFactory.create(0.0, Vectors.dense(sensor1Value, sensor2Value));
	}

}
