package test.spark.encoder;

import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import scala.reflect.ClassTag;

public class LibsvmEncoder {

	public static Encoder <Row> getLibsvmEncoder(){
		  	StructType structType = new StructType();
		    structType = structType.add("label", DataTypes.DoubleType, false);
		    structType = structType.add("features", new VectorUDT(), false);
		    ExpressionEncoder<Row> encoder = RowEncoder.apply(structType);
		    return encoder;
	}

}
