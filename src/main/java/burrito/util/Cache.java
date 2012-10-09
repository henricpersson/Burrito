/**
 * Copyright 2011 Henric Persson (henric.persson@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package burrito.util;

import java.util.logging.Logger;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class Cache {

	private static MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();

	private static Logger log = Logger.getLogger(Cache.class.getName());

	public static void put(String key, Object value) {
		try {
			memcacheService.put(key, value);
		}
		catch (MemcacheServiceException e) {
			log.warning("Memcache put failed for key " + key + ": " + e.getMessage());
		}
	}

	public static void put(String key, Object value, int expirationInSeconds) {
		try {
			memcacheService.put(key, value, Expiration.byDeltaSeconds(expirationInSeconds));
		}
		catch (MemcacheServiceException e) {
			log.warning("Memcache put failed for key " + key + ": " + e.getMessage());
		}
	}
	
	public static Object get(String key) {
		try {
			return memcacheService.get(key);
		}
		catch (MemcacheServiceException e) {
			log.warning("Memcache get failed for key " + key + ": " + e.getMessage());
			return null;
		}
	}

	public static void delete(String key) {
		try {
			memcacheService.delete(key);
		}
		catch (MemcacheServiceException e) {
			log.warning("Memcache delete failed for key " + key + ": " + e.getMessage());
		}
	}
}
