/**
 *
 */
package be.i8c.carbon.apimgt.gateway.handlers.throttling;

import java.util.Iterator;
import java.util.Map;

import javax.cache.Cache;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import com.hazelcast.cluster.Endpoint;

import org.apache.synapse.core.axis2.Axis2MessageContext;

/**
 * @author Tom Dierckx
 *
 */
public class APIThrottleHandler extends AbstractHandler {
	// configurable variables like amounts of requests and timewindow for
	// thottling
	// TODO add to parameters added when adding the handler to xml
	private int amountofRequests = 1;
	private Long timeWindow = new Long(60000);

	private static final Log log = LogFactory.getLog(APIThrottleHandler.class);
	// Class variables
	private boolean isClusteringEnabled = false;
	Map<String, EmailBasedCallerContext> localData = null;
	Cache<String, EmailBasedCallerContext> cacheData;
	ThrottleDataHandler accessControl;
	private final String JsonObject = "jsonObject";
	private final String SCHOOLEMAIL = "schoolEmail";
	private final String STUDENT = "student";

	/**
	 * The property key that used when the ConcurrentAccessController look up
	 * from ConfigurationContext
	 */
	public APIThrottleHandler() {
		this.accessControl = new ThrottleDataHandler(localData, timeWindow);
	}

	public boolean handleRequest(MessageContext mc) {
		return handleThrottle(mc);
	}

	private boolean handleThrottle(MessageContext mc) {
		boolean allowed = false;

		// only incoming messages are throttled
		// Getting the AXIScontext and the configuration context for checking
		// cluster

		// Checking if cluster is enabled if enabled check if cache is already
		// created if not recreate
		org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) mc).getAxis2MessageContext();
		ConfigurationContext configurationContext = axis2MC.getConfigurationContext();
		ClusteringAgent clusteringAgent = configurationContext.getAxisConfiguration().getClusteringAgent();
		if (clusteringAgent != null) {
			if (log.isDebugEnabled()) {
			log.debug("clustering is enabled");}
			isClusteringEnabled = true;
		} else {
			if (log.isDebugEnabled()) {
			log.debug("clustering is disabled");}
		}

		// FirstExtract the possible email from message
		String email = mailExtract(mc);
		if (log.isDebugEnabled()) {
		log.debug("Following mail in message: "+email);}
		// check if mail string is present
		if (email == null || email == "") {
			if (log.isDebugEnabled()) {
			log.debug("no email in message found");}
			allowed = false;
		} else {
			// Calling right way to access data
			if (isClusteringEnabled) {
				allowed = accessControl.clusterThrottler(email, amountofRequests);
			} else {
				allowed = accessControl.localThrottler(email, amountofRequests);
			}
		}
		if (!allowed) {
			handleThrottleOut(mc);
		}
		if (log.isDebugEnabled()) {
			log.debug("Is the message allowed :" + allowed);
		}
		return allowed;
	}

	// Extract and return the email adress from the message
	private String mailExtract(MessageContext mc) {
		String schoolEmail = new String();
		SOAPBody soapBody = mc.getEnvelope().getBody();
		OMElement omElement = null;
		Iterator<OMElement> i = soapBody.getChildElements();
		while (i.hasNext()) {
			omElement = i.next();
			if (omElement.getLocalName().equals(JsonObject)) {
				//New synaps message builder encapsulates in JSON tags
				Iterator<OMElement> k = omElement.getChildElements();
				OMElement studentOMElement = null;
				while (k.hasNext()) {
					studentOMElement = k.next();
					if (studentOMElement.getLocalName().equals(STUDENT)) {
						Iterator<OMElement> j = studentOMElement.getChildElements();
						OMElement childOMElement = null;
						while (j.hasNext()) {
							childOMElement = j.next();
							if (childOMElement.getLocalName().equals(SCHOOLEMAIL)) {
								schoolEmail = childOMElement.getText();
								if (schoolEmail != null && !schoolEmail.equals("")) {
									schoolEmail = schoolEmail.toLowerCase();
									break;
								}
							}
						}
					}
				}
			} else {
				//No JSONObject old method for extracting the mail
				if(omElement.getLocalName().equals(STUDENT)){
					Iterator<OMElement> j = omElement.getChildElements();
					OMElement childOMElement = null;
					while(j.hasNext()){
						childOMElement = j.next();
						if (childOMElement.getLocalName().equals(SCHOOLEMAIL)){
							schoolEmail = childOMElement.getText();
							if (schoolEmail != null && !schoolEmail.equals("")){
								schoolEmail = schoolEmail.toLowerCase();
								break;
							}
						}
					}
				}
			}
		}
		return schoolEmail;
	}

	/*
	 * No need to throttle the response.
	 *
	 * (non-Javadoc)
	 *
	 * @see org.apache.synapse.rest.Handler#handleResponse(org.apache.synapse.
	 * MessageContext)
	 */
	public boolean handleResponse(MessageContext mc) {
		return true;
	}

	/*
	 * Copied from APIThrottleHandler in API manager v1.8.0
	 */
	private void handleThrottleOut(MessageContext messageContext) {
		// TODO: Hardcoded const should be moved to a common place which is
		// visible to org.wso2.carbon.apimgt.gateway.handlers
		String applicationName = (String) messageContext.getProperty("APPLICATION_NAME");

		String apiURL = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
		if (log.isDebugEnabled()) {
		}
		// log.debug("Message throttled out for application-name:" +
		// applicationName + ", api-url:" + apiURL + ".");
		messageContext.setProperty(SynapseConstants.ERROR_CODE, 900800);
		messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, "Message throttled out");

		Mediator sequence = messageContext.getSequence(APIThrottleConstants.API_THROTTLE_OUT_HANDLER);
		// Invoke the custom error handler specified by the user
		if (sequence != null && !sequence.mediate(messageContext)) {
			// If needed user should be able to prevent the rest of the fault
			// handling
			// logic from getting executed
			return;
		}

		// By default we send a 503 response back
		if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
			Utils.setFaultPayload(messageContext, getFaultPayload());
		} else {
			Utils.setSOAPFault(messageContext, "Server", "Message Throttled Out", "You have exceeded your quota");
		}
		org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
				.getAxis2MessageContext();

		/* For CORS support adding required headers to the fault response */
		Map<String, String> headers = (Map) axis2MC
				.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
		headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS,
				APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS_VALUE);
		headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
				APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS_VALUE);
		axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
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
