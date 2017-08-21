package cc.before30.mygrpc.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * User: before30 
 * Date: 2017. 8. 7.
 * Time: PM 12:43
 */
@Slf4j
public class PropertyChannelResolver extends NameResolver {

	private final String authority;

	private final GrpcSource properties;

	private final Attributes attributes;

	private final SharedResourceHolder.Resource<ExecutorService> executorResource;

	@GuardedBy("this")
	private boolean shutdown;

	@GuardedBy("this")
	private ExecutorService executor;

	@GuardedBy("this")
	private boolean resolving;

	@GuardedBy("this")
	private Listener listener;

	public PropertyChannelResolver(String authority, GrpcSource properties, Attributes attributes, SharedResourceHolder.Resource<ExecutorService> executorResource) {
		this.authority = authority;
		this.properties = properties;
		this.attributes = attributes;
		this.executorResource = executorResource;
	}


	@Override public String getServiceAuthority() {
		return authority;
	}

	@Override public void start(Listener listener) {
		Preconditions.checkState(this.listener == null, "already started");
		executor = SharedResourceHolder.get(executorResource);
		this.listener = Preconditions.checkNotNull(listener, "listener");
		
		resolve();
	}

	@Override public void refresh() {
		Preconditions.checkState(listener != null, "not started");
		resolve();
	}

	private final Runnable resolutionRunnable = new Runnable() {
		@Override public void run() {
			Listener savedListener;
			synchronized (PropertyChannelResolver.this) {
				if (shutdown) {
					return;
				}
				savedListener = listener;
				resolving = true;
			}

			try {
				List<EquivalentAddressGroup> equivalentAddressGroupList = Lists.newArrayList();

				for (final String server : properties.getServers()) {
					final int colon = server.indexOf(GrpcSource.COLON);
					if (colon == -1) {
						EquivalentAddressGroup addressGroup = new EquivalentAddressGroup(
							new InetSocketAddress(server, GrpcSource.DEFAULT_PORT));
						equivalentAddressGroupList.add(addressGroup);
					} else {
						final int port = Integer.parseInt(server.substring(colon + 1));
						final String host = server.substring(0, colon);
						EquivalentAddressGroup addressGroup = new EquivalentAddressGroup(
							new InetSocketAddress(host, port));
						equivalentAddressGroupList.add(addressGroup);
					}
				}

				savedListener.onAddresses(equivalentAddressGroupList, Attributes.EMPTY);
			} finally {
				synchronized (PropertyChannelResolver.this) {
					resolving = false;
				}
			}
		}
	};

	@GuardedBy("this")
	private void resolve() {
		if (resolving || shutdown) {
			return ;
		}
		executor.execute(resolutionRunnable);
	}

	@Override public void shutdown() {
		if (shutdown) {
			return;
		}

		shutdown = true;
		if (executor != null) {
			executor = SharedResourceHolder.release(executorResource, executor);
		}
	}
}
