package be.i8c.carbon.apimgt.gateway.handlers.throttling;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EmailBasedCallerContext implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8347831266062178258L;
	// log
	private static Log log = LogFactory.getLog(EmailBasedCallerContext.class.getName());
	// Previous time there was an access
	private Long previousTime = 0L;
	// amount of requests
	private int count = 0;
	// Max allowed calls
	private int maxcalls = 0;
	// Time
	private Long timeWindow;
	// ID of caller => email adress in this case
	private String ID;

	public EmailBasedCallerContext(String id, int max) {
		ID = id;
		maxcalls = max;
	}

	public void create(Long currentTime, Long Timewindow) {
		// When email not found in map just create function
		timeWindow = Timewindow;
		previousTime = currentTime;

	}

	public boolean canAccess(Long currentTime, Long Timewindow) {
		boolean allow = false;
		// FirstTime
		if (previousTime == 0L && count == 0) {
			log.debug("Initializing first time");
			// Adding new base time to this class
			timeWindow = Timewindow;
			count++;
			previousTime = currentTime;
			allow = true;
		} else {
			// Check if message may proceed according to the Time window
			boolean inTime = timeRestricted(currentTime);
			if (log.isDebugEnabled()) {
			log.debug("amount of calls: " + count + ". maxAllowed time: " + maxcalls);}
			if (inTime) {
				// outside time window reset count and allow call
				count = 0;
				count++;
				previousTime = currentTime;
				allow = true;
			} else if (!inTime && (count < maxcalls)) {
				// inside timewindow but not reached max amount of calls
				// original time will be kept
				count++;
				allow = true;
			} else if (count >= maxcalls) {
				allow = false;
			}
		}
		return allow;
	}

	private boolean timeRestricted(Long currentTime) {
		if ((previousTime + timeWindow) < currentTime) {
			// Outside time window
			return true;
		}
		return false;
	}

	public String getID() {
		return ID;
	}

	public Long getPreviousTime() {
		return previousTime;
	}
}
