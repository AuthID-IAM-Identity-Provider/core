package io.authid.core.shared.aspects;

import io.authid.core.shared.aspects.stringifiers.*; // Import semua stringifier
import io.authid.core.shared.constants.DiagnosticContextConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Aspect
@Component
public class DiagnosticContextAspect {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticContextAspect.class);

    // Konfigurasi untuk penyamaran (masking) data sensitif
    private static final int MAX_STRING_LENGTH_TO_LOG = 100;
    private static final int MAX_MAP_ENTRIES_TO_LOG = 5;
    private static final int MAX_COLLECTION_ELEMENTS_TO_LOG = 5;

    private static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
            "password|secret|token|api_key|credit_card|cvv|ssn|auth|pin|otp",
            Pattern.CASE_INSENSITIVE
    );

    // Inisialisasi StringifierManager sekali
    private final StringifierManager stringifierManager =
            StringifierManager.createDefault(
                    MAX_STRING_LENGTH_TO_LOG,
                    MAX_MAP_ENTRIES_TO_LOG,
                    MAX_COLLECTION_ELEMENTS_TO_LOG,
                    SENSITIVE_KEY_PATTERN
            );

    @Pointcut("within(io.authid.core.shared.rest.controllers.RestController+)")
    public void restControllerMethods() {}

    @Pointcut("within(io.authid.core.shared.rest.services.RestServiceImpl+)")
    public void restServiceMethods() {}

    @Around("restControllerMethods()")
    public Object logRestControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String subclassName = joinPoint.getTarget().getClass().getSimpleName();
        String args = Arrays.stream(joinPoint.getArgs())
                .map(stringifierManager::stringifyArg) // Panggil manager
                .collect(Collectors.joining(", "));

        MDC.put(DiagnosticContextConstant.MDC_KEY_SUPERCLASS_NAME, className);
        MDC.put(DiagnosticContextConstant.MDC_KEY_SUBCLASS_NAME, subclassName);

        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
        } catch (IllegalStateException e) {
            log.debug("Not in a web request context for controller logging.");
        }

        if (request != null) {
            log.info(">>>> Request: {} {} | Controller [{}] method [{}.{}] with args: [{}]",
                    request.getMethod(), request.getRequestURI(), subclassName, className, methodName, args);
        } else {
            log.info(">>>> Controller [{}] method [{}.{}] with args: [{}]",
                    subclassName, className, methodName, args);
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("<<<< Controller [{}] method [{}.{}] threw exception: {} in {}ms",
                    subclassName, className, methodName, e.getMessage(), duration, e);
            throw e;
        } finally {
            MDC.remove(DiagnosticContextConstant.MDC_KEY_SUPERCLASS_NAME);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_SUBCLASS_NAME);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("<<<< Controller [{}] method [{}.{}] returned: {} in {}ms",
                subclassName, className, methodName, stringifierManager.stringifyArg(result), duration); // Panggil manager
        return result;
    }

    @Around("restServiceMethods()")
    public Object logRestServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String subclassName = joinPoint.getTarget().getClass().getSimpleName();
        String args = Arrays.stream(joinPoint.getArgs())
                .map(stringifierManager::stringifyArg) // Panggil manager
                .collect(Collectors.joining(", "));

        MDC.put(DiagnosticContextConstant.MDC_KEY_SUPERCLASS_NAME, className);
        MDC.put(DiagnosticContextConstant.MDC_KEY_SUBCLASS_NAME, subclassName);

        log.debug(">>>> Service [{}] method [{}.{}] with args: [{}]",
                subclassName, className, methodName, args);

        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("<<<< Service [{}] method [{}.{}] threw exception: {} in {}ms",
                    subclassName, className, methodName, e.getMessage(), duration, e);
            throw e;
        } finally {
            MDC.remove(DiagnosticContextConstant.MDC_KEY_SUPERCLASS_NAME);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_SUBCLASS_NAME);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.debug("<<<< Service [{}] method [{}.{}] returned: {} in {}ms",
                subclassName, className, methodName, stringifierManager.stringifyArg(result), duration); // Panggil manager
        return result;
    }
}
