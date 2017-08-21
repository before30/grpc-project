package cc.before30.mygrpc.client;

import com.netflix.discovery.EurekaClient;
import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * User: before30 
 * Date: 2017. 8. 16.
 * Time: PM 3:26
 */
@Slf4j
public class DiscoveryChannelResolverProvider extends NameResolverProvider {

	public static final String SCHEME = "discovery";

	private final EurekaClient client;

	public DiscoveryChannelResolverProvider(EurekaClient client) {
		this.client = client;
	}

	@Nullable @Override public NameResolver newNameResolver(URI targetUri, Attributes params) {
		log.info(targetUri.toString());
		return new DiscoveryChannelResolver(targetUri.getAuthority(), client, params, GrpcUtil.TIMER_SERVICE, GrpcUtil.SHARED_CHANNEL_EXECUTOR);
	}

	@Override public String getDefaultScheme() {
		return SCHEME;
	}

	@Override protected boolean isAvailable() {
		return true;
	}

	@Override protected int priority() {
		return 5;
	}
}
