package be.i8c.carbon.apimgt.gateway.handlers.throttling;

import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThrottleDataHandler {
	// Getting log file
	private static final Log log = LogFactory.getLog(ThrottleDataHandler.class);
	// Configurable timewindows
	private Long Timewindow = 0L;
	// Local map when not in cluster
	Map<String, EmailBasedCallerContext> localData;

	public ThrottleDataHandler(Map<String, EmailBasedCallerContext> localdata, Long timewindow) {
		localData = localdata;
		Timewindow = timewindow;
	}

	// Will be called when clustering is enabled
	public boolean clusterThrottler(String email, int max) {
		boolean allow = false;
		// acquiring cache manager.
		Cache<String, EmailBasedCallerContext> cacheData = EmailThrottleUtil.getThrottleCache();
		if (!cacheData.containsKey(email)) {
			// Totally new email address generate clean EmailContext
			EmailBasedCallerContext newuser = new EmailBasedCallerContext(email, max);
			allow = newuser.canAccess(System.currentTimeMillis(), Timewindow);
			if (log.isDebugEnabled()) {
			log.debug("No entry for email adress " + email + ", adding adress" + ". No throttling applied.");}
			cacheData.put(email, newuser);
		} else {
			// User is already in the cache check if he is allowed access
			EmailBasedCallerContext olduser = cacheData.get(email);
			if (log.isDebugEnabled()) {
			log.debug("Cache user previous Time" + olduser.getPreviousTime());}
			allow = olduser.canAccess(System.currentTimeMillis(), Timewindow);
			// Update cache with new data
			cacheData.put(email, olduser);
		}
		return allow;
	}

	// will be called when clustering is disabled
	public boolean localThrottler(String email, int max) {
		boolean allow = false;
		if (localData == null) {
			if (log.isDebugEnabled()) {
			log.debug("localMap is empty");}
			localData = new HashMap<String, EmailBasedCallerContext>();
		}
		if (localData.containsKey(email)) {
			// User has already tried to connect
			if (log.isDebugEnabled()) {
			log.debug("User already exist in the MAP");}
			EmailBasedCallerContext localuser = localData.get(email);
			allow = localuser.canAccess(System.currentTimeMillis(), Timewindow);
			// update the local map
			localData.put(email, localuser);
		} else {
			// user connect for first time => allow and add to the local map
			if (log.isDebugEnabled()) {
			log.debug("New user in the local throttler");}
			EmailBasedCallerContext newuser = new EmailBasedCallerContext(email, max);
			allow = newuser.canAccess(System.currentTimeMillis(), Timewindow);
			// User has been added
			localData.put(email, newuser);
		}
		return allow;
	}
}
