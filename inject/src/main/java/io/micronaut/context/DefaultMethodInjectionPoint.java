/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.context;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.MethodInjectionPoint;
import io.micronaut.inject.annotation.AbstractEnvironmentAnnotationMetadata;
import io.micronaut.core.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * A method injection point that does not use reflection.
 *
 * @author graemerocher
 * @since 1.0
 * @param <B> The bean type
 * @param <T> The injectable type
 */
@Internal
class DefaultMethodInjectionPoint<B, T> implements MethodInjectionPoint<B, T>, EnvironmentConfigurable {

    protected final Class<?> declaringType;
    private final BeanDefinition<B> declaringBean;
    private final AnnotationMetadata annotationMetadata;
    private final String methodName;
    private final Class<?>[] argTypes;
    private final Argument<?>[] arguments;
    private Environment environment;

    /**
     * Constructs a new {@link DefaultMethodInjectionPoint}.
     *
     * @param declaringBean      The declaring bean
     * @param declaringType      The declaring type
     * @param methodName         The method name
     * @param arguments          The arguments
     * @param annotationMetadata The annotation metadata
     */
    DefaultMethodInjectionPoint(
        BeanDefinition<B> declaringBean,
        Class<?> declaringType,
        String methodName,
        @Nullable Argument<?>[] arguments,
        @Nullable AnnotationMetadata annotationMetadata) {
        Objects.requireNonNull(declaringBean, "Declaring bean cannot be null");
        this.declaringType = declaringType;
        this.methodName = methodName;
        this.arguments = arguments == null ? Argument.ZERO_ARGUMENTS : arguments;
        this.argTypes = Argument.toClassArray(arguments);
        this.declaringBean = declaringBean;
        this.annotationMetadata = initAnnotationMetadata(annotationMetadata);
    }

    @Override
    public final boolean hasPropertyExpressions() {
        return annotationMetadata.hasPropertyExpressions();
    }

    @Override
    public String toString() {
        String text = Argument.toString(getArguments());
        return declaringType.getSimpleName() + "." + methodName + "(" + text + ")";
    }

    @Override
    public void configure(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Method getMethod() {
        Method method = ReflectionUtils.getMethod(declaringType, methodName, argTypes)
            .orElseThrow(() -> ReflectionUtils.newNoSuchMethodError(declaringType, methodName, argTypes));
        method.setAccessible(true);
        return method;
    }

    @Override
    public String getName() {
        return methodName;
    }

    @Override
    public boolean isPreDestroyMethod() {
        return annotationMetadata.hasDeclaredAnnotation(AnnotationUtil.PRE_DESTROY);
    }

    @Override
    public boolean isPostConstructMethod() {
        return annotationMetadata.hasDeclaredAnnotation(AnnotationUtil.POST_CONSTRUCT);
    }

    @Override
    public T invoke(Object instance, Object... args) {
        Method targetMethod = getMethod();
        return ReflectionUtils.invokeMethod(instance, targetMethod, args);
    }

    @Override
    @NonNull
    public AnnotationMetadata getAnnotationMetadata() {
        return annotationMetadata;
    }

    @Override
    @NonNull
    public BeanDefinition<B> getDeclaringBean() {
        return declaringBean;
    }

    @Override
    public boolean requiresReflection() {
        return false;
    }

    @Override
    @NonNull
    public Argument<?>[] getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        DefaultMethodInjectionPoint<B, T> that = (DefaultMethodInjectionPoint<B, T>) o;
        return Objects.equals(declaringType, that.declaringType) &&
            Objects.equals(methodName, that.methodName) &&
            Arrays.equals(argTypes, that.argTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(declaringType, methodName);
        result = 31 * result + Arrays.hashCode(argTypes);
        return result;
    }

    private AnnotationMetadata initAnnotationMetadata(@Nullable AnnotationMetadata annotationMetadata) {
        if (annotationMetadata != AnnotationMetadata.EMPTY_METADATA) {
            if (annotationMetadata != null) {
                if (annotationMetadata.hasPropertyExpressions()) {
                    return new MethodAnnotationMetadata(annotationMetadata);
                } else {
                    return annotationMetadata;
                }
            }
        }
        return AnnotationMetadata.EMPTY_METADATA;
    }

    /**
     * Internal environment aware annotation metadata delegate.
     */
    private final class MethodAnnotationMetadata extends AbstractEnvironmentAnnotationMetadata {
        MethodAnnotationMetadata(AnnotationMetadata targetMetadata) {
            super(targetMetadata);
        }

        @Nullable
        @Override
        protected Environment getEnvironment() {
            return environment;
        }
    }
}
