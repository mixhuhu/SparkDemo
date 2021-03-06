package com.huangyueran.spark.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.derby.tools.sysinfo;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

/**
 * @category 读取JDBC数据源
 * @author huangyueran
 * @time 2017-7-24 13:58:59
 */
public class JDBCDataSource {
	public static void main(String[] args) {
//		SparkConf conf = new SparkConf().setAppName("JDBCDataSource").setMaster("local");
		SparkConf conf = new SparkConf().setAppName("JDBCDataSource");
		JavaSparkContext sc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);

		Map<String, String> options = new HashMap<String, String>();
		options.put("url", "jdbc:mysql://192.168.68.1:3306/tourismdb");
		options.put("dbtable", "t_user");
		options.put("user", "root");
		options.put("password", "666666");

		// 加载jdbc数据配置信息 并不会立即连接数据库
		DataFrame dataFrame1 = sqlContext.read().format("jdbc").options(options).load();

		//		options.put("dbtable", "tb_item");
		//		DataFrame dataFrame2 = sqlContext.read().format("jdbc").options(options).load();

		// 读取jdbc表数据
		dataFrame1.javaRDD().foreach(new VoidFunction<Row>() {
			@Override
			public void call(Row row) throws Exception {
				System.out.println(row);
			}
		});
		
		// 将RDD数据存储到MYSQL中
		saveToMysql( sqlContext, options);

		sc.close();
	}

	/**
	 * @category 将RDD的数据存储到Mysql数据库中
	 * @param sc
	 * @param sqlContext
	 * @param options
	 */
	private static void saveToMysql( SQLContext sqlContext, Map<String, String> options) {
		options.put("url", "jdbc:mysql://192.168.68.1:3306/tourismdb");
		options.put("dbtable", "t_user");
		DataFrame dataFrame = sqlContext.read().format("jdbc").options(options).load();

		dataFrame.javaRDD().foreach(new VoidFunction<Row>() {
			@Override
			public void call(Row row) throws Exception {
				String sql = "insert into t_user( name, password, phone, email,type,status,del) values("
						+ "'"+ row.getString(1) + "'," 
						+ "'"+ row.getString(2) + "'," 
						+ "'"+ row.getString(3) + "'," 
						+ "'"+ row.getString(4) + "'," 
						+ row.getInt(5)+ ","
						+ row.getInt(6)+ ","
						+ row.getInt(7)+ ")";
				System.out.println(sql);
				Class.forName("com.mysql.jdbc.Driver");
				Connection conn = null;
				Statement statement = null;
				try {
					conn = DriverManager.getConnection("jdbc:mysql://192.168.68.129:3306/sparkdemo","root","666666");
					statement = conn.createStatement();
					statement.executeUpdate(sql);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if(statement!=null){
						statement.close();
					}
					if (conn!=null) {
						conn.close();
					}
				}
			}
		});
	}
}
