package cc.before30.mygrpc.client.autoconfigure;

import cc.before30.mygrpc.client.GrpcChannelBuilder;
import io.grpc.LoadBalancer;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User: before30 
 * Date: 2017. 8. 21.
 * Time: PM 8:30
 */
@Configuration
@AutoConfigureOrder
public class GrpcClientAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public GrpcChannelBuilder grpcChannelBuilder() {
		return new GrpcChannelBuilder();
	}

	@ConditionalOnMissingBean
	@Bean
	public LoadBalancer.Factory grpcLoadBalancerFactory() {
		return RoundRobinLoadBalancerFactory.getInstance();
	}
}
