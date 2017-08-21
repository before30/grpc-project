package cc.before30.mygrpc.server.autoconfigure;

import cc.before30.mygrpc.server.GrpcServerBuilderConfigurer;
import cc.before30.mygrpc.server.GrpcServerRunner;
import cc.before30.mygrpc.server.GrpcService;
import io.grpc.services.HealthStatusManager;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * User: before30 
 * Date: 2017. 8. 21.
 * Time: PM 8:25
 */
@Configuration
@AutoConfigureOrder
public class GrpcServerAutoConfiguration {
	@Bean
	@ConditionalOnBean(annotation = GrpcService.class)
	public GrpcServerRunner grpcServerRunner(GrpcServerBuilderConfigurer configure, HealthStatusManager healthStatusManager, AbstractApplicationContext context) {
		return new GrpcServerRunner(configure, healthStatusManager, context);
	}

	@Bean
	@ConditionalOnBean(annotation = GrpcService.class)
	public HealthStatusManager healthStatusManager() {
		return new HealthStatusManager();
	}

	@Bean
	@ConditionalOnMissingBean(GrpcServerBuilderConfigurer.class)
	public GrpcServerBuilderConfigurer serverBuilderConfigurer() {
		return new GrpcServerBuilderConfigurer();
	}
}
