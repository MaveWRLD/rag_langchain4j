package org.mave.rag_langchain4j.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class ServiceLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLoggingAspect.class);

//    @Pointcut("execution(* org.mave.rag_langchain4j.services..*(..))")
//    public void serviceMethods() {
//    }
//
//    @Around("serviceMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();

        logger.info("Entering {}", methodName);

        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            logger.info("Exiting {} ({} ms)", methodName, elapsed);
            return result;
        } catch (Throwable t) {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            logger.error("Error in {} after {} ms", methodName, elapsed, t);
            throw t;
        }
    }
}
