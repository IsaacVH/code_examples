package org.isaac.cs4000.hw7;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class AverageTemp {

	public static double haversine(double theta) {
		return(1 - Math.cos(theta)/2.0);
	}

	public static double distance(double lat1, double long1,
									double lat2, double long2) {
		double O1, O2, L1, L2;
		O1 = (lat1 * Math.PI)/180.0;
		L1 = (long1 * Math.PI)/180.0;
		O2 = (lat2 * Math.PI)/180.0;
		L2 = (long2 * Math.PI)/180.0;
		
		double earth_radius = 3963.19; //(Units are in miles)
		double delta_lat = O2 - O1;
		double delta_long = L2 - L1;
		return(2 * earth_radius * Math.asin(Math.sqrt(haversine(delta_lat) +
				Math.cos(O1) * Math.cos(O2) * haversine(delta_long))));
	}

	public static class TempMapper extends
			Mapper<Object, Text, Text, DoubleWritable> {

		private final static DoubleWritable temperature = new DoubleWritable();
	    private Text word = new Text();
	      
	    public void map(Object key, Text value, Context context
	                    ) throws IOException, InterruptedException {

	      	String line = value.toString();
	      	String year, lat2, long2, temp;
				
			year = line.substring(15, 19);
			lat2 = line.substring(28, 34);
			long2 = line.substring(34, 41);
			temp = line.substring(87, 92);
			
			double dlat2 = Double.parseDouble(lat2);
			double dlong2 = Double.parseDouble(long2);
			double dtemp = Double.parseDouble(temp);

			Configuration conf = context.getConfiguration();
			String lat1 = conf.get("Latitude");
			String long1 = conf.get("Longitude");

			double dlat1 = Double.parseDouble(lat1);
			double dlong1 = Double.parseDouble(long1);

			if(distance(dlat1, dlong1, dlat2, dlong2) <= 50.0) {
				temperature.set(dtemp);
				System.out.println(dtemp);
				word.set(year);
				System.out.println(year);
				context.write(word, temperature);
			}
	    }
		
	}

	public static class TempReducer extends
			Reducer<Text, DoubleWritable, Text, DoubleWritable> {

		private DoubleWritable result = new DoubleWritable();

	    public void reduce(Text key, Iterable<DoubleWritable> values, 
	                       Context context
	                       ) throws IOException, InterruptedException {

	      double sum = 0;
	      int count = 0;
	      for (DoubleWritable val : values) {
	        sum += val.get();
	        System.out.println(sum);
	        count++;
	      }

	      double avrg = sum/count;
	      result.set(avrg);
	      context.write(key, result);
	    }
	}
	
	public static void main(String[] args) {

		try{

			Configuration conf = new Configuration();
			conf.set("Latitude", args[1]);		//Sending the latitude and longitude
			conf.set("Longitude", args[2]);		//to the map/reduce classes
			String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		    if (otherArgs.length != 3) {
		      System.err.println("Usage: AverageTemp <in> <lat> <long>");
		      System.exit(2);
		    }
			Job job = new Job(conf, "Average Temps");
			job.setJarByClass(AverageTemp.class);
		    job.setMapperClass(TempMapper.class);
		    job.setCombinerClass(TempReducer.class);
		    job.setReducerClass(TempReducer.class);
		    job.setOutputKeyClass(Text.class);
		    job.setOutputValueClass(DoubleWritable.class);
		    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
	   	 	FileOutputFormat.setOutputPath(job, new Path("./homework7"));
			System.exit(job.waitForCompletion(true) ? 0 : 1);
			
		} 
		catch (IOException e) {
			e.printStackTrace();

		}
		catch (InterruptedException f) {
		  	f.printStackTrace();
		}
		catch (ClassNotFoundException g) {
			g.printStackTrace();
		}

		/*
		File file = new File(args[0]);
		
		try {

			// Input will be from: /user/juedes/weather/1950.txt
			Scanner sc = new Scanner(file);

			String line;
			while (sc.hasNextLine()) {
				
				line = sc.nextLine();
				
				String year;
				String lat2;
				String long2;
				String temp;
				
				year = line.substring(15, 19);
				lat2 = line.substring(28, 34);
				long2 = line.substring(34, 41);
				temp = line.substring(87, 92);
				
				double dlat2 = Double.parseDouble(lat2);
				double dlong2 = Double.parseDouble(long2);
				double dtemp = Double.parseDouble(temp);
				
				if(distance(lat_c, long_c, dlat2, dlong2) <= 50.0) {
					if() {
						
					}
				}
			}
			
			sc.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		*/

	}

}
