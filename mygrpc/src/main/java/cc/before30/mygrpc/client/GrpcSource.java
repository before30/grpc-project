package cc.before30.mygrpc.client;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * User: before30 
 * Date: 2017. 8. 16.
 * Time: AM 10:51
 */
@Getter
@Setter
public class GrpcSource {
	public static final String DEFAULT_HOST = "127.0.0.1";

	public static final String COLON = ":";

	public static final int DEFAULT_PORT = 8081;

	private List<String> servers = new ArrayList<String>();
	private String url;
	private boolean plaintext;
	private boolean enableKeepAlive;
	private boolean keepAliveWithoutCalls;
	private long keepAliveTime;
	private long keepAliveTimeout ;

	private GrpcSource(Builder builder) {
		this.servers = builder.servers;
		this.url = builder.url;
		this.plaintext = builder.plaintext;
		this.enableKeepAlive = builder.enableKeepAlive;
		this.keepAliveWithoutCalls = builder.keepAliveWithoutCalls;
		this.keepAliveTime = builder.keepAliveTime;
		this.keepAliveTimeout = builder.keepAliveTimeout;
	}

	public static Builder create() {
		return new Builder();
	}

	public static final class Builder {

		private List<String> servers = new ArrayList<String>();
		private String url;
		private boolean plaintext = true;
		private boolean enableKeepAlive = false;
		private boolean keepAliveWithoutCalls = false;
		private long keepAliveTime = 180;
		private long keepAliveTimeout = 20;

		public Builder servers(List<String> servers) {
			this.servers = servers;
			return this;
		}

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public Builder plaintext(boolean plaintext) {
			this.plaintext = plaintext;
			return this;
		}

		public Builder enableKeepAlive(boolean enableKeepAlive) {
			this.enableKeepAlive = enableKeepAlive;
			return this;
		}

		public Builder keepAliveWithoutCalls(boolean keepAliveWithoutCalls) {
			this.keepAliveWithoutCalls = keepAliveWithoutCalls;
			return this;
		}

		public Builder keepAliveTime(long keepAliveTime) {
			this.keepAliveTime = keepAliveTime;
			return this;
		}

		public Builder keepAliveTimeout(long keepAliveTimeout) {
			this.keepAliveTimeout = keepAliveTimeout;
			return this;
		}

		public GrpcSource
		build() {
			return new GrpcSource(this);
		}
	}
}
