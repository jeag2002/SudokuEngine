package es.sudokusolver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;



@Configuration
public class HazelcastCacheConfig {
	
	private final Logger logger = LoggerFactory.getLogger(HazelcastCacheConfig.class);
	
    @Bean
    public Config hazelCastConfig(){
    	
    	logger.info("[HazelcastCacheConfig] hazelcastCache INI");
    	
        return new Config()
                .setInstanceName("hazelcast-instance")
                .addMapConfig(
                        new MapConfig()
                                .setName("sudokus")
                                .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                                .setEvictionPolicy(EvictionPolicy.LRU)
                                .setTimeToLiveSeconds(20));
    }
	
	

}
