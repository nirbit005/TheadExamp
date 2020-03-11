package cris.apos.prs.chart;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class ReportCHTEngg {
	
	public static void mailreport(String filename, String fildate){
		
		BufferedReader br1 = null;	
		String rdLine = null;
		
		 // Recipient's email ID needs to be mentioned.
		  String chartusr[] = {"sangeetha@cris.org.in", "bindesh.dhakarwar@cris.org.in",
				  			 "Umadevi.K@cris.org.in", "sareena.k@cris.org.in", "yadav.nishant@cris.org.in", "bijumon.c@cris.org.in",
						     "Naik.Guruprasad@cris.org.in","kumar.nirmal@cris.org.in"};
		  
	//      String to[] = {"asishgupta@cris.org.in", "kumar.nirmal@cris.org.in"};

	      // Sender's email ID needs to be mentioned
	      String from = "cht_support@cris.org.in";

	      // Assuming you are sending email from localhost
	      String host = "172.16.1.206";
	      
	      String pwd = "lotusnotes";	

	      // Get system properties
	      Properties properties = System.getProperties();
	      
	      properties.put("mail.smtp.starttls.enable", "true");

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);
	      
	      properties.put("mail.smtp.user", from);
	      properties.put("mail.smtp.password", pwd);
	      properties.put("mail.smtp.port", "25");
	      properties.put("mail.smtp.auth", "true");

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties);

	      try {
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	        // message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	         
	         String TO;
	         for(int i=0; i < chartusr.length; i++){
	        	 TO = chartusr[i];
	        	 message.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
	         }

	//         String msgdate = fildate.substring(0, 2) + "-" + fildate.substring(2, 4) + "-" + fildate.substring(4);
	         // Set Subject: header field
	         
	         DateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			  Date msgdate = new Date();
			  
	         message.setSubject("CHART STATUS TABLE UPLOAD ON  " + dateFormat1.format(msgdate) + " !!!");

	         // Now set the actual message
	     //    message.setText("This is actual message");
	         
	           String MSGS = "";	//"This is the Subject Line!";
	           
	           br1 = new BufferedReader(new FileReader(filename.trim()));
	           
	           while ( (rdLine = br1.readLine()) != null) {
	        	   
	        	   MSGS = MSGS + rdLine;
	        	   MSGS =   String.format("%s%n", MSGS);
	           }
	           
	           BodyPart messageBodyPart=new MimeBodyPart();
	           messageBodyPart.setText(MSGS);
	           Multipart multipart=new MimeMultipart();
	           multipart.addBodyPart(messageBodyPart);
	           
	           message.setContent(multipart);
	           Transport transport=session.getTransport("smtp");
	           transport.connect(host, from, pwd);

	         // Send message
	       //  Transport.send(message);
	           transport.sendMessage(message,message.getAllRecipients());  
	         System.out.println("Sent message successfully....");
	         br1.close();
	      }catch (MessagingException | IOException mex) {
	         mex.printStackTrace();
	      }

	}

}
