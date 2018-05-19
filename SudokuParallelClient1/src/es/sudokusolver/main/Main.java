package es.sudokusolver.main;

import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;

public class Main {

	public static void main(String[] args) {
		
		HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance();
        Set<HazelcastInstance> instances = HazelcastInstanceFactory.getAllHazelcastInstances();
        
	}

}
