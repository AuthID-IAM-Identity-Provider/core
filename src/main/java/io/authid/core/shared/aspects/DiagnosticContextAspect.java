package io.authid.core.shared.aspects;

import io.authid.core.shared.aspects.stringifiers.*;
import io.authid.core.shared.constants.DiagnosticContextConstant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Aspect untuk logging terstruktur dan pengisian MDC pada layer Controller dan Service.
 * Menggunakan satu advice untuk mengurangi duplikasi kode.
 */
@Aspect
@Component
public class DiagnosticContextAspect {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticContextAspect.class);

    // --- Konfigurasi Stringifier (Sama seperti sebelumnya) ---
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

    // --- Definisi Pointcut ---
    @Pointcut("within(io.authid.core..*.controllers.*Controller)")
    public void controllerLayer() {}

    @Pointcut("within(io.authid.core..*.services.*Service*)")
    public void serviceLayer() {}

    /**
     * Advice tunggal yang menangani logging untuk Controller dan Service.
     */
    @Around("controllerLayer() || serviceLayer()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String layer = getLayerType(joinPoint); // Mendapatkan tipe layer: "CONTROLLER" atau "SERVICE"
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // Mengisi MDC dengan konteks yang lebih spesifik
        MDC.put(DiagnosticContextConstant.MDC_KEY_CONTEXT_CLASS, className);
        MDC.put(DiagnosticContextConstant.MDC_KEY_OPERATION_NAME, methodName);

        try {
            String args = stringifyArguments(joinPoint.getArgs());
            log.info("==> {}::{}({})", layer, methodName, args);

            // Eksekusi method asli
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.info("<== {}::{} returned [{}] in {}ms", layer, methodName, stringifierManager.stringifyArg(result), duration);

            return result;

        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("<== {}::{} threw {} in {}ms", layer, methodName, e.getClass().getSimpleName(), duration, e);
            throw e; // Lemparkan kembali exception setelah di-log

        } finally {
            // Selalu bersihkan MDC yang ditambahkan oleh aspect ini
            MDC.remove(DiagnosticContextConstant.MDC_KEY_CONTEXT_CLASS);
            MDC.remove(DiagnosticContextConstant.MDC_KEY_OPERATION_NAME);
        }
    }

    /**
     * Metode bantuan untuk mengubah argumen method menjadi string.
     */
    private String stringifyArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .map(stringifierManager::stringifyArg)
                .collect(Collectors.joining(", "));
    }

    /**
     * Metode bantuan untuk menentukan tipe layer berdasarkan anotasi atau nama kelas.
     */
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