package cc.before30.mygrpc.server;

import io.grpc.*;
import io.grpc.services.HealthStatusManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User: before30 
 * Date: 2017. 8. 21.
 * Time: PM 2:15
 */

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GrpcServerRunner implements CommandLineRunner, DisposableBean {

	private final Set<String> services = new ConcurrentSkipListSet<>();

	private final HealthStatusManager healthStatusManager;

	private final AbstractApplicationContext applicationContext;

	private final GrpcServerBuilderConfigurer configurer;

	private Server server = null;

	public GrpcServerRunner(GrpcServerBuilderConfigurer configurer, HealthStatusManager healthStatusManager, AbstractApplicationContext applicationContext) {
		this.configurer = configurer;
		this.healthStatusManager = healthStatusManager;
		this.applicationContext = applicationContext;
	}

	@Override public void run(String... args) throws Exception {
		log.info("*** Starting gRPC server ***");

		Collection<ServerInterceptor> globalInterceptors = getBeanNamesByTypeWithAnnotation(GrpcGlobalServerInterceptor.class, ServerInterceptor.class)
			.map(name -> applicationContext.getBeanFactory().getBean(name, ServerInterceptor.class))
			.collect(Collectors.toList());

		Collection<BindableService> grpcServices = getBeanNamesByTypeWithAnnotation(GrpcService.class, BindableService.class)
			.map(name -> applicationContext.getBeanFactory().getBean(name, BindableService.class))
			.collect(Collectors.toList());

		final ServerBuilder<?> serverBuilder = ServerBuilder.forPort(8888);
		grpcServices.forEach(service -> {
			serverBuilder.addService(service);
		});

		/*
				getBeanNamesByTypeWithAnnotation(GrpcService.class, BindableService.class)
			.forEach(name -> {
				BindableService srv = applicationContext.getBeanFactory().getBean(name, BindableService.class);
				ServerServiceDefinition serviceDefinition = srv.bindService();
				GrpcService gRpcServiceAnn = applicationContext.findAnnotationOnBean(name, GrpcService.class);
				serviceDefinition = bindInterceptors(serviceDefinition, gRpcServiceAnn, globalInterceptors);
				serverBuilder.addService(serviceDefinition);
				String serviceName = serviceDefinition.getServiceDescriptor().getName();
				healthStatusManager.setStatus(serviceName, HealthCheckResponse.ServingStatus.SERVING);
				serviceList.add(serviceName);
				log.info("'{}' service has been registered.", srv.getClass().getName());
			});
		 */
		serverBuilder.addService(healthStatusManager.getHealthService());
		configurer.configure(serverBuilder);
		server = serverBuilder.build().start();

		log.info("gRPC server started, listening on port{}.", 8888);
		startDaemonAwaitThread();
	}

	@Override public void destroy() throws Exception {
		log.info("** Shutting down gRPC server **");
		services.forEach(s -> healthStatusManager.clearStatus(s));
		Optional.ofNullable(server).ifPresent(s -> s.shutdown());
		log.info("** gRPC server stopped **");
	}

	private void startDaemonAwaitThread() {
		Thread awaitThread = new Thread(() -> {
			try {
				GrpcServerRunner.this.server.awaitTermination();
			} catch (InterruptedException e) {
				log.error("gRPC server stopped abnormally.", e);
			}
			;
		});

		awaitThread.setDaemon(false);
		awaitThread.start();
	}


	private <T> Stream<String> getBeanNamesByTypeWithAnnotation(Class<?extends Annotation> annotationType, Class<T> beanType) throws Exception {

		return Stream.of(applicationContext.getBeanNamesForType(beanType))
			.filter(name -> {
				final BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
				final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotationType);

				if (!beansWithAnnotation.isEmpty()) {
					return beansWithAnnotation.containsKey(name);
				} else if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
					StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
					return metadata.isAnnotated(annotationType.getName());
				}
				return false;
			});
	}
}
