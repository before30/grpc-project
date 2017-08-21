package cc.before30.mygrpc.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.internal.LogExceptionRunnable;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * User: before30 
 * Date: 2017. 8. 16.
 * Time: PM 3:25
 */
@Slf4j
public class DiscoveryChannelResolver extends NameResolver {

	private final String authority;

	private final Attributes attributes;

	private final SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource;

	private final SharedResourceHolder.Resource<ExecutorService> executorResource;

	private final EurekaClient client;

	@GuardedBy("this")
	private boolean shutdown;

	@GuardedBy("this")
	private ScheduledExecutorService timerService;

	@GuardedBy("this")
	private ExecutorService executor;

	@GuardedBy("this")
	private ScheduledFuture<?> resolutionTask;

	@GuardedBy("this")
	private boolean resolving;

	@GuardedBy("this")
	private Listener listener;

	@GuardedBy("this")
	private List<InstanceInfo> serviceInstances;

	public DiscoveryChannelResolver(String authority, EurekaClient client, Attributes attributes,
		SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource,
		SharedResourceHolder.Resource<ExecutorService> executorResource) {

		this.authority = authority;
		this.client = client;
		this.attributes = attributes;
		this.timerServiceResource = timerServiceResource;
		this.executorResource = executorResource;
		this.serviceInstances = Lists.newArrayList();
	}

	@Override public String getServiceAuthority() {
		return authority;
	}

	@Override public void start(Listener listener) {
		Preconditions.checkState(this.listener == null, "already started");
		timerService = SharedResourceHolder.get(timerServiceResource);
		this.listener = listener;
		executor = SharedResourceHolder.get(executorResource);
		this.listener = Preconditions.checkNotNull(listener, "listener");
		resolve();
		timerService.scheduleWithFixedDelay(new LogExceptionRunnable(resolutionRunnableOnExecutor), 1, 1, TimeUnit.MINUTES);
	}

	@Override public final synchronized void refresh() {
		Preconditions.checkState(listener != null, "not started");
		resolve();
	}

	@Override public void shutdown() {
		if (shutdown) {
			return;
		}

		shutdown = true;
		if (resolutionTask != null) {
			resolutionTask.cancel(false);
		}

		if (timerService != null) {
			timerService = SharedResourceHolder.release(timerServiceResource, timerService);
		}

		if (executor != null) {
			executor = SharedResourceHolder.release(executorResource, executor);
		}
	}

	@GuardedBy("this")
	private void resolve() {

		if (resolving || shutdown) {
			return;
		}

		executor.execute(resolutionRunnable);
	}

	private final Runnable resolutionRunnableOnExecutor = new Runnable() {

		@Override public void run() {
			synchronized (DiscoveryChannelResolver.this) {
				if (!shutdown) {
					executor.execute(resolutionRunnable);
				}
			}
		}
	};

	private final Runnable resolutionRunnable = new Runnable() {

		@Override public void run() {
			Listener savedListener;
			synchronized (DiscoveryChannelResolver.this) {

				if (resolutionTask != null) {
					resolutionTask.cancel(false);
					resolutionTask = null;
				}
				if (shutdown) {
					return;
				}
				savedListener = listener;
				resolving = true;

				try {
					List<InstanceInfo> newServiceInstances;
					try {

						newServiceInstances = client.getApplication(authority).getInstances();
					} catch (Exception ex) {
						savedListener.onError(Status.UNAVAILABLE.withCause(ex));
						return;
					}

					if (CollectionUtils.isNotEmpty(newServiceInstances)) {
						if (isNeedToUpdateServiceInstanceList(newServiceInstances)) {
							serviceInstances = newServiceInstances;
						} else {
							return;
						}

						List<EquivalentAddressGroup> equivalentAddressGroups = Lists.newArrayList();

						for (InstanceInfo instance : serviceInstances) {
							Map<String, String> metadata = instance.getMetadata();
							if (Objects.nonNull(metadata.get("grpc.port"))) {
								Integer port = Integer.valueOf(metadata.get("grpc.port"));
								String host = instance.getHostName();

								log.info("Found new gRPC server {} {}:{}", authority, host, port);
								EquivalentAddressGroup addressGroup = new EquivalentAddressGroup(new InetSocketAddress(host, port));
								equivalentAddressGroups.add(addressGroup);
							} else {
								log.info ("Can't find gRPC metadata {} {}", authority, instance.getHostName());
							}
						}
						savedListener.onAddresses(equivalentAddressGroups, Attributes.EMPTY);
					} else {
						savedListener.onError(Status.UNAVAILABLE.withCause(
							new RuntimeException("UNAVAILABLE : Eureka returned an empty list " + authority)
						));
					}
				} finally {
					synchronized (DiscoveryChannelResolver.this) {
						resolving = false;
					}
				}
			}
		}
	};

	private boolean isNeedToUpdateServiceInstanceList(List<InstanceInfo> newServiceInstanceList) {
		if (serviceInstances.size() == newServiceInstanceList.size()) {
			for (InstanceInfo serviceInstance : serviceInstances) {
				boolean isSame = false;
				for (InstanceInfo newServiceInstance : newServiceInstanceList) {
					if (newServiceInstance.getHostName().equals(serviceInstance.getHostName()) && newServiceInstance.getPort() == serviceInstance.getPort()) {
						isSame = true;
						break;
					}
				}
				if (!isSame) {
					log.info("Ready to update {} server info group list", authority);
					return true;
				}
			}
		} else {
			log.info("Ready to update {} server info group list", authority);
			return true;
		}
		return false;
	}
}
