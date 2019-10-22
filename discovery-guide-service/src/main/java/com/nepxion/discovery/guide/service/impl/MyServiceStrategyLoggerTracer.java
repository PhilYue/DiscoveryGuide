package com.nepxion.discovery.guide.service.impl;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @author Shun Zhang
 * @version 1.0
 */

import java.util.LinkedHashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.strategy.service.tracer.DefaultServiceStrategyTracer;
import com.nepxion.discovery.plugin.strategy.service.tracer.ServiceStrategyTracerInterceptor;

// 自定义调用链和灰度调用链通过MDC输出到日志
public class MyServiceStrategyLoggerTracer extends DefaultServiceStrategyTracer {
    private static final Logger LOG = LoggerFactory.getLogger(MyServiceStrategyLoggerTracer.class);

    @Override
    public void trace(ServiceStrategyTracerInterceptor interceptor, MethodInvocation invocation) {
        super.trace(interceptor, invocation);

        // 自定义调用链
        MDC.put("traceid", "traceid=" + strategyContextHolder.getHeader("traceid"));
        MDC.put("spanid", "spanid=" + strategyContextHolder.getHeader("spanid"));
        MDC.put("mobile", "mobile=" + strategyContextHolder.getHeader("mobile"));
        MDC.put("user", "user=" + strategyContextHolder.getHeader("user"));

        // 灰度路由调用链
        MDC.put(DiscoveryConstant.N_D_SERVICE_GROUP, "服务组名=" + pluginAdapter.getGroup());
        MDC.put(DiscoveryConstant.N_D_SERVICE_TYPE, "服务类型=" + pluginAdapter.getServiceType());
        MDC.put(DiscoveryConstant.N_D_SERVICE_ID, "服务名=" + pluginAdapter.getServiceId());
        MDC.put(DiscoveryConstant.N_D_SERVICE_ADDRESS, "地址=" + pluginAdapter.getHost() + ":" + pluginAdapter.getPort());
        MDC.put(DiscoveryConstant.N_D_SERVICE_VERSION, "版本=" + pluginAdapter.getVersion());
        MDC.put(DiscoveryConstant.N_D_SERVICE_REGION, "区域=" + pluginAdapter.getRegion());

        LOG.info("全链路灰度调用链输出到日志");

        String methodName = interceptor.getMethodName(invocation);
        LOG.info("methodName={}", methodName);

        Object[] arguments = interceptor.getArguments(invocation);
        for (int i = 0; i < arguments.length; i++) {
            LOG.info("arguments[{}]={}", i, arguments[i].toString());
        }
    }

    @Override
    public void error(ServiceStrategyTracerInterceptor interceptor, MethodInvocation invocation, Throwable e) {
        // 一般来说，日志方式对异常不需要做特殊处理，但必须也要把上下文参数放在MDC里，否则链路中异常环节会中断
        trace(interceptor, invocation);
    }

    @Override
    public void release(ServiceStrategyTracerInterceptor interceptor, MethodInvocation invocation) {
        MDC.clear();

        LOG.info("全链路灰度调用链日志上下文清除");
    }

    // Debug用
    @Override
    public Map<String, String> getDebugTraceMap() {
        Map<String, String> debugTraceMap = new LinkedHashMap<String, String>();
        debugTraceMap.put("traceid", strategyContextHolder.getHeader("traceid"));
        debugTraceMap.put("spanid", strategyContextHolder.getHeader("spanid"));
        debugTraceMap.put("mobile", strategyContextHolder.getHeader("mobile"));
        debugTraceMap.put("user", strategyContextHolder.getHeader("user"));

        return debugTraceMap;
    }
}