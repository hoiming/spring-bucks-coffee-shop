package com.haiming.springbuckscoffeeshop.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformaceAspect {
    @Around("execution(* com.haiming.springbuckscoffeeshop.repositories..*(..))")
    public Object logPerformance(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        long startTime = System.currentTimeMillis();
        String name = "-";
        String result = "Y";
        try{
            name = proceedingJoinPoint.getSignature().toString();
            return proceedingJoinPoint.proceed();
        }catch (Throwable t){
            result = "N";
            throw t;
        }finally {
            long endTime = System.currentTimeMillis();
            System.out.println(name + "; " + result + "; " + (endTime - startTime) + " ms");
        }
    }

}
