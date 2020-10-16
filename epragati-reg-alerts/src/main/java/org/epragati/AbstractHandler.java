package org.epragati;


import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.mail.SendFailedException;
import javax.mail.util.ByteArrayDataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import org.epragati.notification.DestinationInfo;
import org.epragati.notification.exception.EmailChannelException;
import org.epragati.notification.exception.ProtocolNotFoundException;
import org.epragati.notification.msg.Msg;
import org.epragati.notification.response.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPSenderFailedException;

public abstract class AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

	protected String buildURI(DestinationInfo destinationInfo) throws Exception{

		StringBuilder uri = new StringBuilder(64);

		if(StringUtils.isBlank(destinationInfo.getProtocol())){
			throw new ProtocolNotFoundException(Msg.PROTOCOL_MISSING);
		}

		uri.append(destinationInfo.getProtocol());
		uri.append("://");
		uri.append(destinationInfo.getHost());

		//If port is not available It will be consider as default
		if(StringUtils.isNotBlank(destinationInfo.getPort())){
			uri.append(":").append(destinationInfo.getPort());
		}

		String uriParameters = buildUriParameters(destinationInfo);
		uri.append(uriParameters);

		return uri.toString();
	}

	protected String buildUriParameters(DestinationInfo info) throws Exception {

		StringBuilder parameterUri = new StringBuilder(64);

		parameterUri.append("?");
		for (Entry<String, String> parameter : info.getParameters().entrySet()) {
			parameterUri.append(parameter.getKey()).append("=").append(parameter.getValue());
			parameterUri.append("&");
		}

		parameterUri.setLength(parameterUri.length() - 1); 
		return parameterUri.toString();
	}

	protected void setBodyAndHeaders(Exchange exchange, DestinationInfo destinationInfo) throws IOException {

		Message in = exchange.getIn();
		in.removeHeaders("JMS.*");
		in.removeHeader("CamelJmsDeliveryMode");

		in.setBody(destinationInfo.getEmailBody());
		byte[] document;

		if (destinationInfo.isSendAttachment()) {
			document = destinationInfo.getAttachment();
			in.addAttachment(destinationInfo.getFileName(), new DataHandler(new ByteArrayDataSource(document, "application/octet-stream"))); //or application/octetstream

		}
		
		// TODO: Make it Dynamic
		in.setHeader(Exchange.HTTP_METHOD, "POST");
		in.setHeader(Exchange.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);   
		

		if(destinationInfo.getHeaders() != null){
			Map<String, String> headers = destinationInfo.getHeaders();
			if (headers != null) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					in.setHeader(header.getKey(), header.getValue());
				}
			}
		}
	}

	public void handleException(Exchange exchange,DestinationInfo destinationInfo){

		String newMsg = null;
		if (exchange.getException() != null) 
		{
			Exception exchangeException = exchange.getException();
			logger.error("Error [{}] ",exchangeException.getMessage());
			newMsg=exchangeException.getLocalizedMessage();

			EmailChannelException emailChannelException = null;
			if (exchangeException instanceof SendFailedException) 
			{
				Exception nextException = ((SendFailedException)exchangeException).getNextException();
				if (nextException instanceof SMTPAddressFailedException) 
				{
					newMsg = ((SMTPAddressFailedException)nextException).getLocalizedMessage();
					emailChannelException = new EmailChannelException(newMsg, exchangeException);
					logger.debug("emailChannelException  [{}] ",emailChannelException.getMessage());

				} else if (nextException instanceof SMTPSendFailedException) 
				{
					newMsg = ((SMTPSendFailedException)nextException).getLocalizedMessage();
					emailChannelException = new EmailChannelException(newMsg, exchangeException);
					logger.debug("emailChannelException  [{}] ",emailChannelException.getMessage());
				}  else if (nextException instanceof SMTPSenderFailedException) 
				{
					newMsg = ((SMTPSenderFailedException)nextException).getLocalizedMessage();
					emailChannelException = new EmailChannelException(newMsg, exchangeException);
					logger.debug("emailChannelException  [{}] ",emailChannelException.getMessage());
				}
			}

			logger.info("Processing Notification Response ");
			NotificationResponse notificationResponse=new NotificationResponse();
			notificationResponse.setMessage(newMsg);
			notificationResponse.setStatus('F');
			if(destinationInfo!=null){
				notificationResponse.setProtocol(destinationInfo.getProtocol());
				notificationResponse.setTo(destinationInfo.getParameters().get("to"));
				notificationResponse.setTransactionId(destinationInfo.getTransactionId());
				notificationResponse.setMessageType(destinationInfo.getMessageType());
				notificationResponse.setServiceId(destinationInfo.getServiceId());
			}
			exchange.getOut().setBody(notificationResponse);
			//exchange.getOut().setHeaders(exchange.getIn().getHeaders());
			logger.info("Notification Response is  [{}]",notificationResponse);
		}
	}

	protected void processResponse(Exchange exchange,DestinationInfo destinationInfo){
		logger.info("Processing Notification Response ");
		NotificationResponse notificationResponse = new NotificationResponse();
		notificationResponse.setStatus('S');
		if(destinationInfo!=null){
			notificationResponse.setProtocol(destinationInfo.getProtocol());
			notificationResponse.setMessageType(destinationInfo.getMessageType());
			notificationResponse.setServiceId(destinationInfo.getServiceId());
			if(destinationInfo.getProtocol().equalsIgnoreCase("http")){
				notificationResponse.setTo(destinationInfo.getParameters().get("mobileno"));
			}else{
				notificationResponse.setTo(destinationInfo.getParameters().get("to"));
			}
			notificationResponse.setTransactionId(destinationInfo.getTransactionId());
		}
		exchange.getOut().setBody(notificationResponse);
		//exchange.getOut().setHeaders(exchange.getIn().getHeaders());
		logger.info("Notification Response is  [{}]",notificationResponse);

	}

}