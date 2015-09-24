/**
 * 
 */
package be.i8c.carbon.apimgt.gateway.handlers.throttling;



import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;



/**
 * @author Kristof Lievens
 *
 */
public class APIThrottleHandler extends AbstractHandler {
	
	private static final Log log = LogFactory.getLog(APIThrottleHandler.class);
	
	private final String SCHOOLEMAIL = "schoolEmail";
	private final String STUDENT = "student";
	// We just run one API Manager instance for the student contest, so in memory map is sufficient.
	// If multiple instances would be required, this should be replaced with Hazelcast clustering.
	private static final Map<String,Long> emailMap = new ConcurrentHashMap<String,Long>();


	/* 
	 * For this method to work:
	 * - make sure the AXIS2 transport protocol != PassThru
	 * - the message Message Builder for content type "application/json" should be org.apache.axis2.json.JSONBuilder
	 * Check axis2.xml contains the correct config.
	 * 
	 * 
	 * @see org.apache.synapse.rest.Handler#handleRequest(org.apache.synapse.MessageContext)
	 */
	public boolean handleRequest(MessageContext mc) {
		// Don't handle by default
		// Should only be set to true is requester identified by school email address in request body
		// hasn't submitted a request in the past 60 seconds.
		boolean handle = false;
		SOAPBody soapBody = mc.getEnvelope().getBody();
		
		OMElement omElement = null;
		
		Iterator<OMElement> i = soapBody.getChildElements();
		while (i.hasNext()){
			omElement = i.next();
			if(omElement.getLocalName().equals(STUDENT)){
				Iterator<OMElement> j = omElement.getChildElements();
				OMElement childOMElement = null;
				while(j.hasNext()){
					childOMElement = j.next();
					if (childOMElement.getLocalName().equals(SCHOOLEMAIL)){
						String schoolEmail = childOMElement.getText();
						if (schoolEmail != null && !schoolEmail.equals("")){
							schoolEmail = schoolEmail.toLowerCase();
							handle = !emailThrottled(schoolEmail);
							break;
						}
					}
				}
			}
		}
		
		if(!handle){
			handleThrottleOut(mc);
		}
		return handle;
	}

	private boolean emailThrottled(String email){
		// Throttle by default
		boolean throttle = true;
		Long lastAccessTime = emailMap.get(email);
		Long currentTime = System.currentTimeMillis();
		if (lastAccessTime == null) {
			if (log.isDebugEnabled())
				log.debug("No entry in map for email address "+email+", adding address to map with access time "+currentTime+". No throttling applied");
			emailMap.put(email, currentTime);
			throttle = false;
		} else {
			// if last accessed more than one minute ago, don't throttle
			if(lastAccessTime+60000 < currentTime){
				if(log.isDebugEnabled())
					log.debug("Last access at "+lastAccessTime+" for email address "+email+", updating access time to "+currentTime+". No throttling applied");
				emailMap.put(email, currentTime);
				throttle = false;
			} else {
				if(log.isDebugEnabled())
					log.debug("Last access less than one minute ago at "+lastAccessTime+" for email address "+email+". Throttling applied");
			}
		}
		return throttle;
	}
	
	
	/* 
	 * No need to throttle the response.
	 * 
	 * (non-Javadoc)
	 * @see org.apache.synapse.rest.Handler#handleResponse(org.apache.synapse.MessageContext)
	 */
	public boolean handleResponse(MessageContext mc) {
		return true;
	}

	
	/*
	 * Copied from APIThrottleHandler in API manager v1.8.0
	 */
    private void handleThrottleOut(MessageContext messageContext) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, 900800);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, "Message throttled out");

        Mediator sequence = messageContext.getSequence(APIThrottleConstants.API_THROTTLE_OUT_HANDLER);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }

        // By default we send a 503 response back
        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload());
        } else {
            Utils.setSOAPFault(messageContext, "Server", "Message Throttled Out",
                               "You have exceeded your quota");
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        if (Utils.isCORSEnabled()) {
            /* For CORS support adding required headers to the fault response */
            Map<String, String> headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Utils.getAllowedOrigin((String) headers.get("Origin")));
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils.getAllowedMethods());
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils.getAllowedHeaders());
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }
        Utils.sendFault(messageContext, HttpStatus.SC_SERVICE_UNAVAILABLE);
    }
	
	/*
	 * Copied from APIThrottleHandler in API manager v1.8.0
	 */
    private OMElement getFaultPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APIThrottleConstants.API_THROTTLE_NS,
                                               APIThrottleConstants.API_THROTTLE_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(APIThrottleConstants.THROTTLE_OUT_ERROR_CODE));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText("Message Throttled Out");
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText("You have exceeded your quota");

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }
	
}
