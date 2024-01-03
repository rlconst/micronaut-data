/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.data.runtime.intercept.criteria.reactive;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.operations.reactive.ReactiveCapableRepository;
import io.micronaut.data.operations.reactive.ReactiveCriteriaCapableRepository;
import io.micronaut.data.operations.reactive.ReactiveCriteriaRepositoryOperations;
import io.micronaut.data.operations.reactive.ReactiveRepositoryOperations;
import io.micronaut.data.runtime.intercept.criteria.AbstractSpecificationInterceptor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Abstract reactive specification interceptor.
 *
 * @param <T> The declaring type
 * @param <R> The return type
 * @author Denis Stepanov
 * @since 3.2
 */
public abstract class AbstractReactiveSpecificationInterceptor<T, R> extends AbstractSpecificationInterceptor<T, R> {

    protected final ReactiveRepositoryOperations reactiveOperations;
    protected final ReactiveCriteriaRepositoryOperations reactiveCriteriaOperations;

    /**
     * Default constructor.
     *
     * @param operations The operations
     */
    protected AbstractReactiveSpecificationInterceptor(RepositoryOperations operations) {
        super(operations);
        if (operations instanceof ReactiveCapableRepository) {
            this.reactiveOperations = ((ReactiveCapableRepository) operations).reactive();
        } else {
            throw new DataAccessException("Datastore of type [" + operations.getClass() + "] does not support reactive operations");
        }
        if (operations instanceof ReactiveCriteriaRepositoryOperations reactiveCriteriaRepositoryOperations) {
            reactiveCriteriaOperations = reactiveCriteriaRepositoryOperations;
        } else if (operations instanceof ReactiveCriteriaCapableRepository repository) {
            reactiveCriteriaOperations = repository.reactive();
        } else {
            reactiveCriteriaOperations = null;
        }
    }

    @NonNull
    protected final Publisher<Object> findAllReactive(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context, Type type) {
        Set<JoinPath> methodJoinPaths = getMethodJoinPaths(methodKey, context);
        if (reactiveCriteriaOperations != null) {
            return reactiveCriteriaOperations.findAll(buildQuery(context, type, methodJoinPaths));
        }
        return reactiveOperations.findAll(preparedQueryForCriteria(methodKey, context, type, methodJoinPaths));
    }

    @NonNull
    protected final Publisher<Object> findOneReactive(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context, Type type) {
        Set<JoinPath> methodJoinPaths = getMethodJoinPaths(methodKey, context);
        if (reactiveCriteriaOperations != null) {
            return reactiveCriteriaOperations.findOne(buildQuery(context, type, methodJoinPaths));
        }
        return reactiveOperations.findOne(preparedQueryForCriteria(methodKey, context, type, methodJoinPaths));
    }

    @NonNull
    protected final Publisher<Long> countReactive(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context) {
        Set<JoinPath> methodJoinPaths = getMethodJoinPaths(methodKey, context);
        if (reactiveCriteriaOperations != null) {
            return reactiveCriteriaOperations.findOne(buildCountQuery(context));
        }
        return reactiveOperations.findOne(preparedQueryForCriteria(methodKey, context, Type.COUNT, methodJoinPaths));
    }

    protected final Publisher<Boolean> existsReactive(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context) {
        Set<JoinPath> methodJoinPaths = getMethodJoinPaths(methodKey, context);
        if (reactiveCriteriaOperations != null) {
            return reactiveCriteriaOperations.findOne(buildExistsQuery(context, methodJoinPaths));
        }
        return Mono.from(reactiveOperations.findOne(preparedQueryForCriteria(methodKey, context, Type.EXISTS, methodJoinPaths)))
            .map(one -> one instanceof Boolean aBoolean ? aBoolean : one != null);
    }

    protected final Publisher<Number> deleteAllReactive(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context) {
        Set<JoinPath> methodJoinPaths = getMethodJoinPaths(methodKey, context);
        if (reactiveCriteriaOperations != null) {
            return reactiveCriteriaOperations.deleteAll(buildDeleteQuery(context));
        }
        return reactiveOperations.executeDelete(preparedQueryForCriteria(methodKey, context, Type.DELETE_ALL, methodJoinPaths));
    }

    protected final Publisher<Number> updateAllReactive(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context) {
        Set<JoinPath> methodJoinPaths = getMethodJoinPaths(methodKey, context);
        if (reactiveCriteriaOperations != null) {
            return reactiveCriteriaOperations.updateAll(buildUpdateQuery(context));
        }
        return reactiveOperations.executeUpdate(preparedQueryForCriteria(methodKey, context, Type.UPDATE_ALL, methodJoinPaths));
    }
}
