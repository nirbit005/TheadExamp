package cris.apos.prs.chart;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import cris.apos.cache.api.train.TrainCache;
import cris.apos.cache.train.TrainDefinitionMaster;
import cris.apos.cache.train.TrainDetail;
import cris.apos.cache.train.TrainMaster;
import cris.apos.cache.train.TrainProfile;
import cris.apos.cache.train.TrainStation;


@Service
public class TrainInfoService {

	@Autowired
	TrainCache trainCache;
	
	@Autowired
	ChartTableLoadDTO chartTableLoadDTO;
	
	@Value("${filedir.path}")
	public String fileDir;

	private static Logger logger = LoggerFactory.getLogger(TrainInfoService.class);

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Integer> trainLoadToDB(int procDay, List<TrainMaster> trnMasterList, AtomicInteger anyErr, int noofRec) {
    	
 HashMap<String,String> zoneCodeSite = new HashMap<>(); 
		 
		 zoneCodeSite.put("ER", "C");
		 zoneCodeSite.put("EC", "C");
		 zoneCodeSite.put("NF", "C");
		 zoneCodeSite.put("EO", "C");
		 zoneCodeSite.put("SE", "C");
		 zoneCodeSite.put("SB", "C");
		 
		 zoneCodeSite.put("NR", "D");
		 zoneCodeSite.put("NW", "D");
		 zoneCodeSite.put("NE", "D");
		 zoneCodeSite.put("NC", "D");
		 
		 zoneCodeSite.put("KR", "B");
		 zoneCodeSite.put("CR", "B");	 
		 zoneCodeSite.put("WC", "B");
		 zoneCodeSite.put("WR", "B");
		 
		 zoneCodeSite.put("SR", "M");
		 zoneCodeSite.put("SC", "M");
		 zoneCodeSite.put("SW", "M");
    	
		LocalDate curDay = LocalDate.now();
 		LocalDate thirdDay =  curDay.plusDays(2);
 		
 		
	//	int noofRec = 0;
 		
		 						
		LocalDate startDate = curDay.plusDays(procDay);
		System.out.println("Processing staterd with start date - " + startDate);
   // 	List<TrainMaster> trnMasterList = trainCache.getAllTrains().stream().collect(Collectors.toList()) ;
    //	.forEach(trnMaster -> {
    	for(TrainMaster trnMaster :trnMasterList) {
    		    		
    		TrainProfile profile = getTrainProfile(trainCache.getTrainDetail(trnMaster.getTrainNumber()), startDate);
    		
    		if( profile != null ) {
    			
    		List<TrainStation> stnList = profile.getTrainScheduleMap().values().stream().filter(TrainStation::isRemoteLocation).collect(Collectors.toList());
    			//	.forEach(stn -> {
    				for(TrainStation stn : stnList)	{
    					LocalDate remDate = startDate.plusDays(stn.getDayCount());
    					String schDepTime = remDate.toString() + " " + stn.getDepartureTime().toString() + ":00";
    					int compareValue = remDate.compareTo(thirdDay);
    					
    					int stat = 0;
    					
    				if( compareValue == 0 || compareValue < 0 ) {
    					
    					stat = chartTableLoadDTO.checkRecAvl(trnMaster.getTrainNumber(), startDate.toString(), stn.getStationCode(), remDate.toString());
    					
    					stat = stat == 0 ? 1 : 2 ;
    					
    					stat = chartTableLoadDTO.chartStatusUpdateDB(stat, trnMaster.getTrainNumber(), startDate.toString(), stn.getStationCode(), 
    												remDate.toString(), schDepTime, stn.getZoneCode(), zoneCodeSite.get(stn.getZoneCode()) ) ;
    					
    					if(stat == 1)   noofRec++;
    		        	
    			//		logger.info("Processed Record, stat- {}, Train- {}, RemLoc- {}, startDate- {}, remDate- {}, ScheDepTime- {}, zoneCode- {}, stnSite-{} ",
    			//				stat, trnMaster.getTrainNumber(), stn.getStationCode(), startDate, remDate, schDepTime, stn.getZoneCode(), zoneCodeSite.get(stn.getZoneCode()));
    					
    		        	if(stat != 1 ){
    		        		logger.error("Error Updating record, Train- {}, RemLoc- {}, startDate- {}, remDate- {} ",  
    		        											trnMaster.getTrainNumber(), stn.getStationCode(), startDate, remDate);
    		        		anyErr = new AtomicInteger(1);
    		        	}
    		        	
    					}
    					
    				}
    		//		}); 
    		}
    	 }
    //	 });
    	   
    	return CompletableFuture.completedFuture(noofRec);
    }

  
    
    public TrainProfile getTrainProfile(TrainDetail trainDetail, LocalDate procDate) {
    	
    	TrainDefinitionMaster def = trainDetail.getTrainDefinitionMasterRange().get(procDate);
    	
    	if (def != null ) {		//&& def.isValid()
			int dayIndex = procDate.getDayOfWeek().getValue();
			int profileIndex = def.getTrainProfileIndex()[dayIndex - 1];
			return def.getTrainProfileMap().get(profileIndex);
			
		/*	
			if (profile != null) {
				TrainStation srcStation = getStationDetail(profile, src);
				
				if(srcStation!=null) {
					int dayCount = srcStation.getDayCount();
	
					if (startDate.plusDays(dayCount).equals(jrnyDate)) {
						return new ProfileOutput(def, profile, startDate, srcStation);
					}
				}

			}  */
			
		}

		return null;
    	   	
    }
    
    public List<TrainMaster> getTrainListFun(){
		
		 return  trainCache.getAllTrains().stream().collect(Collectors.toList()) ;
		
	}
    
}
