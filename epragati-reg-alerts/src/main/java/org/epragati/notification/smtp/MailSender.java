package org.epragati.notification.smtp;

import org.apache.camel.builder.RouteBuilder;
import org.epragati.notification.sms.SMSSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailSender extends RouteBuilder {

	private static final Logger logger = LoggerFactory.getLogger(SMSSender.class);
	private final static String EMAIL_QUEUE = "activemq:QUEUE.IN.email";
	private final static String EMAIL_REPLY_QUEUE = "activemq:QUEUE.OUT.email";
	
	@Value("${activemq.redelivery.attempts}")
	private String activemqRedeliveryAttempt;
	
	@Override
	public void configure()  {
		try{
			logger.info("Configuring EMAIL route ");
			from(EMAIL_QUEUE)
			.doTry()
			.process(new EmailHandler())
			.doCatch(Exception.class)
			.doFinally()
			.to(EMAIL_REPLY_QUEUE);
         // .maximumRedeliveries(activemqRedeliveryAttempt)
		}
		catch(Exception e){
			logger.error(" Exception [{}]",e.getLocalizedMessage());
		}
	}
}
