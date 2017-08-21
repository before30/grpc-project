package cc.before30.mygrpc.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * User: before30 
 * Date: 2017. 8. 21.
 * Time: PM 2:24
 */

@Target({ ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface GrpcGlobalServerInterceptor {
}
