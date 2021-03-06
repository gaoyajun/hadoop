package mapreduce.nginxlog.visits;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 第四步，再次清洗Session日志，并生成Visits信息表
 * 输入数据:time IP_addr session request_URL referal
 * 输出数据: session start_time end_time entry_page leave_page visit_page_num IP_addr referal
 */
public class VisitsInfo {

	/**
	 * Mapper端解析每行,输出session为key,整行为value
	 */
	private static class visitMapper extends Mapper<Object, Text, Text, Text> {
		private Text word = new Text();

		public void map(Object key, Text value, Context context) {

			String line = value.toString();
			String[] webLogContents = line.split(" ");

			// 根据session来分组
			word.set(webLogContents[2]);
			try {
				context.write(word, value);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reducer端对相同Session聚合
	 * 与前几个MR类似业务逻辑,根据相同Session解析数据保存在一个ArrayList中排序然后遍历
	 * 通过一个HashMap数据结构viewedPagesMap保存
	 */
	private static class visitReducer extends Reducer<Text, Text, Text, NullWritable> {

		private Text content = new Text();
		private NullWritable v = NullWritable.get();
		VisitsInfoParser visitsParser = new VisitsInfoParser();
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//PageViewsParser pageViewsParser = new PageViewsParser();
		Map<String, Integer> viewedPagesMap = new HashMap<>();

		//String entry_URL = "";
		//String leave_URL = "";
		int total_visit_pages = 0;

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			// 将session所对应的所有浏览记录按时间排序
			ArrayList<String> browseInfoGroup = new ArrayList<>();
			for (Text browseInfo : values) {
				browseInfoGroup.add(browseInfo.toString());
			}
			//time IP_addr session request_URL referal
			Collections.sort(browseInfoGroup, new Comparator<String>() {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				public int compare(String browseInfo1, String browseInfo2) {
					String dateStr1 = browseInfo1.split(" ")[0] + " " + browseInfo1.split(" ")[1];
					String dateStr2 = browseInfo2.split(" ")[0] + " " + browseInfo2.split(" ")[1];
					Date date1, date2;
					try {
						date1 = sdf.parse(dateStr1);
						date2 = sdf.parse(dateStr2);
						if (date1 == null && date2 == null)
							return 0;
						return date1.compareTo(date2);
					} catch (ParseException e) {
						e.printStackTrace();
						return 0;
					}
				}
			});

			// 统计该session访问的总页面数,第一次进入的页面，跳出的页面
			for (String browseInfo : browseInfoGroup) {

				String[] browseInfoStrArr = browseInfo.split(" ");
				String curVisitURL = browseInfoStrArr[3];
				Integer curVisitURLInteger = viewedPagesMap.get(curVisitURL);
				if (curVisitURLInteger == null) {
					viewedPagesMap.put(curVisitURL, 1);
				}
			}

			total_visit_pages = viewedPagesMap.size();
			String visitsInfo = visitsParser.parser(browseInfoGroup, total_visit_pages + "");
			content.set(visitsInfo);
			try {
				context.write(content, v);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();
		//conf.set("fs.defaultFS", "hdfs://hadoop:9000");
		Job job = Job.getInstance(conf);
		job.setJarByClass(VisitsInfo.class);


		job.setMapperClass(visitMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setReducerClass(visitReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		String dateStr = new SimpleDateFormat("yy-MM-dd").format(new Date());
		FileInputFormat.setInputPaths(job, new Path("file/sessiondata/" + dateStr + "/*"));
		FileOutputFormat.setOutputPath(job, new Path("file/visitsinfo" + dateStr + "/"));

		boolean res = job.waitForCompletion(true);
		System.exit(res ? 0 : 1);
	}
}