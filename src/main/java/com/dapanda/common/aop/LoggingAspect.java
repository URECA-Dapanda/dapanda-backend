package com.dapanda.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@Aspect
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
public class LoggingAspect {

	@Around("execution(* com.dapanda..controller..*.*(..))")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

		String methodName = joinPoint.getSignature().toShortString();
		log.info("[REQUEST] {}", methodName);

		Object result;
		try {
			result = joinPoint.proceed(); // 실제 메서드 실행
		} catch (Throwable throwable) {
			log.error("[EXCEPTION] {}: {}", methodName, throwable.getMessage());
			throw throwable;
		}

		// 반환값이 너무 클 경우를 대비해 toString() 길이 제한
		String responseStr = String.valueOf(result);
		if (responseStr.length() > 500) {
			responseStr = responseStr.substring(0, 500) + "...(truncated)";
		}

		log.info("[RESPONSE] {} → {}", methodName, responseStr);

		return result;
	}
}
