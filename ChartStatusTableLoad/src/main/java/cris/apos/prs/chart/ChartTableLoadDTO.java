package cris.apos.prs.chart;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class ChartTableLoadDTO {

	@Autowired
	EntityManager manager;
	
	ResourceBundle rb = ResourceBundle.getBundle("sql");
	

	public int chartStatusUpdateDB(int qryTyp ,String trainNo, String startDate , String remoLoc, String remDate, String schDepTime,
				String zoneCode, String siteId) {
			int stat = 0;
			Query qry;
		try {
			  if( qryTyp == 1 ) {
				  qry = manager.createNativeQuery(rb.getString("insertRec"));
			
				  qry.setParameter(1, trainNo);
				  qry.setParameter(2, Date.valueOf(startDate) );
				  qry.setParameter(3, remoLoc);
				  qry.setParameter(4, Date.valueOf(remDate) );
				  qry.setParameter(5, Timestamp.valueOf(schDepTime) );			// yyyy-mm-dd hh:mm:ss[.fffffffff]
				  qry.setParameter(6, zoneCode);
				  qry.setParameter(7, siteId);
			  }else {
				  qry = manager.createNativeQuery(rb.getString("updtRec"));
					 
				  qry.setParameter(1, Timestamp.valueOf(schDepTime) );
				  qry.setParameter(2, trainNo);
				  qry.setParameter(3, Date.valueOf(startDate) );
				  qry.setParameter(4, remoLoc);
				  qry.setParameter(5, Date.valueOf(remDate) );
			  }
			
			  stat = qry.executeUpdate();
			  
			  System.out.println("Qry Stat- " + stat + " Train No. - " + trainNo + "--" + remoLoc + "--" + 
					  	 "Start Date - " + startDate + " -- " + "Remote Date- " + remDate + "Sched Dep. Time - " + schDepTime + " -- " + 
						"Zone Code - " + zoneCode + " -- " + " Stn. PRS Site - " + siteId); 	
			  
		 }catch( Exception e) {
			  
				System.out.println("Error inserting chart status record " +e.getMessage() + " Stat- " + stat +
						" Train- "+ trainNo + "--" + remoLoc + "--" +  
						"Start Date - " + startDate + " -- " + "Remote Date- " + remDate);	//NoResultException 
				
				return stat;
									
 		   } 
		
		return stat;
	}
	
	public int checkRecAvl(String trainNo, String startDate , String remoLoc, String remDate) {
		
		int stat = -1;
		
		try {
			
			Query qry = manager.createNativeQuery(rb.getString("chkRec"));
			
			  qry.setParameter(1, trainNo);
			  qry.setParameter(2, Date.valueOf(startDate) );
			  qry.setParameter(3, remoLoc);
			  qry.setParameter(4, Date.valueOf(remDate) );
			  
		//	  stat = qry.getFirstResult();
			  List<?> obj =  qry.getResultList();
		 	     stat = obj.size();
			  
		}catch( Exception e) {
			  
				System.out.println("Error inserting chart status record " +e.getMessage() + " Stat- " + stat +
						" Train- "+ trainNo + "--" + remoLoc + "--" +  
						"Start Date - " + startDate + " -- " + "Remote Date- " + remDate);	//NoResultException 
				
				return stat = 2;
									
		   } 
		return stat;
	}
	
}
