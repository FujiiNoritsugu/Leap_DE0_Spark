package test.spark;

import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import net.arnx.jsonic.JSON;

public class TestSpark20170320 {

	/**
	 * ストリーミング処理で一秒毎にデータを受信する
	 * @param args
	 */
	public static void main(String... args) {
		try {
			SparkConf conf = new SparkConf().setMaster("local").setAppName("Test20170320");
			JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(1));
			JavaReceiverInputDStream<String> lines = jsc.socketTextStream("localhost", 25);

			// 整数型Listオブジェクトに変換する
			JavaDStream<Integer> dataList = lines.flatMap(data -> {
				return ((List<Integer>)JSON.decode(data)).iterator();
			});

			// 整数型のペアに分けて多層パーセプトロンを適用する
			dataList.foreachRDD(data->{
				List<Integer> valueList= data.collect();
				valueList.stream().forEach(value->{
					int temp = value.intValue();
					int sensor1 = 0x00000fff & temp;
					int sensor2 = ((0x00fff000 & temp) >> 12);
					
				});
			});
			
			jsc.start();
			jsc.awaitTermination();
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}
}