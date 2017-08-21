package cc.before30.mygrpc.client;

import com.google.common.base.Preconditions;
import com.netflix.discovery.EurekaClient;
import io.grpc.*;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * User: before30 
 * Date: 2017. 8. 16.
 * Time: AM 11:18
 */
public class GrpcChannelBuilder {

	public Builder grpcSource(GrpcSource grpcSource) {
		return new Builder(grpcSource);
	}

	public final class Builder {

		private GrpcSource grpcSource;

		private List<ClientInterceptor> interceptors = new ArrayList<>();

		private LoadBalancer.Factory loadBalancerFactory = RoundRobinLoadBalancerFactory.getInstance();

		private EurekaClient eurekaClient;

		public Builder(GrpcSource grpcSource) {
			this.grpcSource = grpcSource;
		}

		public Builder interceptors(List<ClientInterceptor> interceptors) {
			if (Objects.nonNull(interceptors) && CollectionUtils.isNotEmpty(interceptors)) {
				interceptors.addAll(interceptors);
			}
			return this;
		}

		public Builder interceptor(ClientInterceptor interceptor) {
			if (Objects.nonNull(interceptor)) {
				interceptors.add(interceptor);
			}
			return this;
		}

		public Builder loadbalancer(LoadBalancer.Factory loadbalancerFactory) {
			if (Objects.nonNull(loadbalancerFactory)) {
				this.loadBalancerFactory = loadbalancerFactory;
			}
			return this;
		}

		public Builder eurekaClient(EurekaClient eurekaClient) {
			this.eurekaClient = eurekaClient;
			return this;
		}

		public Channel build() {
			String[] split = grpcSource.getUrl().split("://");
			String protocol = split[0];
			NameResolverProvider nameResolverProvider = null;

			switch (protocol) {
				case DiscoveryChannelResolverProvider.SCHEME:
					Preconditions.checkNotNull(eurekaClient);
					nameResolverProvider = new DiscoveryChannelResolverProvider(eurekaClient);
					break;
				case PropertyChannelResolverProvider.SCHEME:
					nameResolverProvider = new PropertyChannelResolverProvider(grpcSource);
					break;
				case "dns":
					nameResolverProvider = new DnsNameResolverProvider();
					break;
				default:
					throw new RuntimeException("Not supported protocol for grpc");
			}

			NettyChannelBuilder builder = NettyChannelBuilder.forTarget(grpcSource.getUrl())
				.loadBalancerFactory(loadBalancerFactory)
				.nameResolverFactory(nameResolverProvider)
				.usePlaintext(grpcSource.isPlaintext());

			if (grpcSource.isEnableKeepAlive()) {
				builder.keepAliveWithoutCalls(grpcSource.isKeepAliveWithoutCalls())
					.keepAliveTime(grpcSource.getKeepAliveTime(), TimeUnit.SECONDS)
					.keepAliveTimeout(grpcSource.getKeepAliveTimeout(), TimeUnit.SECONDS);
			}

			return ClientInterceptors.intercept(builder.build(), interceptors);
		}
	}
}
