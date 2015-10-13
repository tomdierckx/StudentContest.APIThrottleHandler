package be.i8c.carbon.apimgt.gateway.handlers.throttling;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EmailThrottleUtil {
	private static Log log = LogFactory.getLog(EmailThrottleUtil.class.getName());

	public static final String THROTTLING_CACHE_MANAGER = "emailthrottling.cache.manager";

	public static final String THROTTLING_CACHE = "emailthrottling.cache";

	public static Cache<String, EmailBasedCallerContext> getThrottleCache() {
		// acquiring cache manager.
		Cache<String, EmailBasedCallerContext> cache;
		CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(THROTTLING_CACHE_MANAGER);
		if (log.isDebugEnabled()) {
		log.debug("acquiring  cache manager");}
		if (cacheManager != null) {
			if (log.isDebugEnabled()) {
			log.debug("cachemanager exists");}
			cache = cacheManager.getCache(THROTTLING_CACHE);
		} else {
			if (log.isDebugEnabled()) {
			log.debug("cachemanager doesn't exist");}
			cache = Caching.getCacheManager().getCache(THROTTLING_CACHE);
		}
		if (log.isDebugEnabled()) {
		log.debug("created throttling cache : " + cache);}
		return cache;
	}
}
