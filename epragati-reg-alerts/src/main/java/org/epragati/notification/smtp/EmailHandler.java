package org.epragati.notification.smtp;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultCamelContext;
import org.epragati.AbstractHandler;
import org.epragati.notification.DestinationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailHandler extends AbstractHandler implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(EmailHandler.class); 

	@Override
	public void process(Exchange exchange) throws Exception {

		CamelContext context = new DefaultCamelContext();
		DestinationInfo destinationInfo = exchange.getIn().getBody(DestinationInfo.class);

		logger.info("SMTP Destination Info : [{}]",destinationInfo);

		String uri = buildURI(destinationInfo);
		setBodyAndHeaders(exchange, destinationInfo);

		logger.debug("URI : [{}]",uri);
		Endpoint endpoint = context.getEndpoint(uri);

		Producer producer = endpoint.createProducer();
		// start the producer
		producer.start();
		// and let it go (processes the exchange by sending the email)
		producer.process(exchange);

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
