package org.epragati.notification.sms;

import java.util.Map.Entry;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang3.StringUtils;
import org.epragati.AbstractHandler;
import org.epragati.notification.DestinationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSHandler extends AbstractHandler implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(SMSHandler.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		CamelContext context = new DefaultCamelContext();
		DestinationInfo destinationInfo = exchange.getIn().getBody(DestinationInfo.class);

		logger.info("SMPP Destination Info [{}] " ,destinationInfo);

		exchange.getIn().setBody(StringUtils.EMPTY);
		String uri = buildURI(destinationInfo);
		setBodyAndHeaders(exchange, destinationInfo);

		logger.debug("URI : [{}]",uri);

		Endpoint endpoint = context.getEndpoint(uri);
		Producer producer = endpoint.createProducer();
		producer.start();
		producer.process(exchange);

		String mobileNumber = StringUtils.EMPTY;
		
		for (Entry<String, String> parameter : destinationInfo.getParameters().entrySet()) {
			if(parameter.getKey().equals("mobileno")) {
				mobileNumber = parameter.getValue();
			}
		}
		
		logger.info(" Mobile [{}] - Generated response   [{}]  ",mobileNumber, exchange.getOut().getBody(String.class));

		if (exchange.getException() != null) 
		{
			Exception exchangeException = exchange.getException();
			logger.error("Error [{}] ",exchangeException.getMessage());
			handleException(exchange, destinationInfo);

		}else{
			processResponse(exchange,destinationInfo);
		}
	}
}
