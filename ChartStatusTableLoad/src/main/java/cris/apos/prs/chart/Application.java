package cris.apos.prs.chart;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cris.apos.cache.train.TrainMaster;

@SpringBootApplication
@ComponentScan({"cris.apos"})
@EnableAsync
public class Application {

//	@Autowired
//	TrainInfoService trainInfoService;
	
	
	public static void main(String[] args) {
			
		String  fileName = null;
		String  fileNamePass = null;
		String fildate = null;
		BufferedWriter bufferedWriter = null;
		AtomicInteger anyErr = new AtomicInteger();
		
		ApplicationContext applicationContext = SpringApplication.run(Application.class, args);
		TrainInfoService trainInfoService = applicationContext.getBean(TrainInfoService.class);
	//	System.out.println("Args value is- " + args[1]);  --- getting err in argument.
	
		 try{
			 
			 DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
			 Calendar cal = Calendar.getInstance();
	         fildate = dateFormat.format(cal.getTime());
		
	         fileNamePass   = "chartstatus_tblupld_log_" + fildate + ".txt";      
	         fileName =  trainInfoService.fileDir + "chartstatus_tblupld_log_" + fildate + ".txt";
		 
	         FileWriter fileWriter = new FileWriter(fileName);
			
	         bufferedWriter = new BufferedWriter(fileWriter);
	 
	         DateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	         Date batchdatetime = new Date();
	  
	         bufferedWriter.write("Chart Status Table Upload from STATIC CACHE to CHART DB on - " + dateFormat1.format(batchdatetime) + '\n');
	         bufferedWriter.write('\n');
	 
	         List<TrainMaster> trnMasterList = trainInfoService.getTrainListFun();
	         
	         CompletableFuture<Integer> tot1 = trainInfoService.trainLoadToDB(0, trnMasterList.subList(0, trnMasterList.size()/2),anyErr, 0);
	         CompletableFuture<Integer> tot11 = trainInfoService.trainLoadToDB(0, trnMasterList.subList(trnMasterList.size()/2, trnMasterList.size()),anyErr, 0);
	         CompletableFuture<Integer> tot2 = trainInfoService.trainLoadToDB(1, trnMasterList.subList(0, trnMasterList.size()/2),anyErr, 0);
	         CompletableFuture<Integer> tot22 = trainInfoService.trainLoadToDB(1, trnMasterList.subList(trnMasterList.size()/2, trnMasterList.size()),anyErr, 0);
	         CompletableFuture<Integer> tot3 = trainInfoService.trainLoadToDB(2, trnMasterList.subList(0, trnMasterList.size()/2),anyErr, 0);
	         CompletableFuture<Integer> tot33 = trainInfoService.trainLoadToDB(2, trnMasterList.subList(trnMasterList.size()/2, trnMasterList.size()),anyErr, 0);
	         
	         CompletableFuture.allOf(tot1, tot11, tot2, tot22, tot3, tot33).join();
	         
	   				int val1 = 	tot1.get() + tot11.get();
			   		bufferedWriter.write("Loading data from current day to next 2 days"+ '\n');
			   		bufferedWriter.write("Total No. of record uploaded for current + 2 days - " + val1 + '\n');
		        	bufferedWriter.write("Loading data completed from current day to next 2 days"+ '\n');		
		        	bufferedWriter.write('\n');
			   		
			   		int val2 = tot2.get() + tot22.get();			
			   		bufferedWriter.write("Loading data from current day + 1 to next 1 day"+ '\n');
			   		bufferedWriter.write("Total No. of record uploaded for current day + 1 to next 1 day - " + val2 + '\n'); 
			   		bufferedWriter.write("Loading data completed from current day + 1 to next 1 day"+ '\n');
			   		bufferedWriter.write('\n');
			   		
			   		int val3 = 	tot3.get() + tot33.get();				
			   		bufferedWriter.write("Loading data for current day + 2 "+ '\n');
			   		bufferedWriter.write("Total No. of record uploaded for current day + 2 - " + val3 + '\n');	 
			   		bufferedWriter.write("Loading data completed for current day + 2 "+ '\n');
			   		bufferedWriter.write('\n');
			 
			   		int totval = val1+val2 +val3;
			  bufferedWriter.write("Total No. of records uploaded - " + totval + '\n');
			  
		      if(anyErr.get() == 0)
		    	  bufferedWriter.write("DATA LOADED SUCCESSFULLY" + '\n');
		      else
		    	  bufferedWriter.write("DATA LOADED WITH ERROR" + '\n');		      
	        
		
		 }catch (Exception e) {
				e.printStackTrace();
		//		bufferedWriter.write(e.toString());
						
			}finally {
	            try {                        
	                bufferedWriter.close();
	                ReportCHTEngg.mailreport(fileName, fildate);
	 // FTP Commented as from APP server, ftp not allowed to our local PRS server. 
//	                Only mail working and sufficient for info purpose.               
//	                FtpLogtoPRS.ftplogfile(fileName, fileNamePass);
	                
	                System.out.println("Shuting Down Aplication...");
//	   	         	SpringApplication.exit(applicationContext, () -> 0);
	                System.exit(0);
	   	         
	             } catch (Exception e) {
	                 e.printStackTrace();
	                 
	 //                System.out.println("Shuting Down Aplication...");
	//	   	         SpringApplication.exit(applicationContext, () -> 0);
	//                 System.exit(0);
	             }
	         }
		
	}
	
	@Bean("threadPoolTaskExecutor")
	public TaskExecutor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setMaxPoolSize(1000);
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setThreadNamePrefix("Async-");
		return executor;
	}

}
