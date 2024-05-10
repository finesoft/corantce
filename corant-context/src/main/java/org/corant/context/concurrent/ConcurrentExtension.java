/*
 * Copyright (c) 2013-2021, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.context.concurrent;

import static java.lang.String.format;
import static org.corant.shared.util.Classes.tryAsClass;
import static org.corant.shared.util.Configurations.getConfigValue;
import static org.corant.shared.util.Lists.newArrayList;
import static org.corant.shared.util.Objects.defaultObject;
import static org.corant.shared.util.Strings.isNotBlank;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.annotation.Priority;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import jakarta.interceptor.Interceptor;
import org.corant.config.Configs;
import org.corant.context.concurrent.ContextServiceConfig.ContextInfo;
import org.corant.context.concurrent.annotation.Asynchronous;
import org.corant.context.concurrent.executor.DefaultContextService;
import org.corant.context.concurrent.executor.DefaultManagedExecutorService;
import org.corant.context.concurrent.executor.DefaultManagedScheduledExecutorService;
import org.corant.context.concurrent.executor.DefaultManagedThreadFactory;
import org.corant.context.concurrent.executor.ExecutorServiceManager;
import org.corant.context.concurrent.provider.BlockingQueueProvider;
import org.corant.context.concurrent.provider.ContextSetupProviderImpl;
import org.corant.context.concurrent.provider.TransactionSetupProviderImpl;
import org.corant.context.naming.NamingReference;
import org.corant.context.qualifier.Qualifiers.DefaultNamedQualifierObjectManager;
import org.corant.context.qualifier.Qualifiers.NamedQualifierObjectManager;
import org.corant.shared.exception.CorantRuntimeException;
import org.corant.shared.normal.Names.JndiNames;
import org.corant.shared.normal.Priorities;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedExecutorServiceAdapter;
import org.glassfish.enterprise.concurrent.ManagedScheduledExecutorServiceAdapter;

/**
 * corant-context
 *
 * @author bingo 下午2:51:48
 */
public class ConcurrentExtension implements Extension {

  public static final String ENABLE_DFLT_MES_CFG =
      "corant.concurrent.enable-default-managed-executor";
  public static final String ENABLE_DFLT_MSES_CFG =
      "corant.concurrent.enable-default-managed-scheduled-executor";
  public static final String ENABLE_DFLT_CS_CFG =
      "corant.concurrent.enable-default-context-service";
  public static final String ENABLE_DFLT_MTF_CFG =
      "corant.concurrent.enable-default-managed-thread-factory";
  public static final String ENABLE_HUNG_TASK_LOGGER_CFG =
      "corant.concurrent.enable-hung-task-logger";
  public static final String ENABLE_EXE_RUNNABLE_LOGGER_CFG =
      "corant.concurrent.enable-execute-runnable-logger";
  public static final String ENABLE_CONCURRENT_THROTTLE_INTERCEPTOR_CFG =
      "corant.concurrent.enable-concurrent-throttle-interceptor";
  public static final String ENABLE_ASYNC_INTERCEPTOR_CFG =
      "corant.concurrent.enable-concurrent-asynchronous-interceptor";
  public static final String JNDI_SUBCTX_NAME = JndiNames.JNDI_COMP_NME + "/concurrent";

  public static final boolean ENABLE_DFLT_MES =
      getConfigValue(ENABLE_DFLT_MES_CFG, Boolean.class, Boolean.TRUE);
  public static final boolean ENABLE_DFLT_MSES =
      getConfigValue(ENABLE_DFLT_MSES_CFG, Boolean.class, Boolean.TRUE);
  public static final boolean ENABLE_DFLT_CS =
      getConfigValue(ENABLE_DFLT_CS_CFG, Boolean.class, Boolean.TRUE);
  public static final boolean ENABLE_DFLT_MTF =
      getConfigValue(ENABLE_DFLT_MTF_CFG, Boolean.class, Boolean.TRUE);
  public static final boolean ENABLE_HUNG_TASK_LOGGER =
      getConfigValue(ENABLE_HUNG_TASK_LOGGER_CFG, Boolean.class, Boolean.FALSE);
  public static final boolean ENABLE_EXE_RUNNABLE_LOGGER =
      getConfigValue(ENABLE_EXE_RUNNABLE_LOGGER_CFG, Boolean.class, Boolean.TRUE);
  public static final boolean ENABLE_CONCURRENT_THROTTLE_INTERCEPTOR =
      getConfigValue(ENABLE_CONCURRENT_THROTTLE_INTERCEPTOR_CFG, Boolean.class, Boolean.FALSE);
  public static final boolean ENABLE_ASYNC_INTERCEPTOR =
      getConfigValue(ENABLE_ASYNC_INTERCEPTOR_CFG, Boolean.class, Boolean.FALSE);

