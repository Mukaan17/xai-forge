package com.xaiforge.application.aspect;

import com.xaiforge.application.service.ActivityLogApplicationService;
import com.xaiforge.common.annotation.LogActivity;
import com.xaiforge.domain.activity.entity.ActivityLog;
import com.xaiforge.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AOP Aspect for automatic activity logging.
 * Intercepts methods annotated with @LogActivity and logs the activity automatically.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLoggingAspect {

    private final ActivityLogApplicationService activityLogService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Around("@annotation(logActivity)")
    public Object logActivity(ProceedingJoinPoint joinPoint, LogActivity logActivity) throws Throwable {
        Object result;
        
        try {
            // Execute the method
            result = joinPoint.proceed();
            
            // Extract user ID from SecurityContext
            Long userId = extractUserId();
            if (userId == null) {
                log.debug("No user found in SecurityContext, skipping activity log");
                return result;
            }
            
            // Build evaluation context for SpEL expressions
            EvaluationContext context = buildEvaluationContext(joinPoint, result);
            
            // Extract values using SpEL
            String description = evaluateExpression(logActivity.description(), context, String.class, "");
            String resourceType = logActivity.resourceType();
            String resourceId = evaluateExpression(logActivity.resourceId(), context, String.class, null);
            String resourceName = evaluateExpression(logActivity.resourceName(), context, String.class, null);
            
            // Build metadata
            Map<String, Object> metadata = new HashMap<>();
            if (resourceType != null && !resourceType.isEmpty()) {
                metadata.put("resourceType", resourceType);
            }
            if (resourceId != null && !resourceId.isEmpty()) {
                metadata.put("resourceId", resourceId);
            }
            if (resourceName != null && !resourceName.isEmpty()) {
                metadata.put("resourceName", resourceName);
            }
            
            // Convert string event type to enum
            ActivityLog.EventType eventType;
            try {
                eventType = ActivityLog.EventType.valueOf(logActivity.eventType());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid event type in @LogActivity: {}", logActivity.eventType());
                return result;
            }
            
            // Log activity asynchronously to avoid blocking
            activityLogService.logActivityAsync(
                userId,
                eventType,
                description,
                metadata
            );
            
            return result;
            
        } catch (Throwable e) {
            // Don't log activity if method failed
            throw e;
        }
    }

    /**
     * Extract user ID from SecurityContext.
     */
    private Long extractUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                return user.getId();
            }
        } catch (Exception e) {
            log.debug("Failed to extract user ID from SecurityContext", e);
        }
        return null;
    }

    /**
     * Build evaluation context for SpEL expressions.
     */
    private EvaluationContext buildEvaluationContext(ProceedingJoinPoint joinPoint, Object result) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // Add method arguments
        Object[] args = joinPoint.getArgs();
        String[] paramNames = getParameterNames(joinPoint);
        for (int i = 0; i < args.length; i++) {
            if (paramNames != null && i < paramNames.length) {
                context.setVariable(paramNames[i], args[i]);
            }
            context.setVariable("arg" + i, args[i]);
        }
        
        // Add return value
        context.setVariable("result", result);
        
        return context;
    }

    /**
     * Get parameter names from join point (simplified - in production, use ParameterNameDiscoverer).
     */
    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        // This is a simplified version. In production, you'd use Spring's ParameterNameDiscoverer
        // or compile with -parameters flag to get actual parameter names
        return null;
    }

    /**
     * Evaluate SpEL expression safely.
     */
    private <T> T evaluateExpression(String expression, EvaluationContext context, Class<T> expectedType, T defaultValue) {
        if (expression == null || expression.isEmpty()) {
            return defaultValue;
        }
        
        try {
            Expression expr = expressionParser.parseExpression(expression);
            Object value = expr.getValue(context);
            if (value == null) {
                return defaultValue;
            }
            if (expectedType.isInstance(value)) {
                return expectedType.cast(value);
            }
            return defaultValue;
        } catch (Exception e) {
            log.debug("Failed to evaluate SpEL expression: {}", expression, e);
            return defaultValue;
        }
    }
}
