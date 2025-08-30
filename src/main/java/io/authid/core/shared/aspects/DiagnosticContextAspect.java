package io.authid.core.shared.aspects;

import io.authid.core.shared.aspects.stringifiers.StringifierManager;
import io.authid.core.shared.constants.DiagnosticContextConstant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Aspect
@Component
public class DiagnosticContextAspect {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticContextAspect.class);

    private static final int MAX_STRING_LENGTH_TO_LOG = 100;
    private static final int MAX_MAP_ENTRIES_TO_LOG = 5;
    private static final int MAX_COLLECTION_ELEMENTS_TO_LOG = 5;
    private static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
            "password|secret|token|api_key|credit_card|cvv|ssn|auth|pin|otp",
            Pattern.CASE_INSENSITIVE
    );
    private final StringifierManager stringifierManager =
            StringifierManager.createDefault(
                    MAX_STRING_LENGTH_TO_LOG,
                    MAX_MAP_ENTRIES_TO_LOG,
                    MAX_COLLECTION_ELEMENTS_TO_LOG,
                    SENSITIVE_KEY_PATTERN
            );

    @Pointcut("within(io.authid.core..*.controllers.*Controller)")
    public void controllerLayer() {}

    @Pointcut("within(io.authid.core..*.services.*Service*)")
    public void serviceLayer() {}

    private record LogDetails(String layer, String className, String methodName) {}

    @Around("controllerLayer() || serviceLayer()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        String layer = getLayerType(joinPoint);
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        LogDetails details = new LogDetails(layer, className, methodName);

        MDC.put(DiagnosticContextConstant.MDC_KEY_CONTEXT_CLASS, className);
        MDC.put(DiagnosticContextConstant.MDC_KEY_OPERATION_NAME, methodName);

        try {
            logEntry(details, joinPoint.getArgs());

            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            logExit(details, result, duration);
            
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            logError(details, e, duration);
            throw e;
        } finally {
            MDC.remove(DiagnosticContextConstant.MDC_KEY_CONTEXT_CLASS);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_OPERATION_NAME);
        }
    }

    private void logEntry(LogDetails details, Object[] args) {
        log.info("Method execution started",
                kv("event", "entry"),
                kv("layer", details.layer),
                kv("class", details.className),
                kv("method", details.methodName),
                kv("arguments", stringifyArguments(args))
        );
    }

    private void logExit(LogDetails details, Object result, long duration) {
        log.info("Method execution finished",
                kv("event", "exit"),
                kv("layer", details.layer),
                kv("class", details.className),
                kv("method", details.methodName),
                kv("durationMs", duration),
                kv("result", serializeResult(result)) // Menggunakan serializer khusus
        );
    }

    private void logError(LogDetails details, Throwable error, long duration) {
        log.error("Method execution failed",
                kv("event", "error"),
                kv("layer", details.layer),
                kv("class", details.className),
                kv("method", details.methodName),
                kv("durationMs", duration),
                kv("errorClass", error.getClass().getSimpleName()),
                kv("errorMessage", error.getMessage()),
                error
        );
    }
    
    private Object serializeResult(Object result) {
        if (result instanceof ResponseEntity<?> entity) {
            return Map.of(
                "statusCode", entity.getStatusCode().toString(),
                "body", entity.getBody() // Body akan di-serialize oleh Jackson
            );
        }
        return result;
    }

    private String stringifyArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .map(stringifierManager::stringifyArg)
                .collect(Collectors.joining(", "));
    }

    private String getLayerType(ProceedingJoinPoint joinPoint) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        if (targetClass.isAnnotationPresent(RestController.class)) {
            return "CONTROLLER";
        }
        if (targetClass.isAnnotationPresent(Service.class) || targetClass.getSimpleName().endsWith("Service")) {
            return "SERVICE";
        }
        return "UNKNOWN_LAYER";
    }
}