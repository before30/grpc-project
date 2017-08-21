package cc.before30.mygrpc.server;

import io.grpc.ServerInterceptor;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * User: before30 
 * Date: 2017. 8. 21.
 * Time: PM 2:11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface GrpcService {
	Class<? extends ServerInterceptor>[] interceptors() default {};
	boolean applyGlobalInterceptors() default true;
}
