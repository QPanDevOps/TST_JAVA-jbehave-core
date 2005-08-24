package com.thoughtworks.jbehave.extensions.harness;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.jbehave.extensions.harness.time.Timeouter;
import com.thoughtworks.jbehave.extensions.harness.time.TimeouterFactory;

/**
 * An asynchronous holder implementing some map-like features. If an object is 
 * retrieved using a key and no object is present, this set will wait for a 
 * matching object.
 */
public class QueuedMiniHashMap implements QueuedMiniMap {
        
    private Map map = Collections.synchronizedMap(new HashMap());
    private Object waitingPlace = new Object();
    private TimeouterFactory timeouterFactory;
    
    public QueuedMiniHashMap(TimeouterFactory timeouterFactory) {
        this.timeouterFactory = timeouterFactory;
    }
    
    public void put(Object key, Object value) {
        map.put(key, value);
		synchronized(waitingPlace) {
			waitingPlace.notifyAll();
		}
    }
    
    public Object get(Object key, long timeout) {
        Timeouter timeouter = timeouterFactory.createTimeouter();
        timeouter.start(timeout);
        Object value = map.get(key);
        synchronized(waitingPlace) {    
			while (value == null) {
			    timeouter.checkTime();
			    try {
			        waitingPlace.wait(timeouter.getTimeLeftIfAny());
			    } catch (InterruptedException ie) {}
				value = map.get(key);
			}
		}
        return value;
    }

    public void remove(Object key) {
        map.remove(key);
    }    
}
