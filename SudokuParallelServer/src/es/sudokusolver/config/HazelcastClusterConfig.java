package es.sudokusolver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

import es.sudokusolver.bean.BWrapper;
import es.sudokusolver.bean.NodeWrapper;
import es.sudokusolver.engine.DistTask;

import es.sudokusolver.engine.*;

@Configuration
public class HazelcastClusterConfig {
	
	private final Logger logger = LoggerFactory.getLogger(HazelcastClusterConfig.class);
	private DistTask task;
	
	IQueue<NodeWrapper> queue_i;
	IQueue<NodeWrapper> queue_o;
	
	
	
	/**
	 * HazelcastCluster Instance Definition.
	 * Creation of Distributed Task along the nodes, 
	 * @return
	 */
	

	@Bean
	public HazelcastInstance processHazelcastInstance(){
		
		logger.info("[HazelcastClusterConfig] hazelcastCluster INI");
		
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
		
	
		queue_i = hazelcastInstance.getQueue(Constants.SUDOKU_QUEUE_1);
		queue_o = hazelcastInstance.getQueue(Constants.SUDOKU_QUEUE_2);
		
		task = new DistTask();
		task.setHazelcastInstance(hazelcastInstance);
		
		return hazelcastInstance;
	}
	
	
	@Bean
	public DistTask getDistTask(){
		return task;
	}
	
}
