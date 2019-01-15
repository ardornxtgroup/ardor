/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.addons;

import nxt.util.Logger;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

class ParamInvocationHandler implements InvocationHandler {
    private final Map<Class<? extends Annotation>, JO> annotationToParameters;

    ParamInvocationHandler(JO runnerConfigParams, JO contractSetupParams, JO invocationParams) {
        annotationToParameters = new LinkedHashMap<>();
        annotationToParameters.put(ContractInvocationParameter.class, invocationParams);
        annotationToParameters.put(ContractSetupParameter.class, contractSetupParams);
        annotationToParameters.put(ContractRunnerParameter.class, runnerConfigParams);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
            try {
                return getValueFromParameters(proxy, method, args);
            } catch (ValueNotSet ignored) {
                return getDefaultValue(proxy, method, args);
            }
        });
    }

    private Object getDefaultValue(Object proxy, Method method, Object[] args) throws ReflectiveOperationException {
        if (method.isDefault()) {
            Class<?> declaringClass = method.getDeclaringClass();
            Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);
            try {
                return constructor.newInstance(declaringClass)
                        .in(declaringClass)
                        .unreflectSpecial(method, declaringClass)
                        .bindTo(proxy)
                        .invokeWithArguments(args);
            } catch (Throwable t) {
                throw new IllegalStateException(t);
            }
        }
        return typeDefault(method.getReturnType());
    }

    private Object typeDefault(Class<?> type) {
        if (type.isAssignableFrom(boolean.class)) {
            return false;
        } else if (type.isAssignableFrom(int.class)) {
            return 0;
        } else if (type.isAssignableFrom(long.class)) {
            return 0L;
        }
        return null;
    }

    private Object getValueFromParameters(Object proxy, Method method, Object[] args) throws ValueNotSet {
        for (Class<? extends Annotation> clazz : annotationToParameters.keySet()) {
            if (method.getAnnotation(clazz) == null) {
                continue;
            }
            JO jo = annotationToParameters.get(clazz);
            String name = method.getName();
            if (!jo.containsKey(name)) {
                continue;
            }
            Class<?> type = method.getReturnType();
            BiFunction<JO, String, Object> valueGetter = getValueGetter(method, type);
            if (valueGetter == null) {
                Logger.logWarningMessage(String.format("Param %s of type %s is not of one of the supported parameter types",
                        name, type.getName()));
                break;
            }
            return valueGetter.apply(jo, name);
        }
        throw new ValueNotSet();
    }

    private BiFunction<JO, String, Object> getValueGetter(Method field, Class<?> type) {
        // Update the field value based on the supported data types
        if (type.isAssignableFrom(boolean.class)) {
            return JO::getBoolean;
        } else if (type.isAssignableFrom(int.class)) {
            return JO::getInt;
        } else if (type.isAssignableFrom(long.class)) {
            if (field.getDeclaredAnnotation(BlockchainEntity.class) == null) {
                return JO::getLong;
            } else {
                return JO::getEntityId;
            }
        } else if (type.isAssignableFrom(String.class)) {
            return JO::getString;
        } else if (type.isAssignableFrom(JO.class)) {
            return JO::getJo;
        } else if (type.isAssignableFrom(JA.class)) {
            return JO::getArray;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static <Params> Params getParams(Class<Params> clazz, JO runnerConfigParams, JO contractSetupParameters, JO invocationParams) {
        return (Params) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new ParamInvocationHandler(runnerConfigParams, contractSetupParameters, invocationParams));
    }

    private static class ValueNotSet extends Exception {

    }
}