  protected final Logger logger = Logger.getLogger(this.getClass().getName());

  protected volatile NamedQualifierObjectManager<ManagedExecutorConfig> executorConfigs =
      NamedQualifierObjectManager.empty();
  protected volatile NamedQualifierObjectManager<ManagedScheduledExecutorConfig> scheduledExecutorConfigs =
      NamedQualifierObjectManager.empty();
  protected volatile NamedQualifierObjectManager<ContextServiceConfig> contextServiceConfigs =
      NamedQualifierObjectManager.empty();
  protected volatile NamedQualifierObjectManager<ManagedThreadFactoryConfig> threadFactoryConfigs =
      NamedQualifierObjectManager.empty();
  protected volatile InitialContext jndi;

  protected Set<Class<?>> asyncBeanClass = new HashSet<>();
  protected Map<Asynchronous, AsynchronousConfig> asyncConfigs = new ConcurrentHashMap<>();

  public AsynchronousConfig getAsynchronousConfig(Asynchronous ann) {
    return asyncConfigs.get(ann);
  }

  public NamedQualifierObjectManager<ManagedExecutorConfig> getExecutorConfigs() {
    return executorConfigs;
  }

  public NamedQualifierObjectManager<ManagedScheduledExecutorConfig> getScheduledExecutorConfigs() {
    return scheduledExecutorConfigs;
  }

