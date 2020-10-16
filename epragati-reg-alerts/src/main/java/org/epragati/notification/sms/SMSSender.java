package org.epragati.notification.sms;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SMSSender extends RouteBuilder {

	private static final Logger logger = LoggerFactory.getLogger(SMSSender.class);

	private final static String SMS_QUEUE = "activemq:QUEUE.IN.sms";
	private final static String SMS_REPLY_QUEUE = "activemq:QUEUE.OUT.sms";

	@Value("${activemq.redelivery.attempts}")
	private String activemqRedeliveryAttempt;

	@Override
	public void configure()  {
		try{
			logger.info("Configuring SMS route ");
			from(SMS_QUEUE)
			.doTry()
			.process(new SMSHandler())
			.doCatch(Exception.class)
			.doFinally()
			.to(SMS_REPLY_QUEUE);
		//.maximumRedeliveries(activemqRedeliveryAttempt)
		}
		catch(Exception e){
			logger.error(" Exception [{}]",e.getLocalizedMessage());
		}
	}
}
