package cc.before30.mygrpc.client;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * User: before30
 * Date: 2017. 8. 7.
 * Time: PM 1:36
 */
@Slf4j
public class PropertyChannelResolverProvider extends NameResolverProvider {

	public static final String SCHEME = "property";

	private final GrpcSource properties;

	public PropertyChannelResolverProvider(GrpcSource properties) {
		this.properties = properties;
	}

	@Nullable @Override public NameResolver newNameResolver(URI targetUri, Attributes params) {
		log.info(targetUri.toString());
		return new PropertyChannelResolver(targetUri.toString(), properties, params, GrpcUtil.SHARED_CHANNEL_EXECUTOR);
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
