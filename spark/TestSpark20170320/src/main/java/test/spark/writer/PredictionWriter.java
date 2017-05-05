package test.spark.writer;

import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.spark.sql.ForeachWriter;
import org.apache.spark.sql.Row;

public class PredictionWriter extends ForeachWriter<Row> {

	/**
	 * シリアライズ番号
	 */
	private static final long serialVersionUID = 1L;

		transient private static Socket socket;	
		transient private static PrintWriter writer;
		transient private static Object lock = new Object();
		
	public PredictionWriter(){
	}
	
	@Override
	public void close(Throwable th) {
		if(th != null){
			th.printStackTrace();
		}
	}

	@Override
	public boolean open(long arg0, long arg1) {
		return true;
	}

	@Override
	public void process(Row row) {
		try {
			synchronized (lock) {
				if (writer == null) {
					try {
						socket = new Socket("localhost", 26);
						writer = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
					} catch (Throwable th) {
						th.printStackTrace();
					}
				}

				if (row != null) {
					writer.println(row.getDouble(row.fieldIndex("prediction")));
					writer.flush();
				}
			}
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

}
