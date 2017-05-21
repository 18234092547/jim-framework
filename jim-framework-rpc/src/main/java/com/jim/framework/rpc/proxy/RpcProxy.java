package com.jim.framework.rpc.proxy;

import com.jim.framework.rpc.client.RpcClientInvoker;
import com.jim.framework.rpc.client.RpcClientInvokerManager;
import com.jim.framework.rpc.common.RpcInvoker;
import com.jim.framework.rpc.common.RpcRequest;
import com.jim.framework.rpc.config.ReferenceConfig;
import com.jim.framework.rpc.context.RpcContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class RpcProxy <T> implements InvocationHandler {

    private Class<T> clazz;

    private boolean isSync=true;

    private ReferenceConfig referenceConfig;

    public RpcProxy(Class<T> clazz,ReferenceConfig referenceConfig,boolean isSync) {
        this.clazz = clazz;
        this.referenceConfig=referenceConfig;
        this.isSync=isSync;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        RpcClientInvoker invoker = RpcClientInvokerManager.getInstance(this.referenceConfig).getInvoker();
        invoker.setRpcRequest(request);

        RpcInvoker rpcInvoker=invoker.buildInvokerChain(invoker);
        ResponseFuture response=(ResponseFuture) rpcInvoker.invoke(invoker.buildRpcInvocation(request));

        if(isSync){
            return response.get();
        }
        else {
            RpcContext.getContext().setResponseFuture(response);
            return null;
        }
    }
}