  protected void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery event) {
    // Don't use transitive type closure, the CDI can't differentiate, since the
    // ScheduledExecutorService extends ExecutorService
    if (event != null) {
      executorConfigs.getAllWithQualifiers().forEach((cfg, esn) -> {
        event.<ManagedExecutorService>addBean().addQualifiers(esn)
            .addType(ManagedExecutorService.class).addType(ExecutorService.class)
            .beanClass(ManagedExecutorServiceAdapter.class).scope(ApplicationScoped.class)
            .produceWith(beans -> {
              try {
                return register(beans, produce(beans, cfg), cfg);
              } catch (NamingException e) {
                throw new CorantRuntimeException(e);
              }
            });
        logger.info(() -> format("Resolved managed executor: %s %s", cfg.getName(), cfg));
        if (cfg.isEnableJndi() && isNotBlank(cfg.getName())) {
          registerJndi(cfg.getName(), ManagedExecutorService.class, esn);
        }
      });

      contextServiceConfigs.getAllWithQualifiers().forEach((cfg, esn) -> {
        event.<ContextService>addBean().addTransitiveTypeClosure(DefaultContextService.class)
            .addQualifiers(esn).beanClass(DefaultContextService.class)
            .scope(ApplicationScoped.class).produceWith(beans -> {
              try {
                return produce(beans, cfg);
              } catch (NamingException e) {
                throw new CorantRuntimeException(e);
              }
            });
        logger.info(() -> format("Resolved context service: %s %s", cfg.getName(), cfg));
        if (cfg.isEnableJndi() && isNotBlank(cfg.getName())) {
          registerJndi(cfg.getName(), ContextService.class, esn);
        }
      });

      // TODO FIXME, since the ManagedScheduledExecutorService extends ManagedExecutorService
      scheduledExecutorConfigs.getAllWithQualifiers().forEach((cfg, esn) -> {
        event.<ManagedScheduledExecutorService>addBean().addQualifiers(esn)
            .addType(ScheduledExecutorService.class).addType(ManagedScheduledExecutorService.class)
            .beanClass(ManagedScheduledExecutorServiceAdapter.class).scope(ApplicationScoped.class)
            .produceWith(beans -> {
              try {
                return register(beans, produce(beans, cfg), cfg);
              } catch (NamingException e) {
                throw new CorantRuntimeException(e);
              }
            });
        logger.info(() -> format("Resolved managed scheduled executor: %s %s", cfg.getName(), cfg));
        if (cfg.isEnableJndi() && isNotBlank(cfg.getName())) {
          registerJndi(cfg.getName(), ManagedScheduledExecutorService.class, esn);
        }
      });

      threadFactoryConfigs.getAllWithQualifiers().forEach((cfg, esn) -> {
        event.<ManagedThreadFactory>addBean().addQualifiers(esn)
            .addTransitiveTypeClosure(DefaultManagedThreadFactory.class)
            .beanClass(DefaultManagedThreadFactory.class).scope(ApplicationScoped.class)
            .produceWith(beans -> {
              try {
                return produce(beans, cfg);
              } catch (NamingException e) {
                throw new CorantRuntimeException(e);
              }
            });
        logger.info(() -> format("Resolved managed thread factory: %s %s", cfg.getName(), cfg));
        if (cfg.isEnableJndi() && isNotBlank(cfg.getName())) {
          registerJndi(cfg.getName(), ManagedThreadFactory.class, esn);
        }
      });
    }
  }

  protected void onBeforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
    Collection<ManagedExecutorConfig> mecs =
        newArrayList(Configs.resolveMulti(ManagedExecutorConfig.class).values());
    if (mecs.isEmpty() && ENABLE_DFLT_MES) {
      logger.info(() -> "Use default managed executor configuration");
      mecs.add(ManagedExecutorConfig.DFLT_INST);
    }

    Collection<ManagedScheduledExecutorConfig> msecs =
        newArrayList(Configs.resolveMulti(ManagedScheduledExecutorConfig.class).values());
    if (msecs.isEmpty() && ENABLE_DFLT_MSES) {
      logger.info(() -> "Use default managed scheduled executor configuration");
      msecs.add(ManagedScheduledExecutorConfig.DFLT_INST);
    }

    Collection<ContextServiceConfig> cscs =
        newArrayList(Configs.resolveMulti(ContextServiceConfig.class).values());
    if (cscs.isEmpty() && ENABLE_DFLT_CS) {
      logger.info(() -> "Use default context service configuration");
      cscs.add(ContextServiceConfig.DFLT_INST);
    }

    Collection<ManagedThreadFactoryConfig> mtfcs =
        newArrayList(Configs.resolveMulti(ManagedThreadFactoryConfig.class).values());
    if (mtfcs.isEmpty() && ENABLE_DFLT_MTF) {
      logger.info(() -> "Use default managed thread factory configuration");
      mtfcs.add(ManagedThreadFactoryConfig.DFLT_INST);
    }

    executorConfigs = new DefaultNamedQualifierObjectManager<>(mecs);
    scheduledExecutorConfigs = new DefaultNamedQualifierObjectManager<>(msecs);
    contextServiceConfigs = new DefaultNamedQualifierObjectManager<>(cscs);
    threadFactoryConfigs = new DefaultNamedQualifierObjectManager<>(mtfcs);
  }

  protected void onProcessAsynchronousAnnotatedType(
      @Observes @Priority(Priorities.FRAMEWORK_HIGHER) @WithAnnotations({
          Asynchronous.class}) ProcessAnnotatedType<?> event) {
    if (ENABLE_ASYNC_INTERCEPTOR) {
      Class<?> beanClass = event.getAnnotatedType().getJavaClass();
      if (!beanClass.isInterface() && !Modifier.isAbstract(beanClass.getModifiers())
          && !event.getAnnotatedType().isAnnotationPresent(Interceptor.class)) {
        asyncBeanClass.add(beanClass);
      }
    }
  }

  protected DefaultContextService produce(Instance<Object> instance, ContextServiceConfig cfg)
      throws NamingException {
    logger.fine(() -> format("Create context service %s with %s.", cfg.getName(), cfg));
    return createContextService(cfg.getName(), instance,
        cfg.getContextInfos().toArray(ContextInfo[]::new));
  }

  protected DefaultManagedExecutorService produce(Instance<Object> instance,
      ManagedExecutorConfig cfg) throws NamingException {
    DefaultManagedThreadFactory mtf = new DefaultManagedThreadFactory(cfg.getThreadName());
    DefaultContextService contextService =
        createContextService(cfg.getName(), instance, cfg.getContextInfos());
    Instance<BlockingQueueProvider> ques =
        instance.select(BlockingQueueProvider.class, NamedLiteral.of(cfg.getName()));
    if (ques.isResolvable()) {
      logger
          .fine(() -> format("Create managed executor service %s with customer blocking queue %s.",
              cfg.getName(), cfg));
      return new DefaultManagedExecutorService(cfg.getName(), mtf, cfg.getHungTaskThreshold(),
          cfg.isLongRunningTasks(), cfg.getCorePoolSize(), cfg.getMaxPoolSize(),
          cfg.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS,
          cfg.getThreadLifeTime().toMillis(), cfg.getAwaitTermination(), contextService,
          cfg.getRejectPolicy(), cfg.getRetryDelay(), ques.get().provide(cfg));
    } else {
      logger.fine(() -> format("Create managed executor service %s with %s.", cfg.getName(), cfg));
      return new DefaultManagedExecutorService(cfg.getName(), mtf, cfg.getHungTaskThreshold(),
          cfg.isLongRunningTasks(), cfg.getCorePoolSize(), cfg.getMaxPoolSize(),
          cfg.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS,
          cfg.getThreadLifeTime().toMillis(), cfg.getAwaitTermination(), cfg.getQueueCapacity(),
          contextService, cfg.getRejectPolicy(), cfg.getRetryDelay());
    }
  }

  protected DefaultManagedScheduledExecutorService produce(Instance<Object> instance,
      ManagedScheduledExecutorConfig cfg) throws NamingException {
    DefaultContextService contextService =
        createContextService(cfg.getName(), instance, cfg.getContextInfos());
    logger.fine(
        () -> format("Create managed scheduled executor service %s with %s.", cfg.getName(), cfg));
    return new DefaultManagedScheduledExecutorService(cfg.getName(),
        new DefaultManagedThreadFactory(cfg.getThreadName()), cfg.getHungTaskThreshold(),
        cfg.isLongRunningTasks(), cfg.getCorePoolSize(), cfg.getKeepAliveTime().toMillis(),
        TimeUnit.MILLISECONDS, cfg.getThreadLifeTime().toMillis(), cfg.getAwaitTermination(),
        contextService, cfg.getRejectPolicy(), cfg.getRetryDelay());
  }

  protected DefaultManagedThreadFactory produce(Instance<Object> instance,
      ManagedThreadFactoryConfig cfg) throws NamingException {
    ContextServiceImpl contextService = null;// FIXME use context service interface
    if (cfg.getContext() != null) {
      Annotation[] qualifiers = contextServiceConfigs.getQualifiers(cfg.getContext());
      contextService = instance.select(ContextServiceImpl.class, qualifiers).get();
    }
    logger.fine(() -> format("Create managed thread factory %s with %s.", cfg.getName(), cfg));
    return new DefaultManagedThreadFactory(cfg.getName(), contextService, cfg.getPriority());
  }

  protected ManagedExecutorServiceAdapter register(Instance<Object> instance,
      DefaultManagedExecutorService service, ManagedExecutorConfig cfg) {
    instance.select(ExecutorServiceManager.class).get().register(service);
    return service.getAdapter();
  }

  protected ManagedScheduledExecutorServiceAdapter register(Instance<Object> instance,
      DefaultManagedScheduledExecutorService service, ManagedScheduledExecutorConfig cfg) {
    instance.select(ExecutorServiceManager.class).get().register(service);
    return service.getAdapter();
  }

  protected void validate(@Observes AfterDeploymentValidation adv, BeanManager bm) {
    if (ENABLE_ASYNC_INTERCEPTOR && !asyncBeanClass.isEmpty()) {
      for (Class<?> clazz : asyncBeanClass) {
        Asynchronous clazzAsync = clazz.getAnnotation(Asynchronous.class);
        for (Method m : clazz.getMethods()) {
          if (Modifier.isPublic(m.getModifiers()) && !m.isBridge()) {
            Asynchronous methodAsync =
                defaultObject(m.getAnnotation(Asynchronous.class), clazzAsync);
            if (methodAsync != null) {
              Class<?> returnType = m.getReturnType();
              boolean validReturnType = returnType.equals(Void.TYPE)
                  || Future.class.equals(returnType) || CompletionStage.class.equals(returnType);
              if (!validReturnType) {
                adv.addDeploymentProblem(new CorantRuntimeException(
                    "The asynchronous method %s return type must be java.util.concurrent.Future or void!",
                    m.getName()));
              } else if (!executorConfigs.getAllNames().contains(methodAsync.executor())) {
                adv.addDeploymentProblem(new CorantRuntimeException(
                    "The asynchronous method %s executor not found!", m.getName()));
              }
              asyncConfigs.computeIfAbsent(methodAsync, AsynchronousConfig::new);
            }
          }
        }
      }
    }
  }

  DefaultContextService createContextService(String name, Instance<Object> instance,
      ContextInfo... infos) {
    if (tryAsClass("javax.transaction.TransactionManager") != null) {
      // FIXME check transaction manager CDI scope, now we assume it's application scope
      return new DefaultContextService(name, new ContextSetupProviderImpl(infos),
          new TransactionSetupProviderImpl(instance));
    }
    return new DefaultContextService(name, new ContextSetupProviderImpl(infos));
  }

  synchronized void registerJndi(String name, Class<?> clazz, Annotation... qualifiers) {
    if (isNotBlank(name)) {
      try {
        if (jndi == null) {
          jndi = new InitialContext();
          jndi.createSubcontext(JNDI_SUBCTX_NAME);
        }
        String jndiName = JNDI_SUBCTX_NAME + "/" + name;
        jndi.bind(jndiName, new NamingReference(clazz, qualifiers));
        logger.fine(() -> format("Bind %s %s to jndi.", clazz.getName(), jndiName));
      } catch (NamingException e) {
        throw new CorantRuntimeException(e);
      }
    }
  }
}
