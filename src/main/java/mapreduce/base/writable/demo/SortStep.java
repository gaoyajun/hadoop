package mapreduce.base.writable.demo;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 对sum处理的结果进行sort
 * 输入数据格式:account,in,out,surplus
 * 输出格式:account,in,out,surplus
 */
public class SortStep {


	public static void main(String[] args) throws Exception {

		if (args == null || args.length == 0) {
			args = new String[]{"file/sum-sort/sort", "target/out"};
		}

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		job.setJarByClass(SortStep.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.setMapperClass(SortMapper.class);
		job.setMapOutputKeyClass(InfoBean.class);
		job.setMapOutputValueClass(NullWritable.class);

		job.setReducerClass(SortReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(InfoBean.class);

		job.waitForCompletion(true);
	}


	/**
	 * map端解析每条record并封装为自定义的InfoBean类型实例,value为空
	 */
	public static class SortMapper extends Mapper<LongWritable, Text, InfoBean, NullWritable> {

		private InfoBean k = new InfoBean();

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String[] fields = line.split("\t");
			String account = fields[0];
			double in = Double.parseDouble(fields[1]);
			double out = Double.parseDouble(fields[2]);

			k.set(account, in, out);
			context.write(k, NullWritable.get());
		}
	}

	/**
	 * 在reduce之前框架对key自动排序,
	 * reduce获取数据后通过key对象获取account输出
	 * 结果为:account,account,income,expenses
	 */
	public static class SortReducer extends Reducer<InfoBean, NullWritable, Text, InfoBean> {

		private Text k = new Text();

		@Override
		protected void reduce(InfoBean bean, Iterable<NullWritable> values,
		                      Context context) throws IOException, InterruptedException {

			String account = bean.getAccount();
			k.set(account);
			context.write(k, bean);
		}
	}

}
