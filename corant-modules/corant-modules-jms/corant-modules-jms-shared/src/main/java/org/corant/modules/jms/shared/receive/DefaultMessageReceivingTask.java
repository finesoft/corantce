/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
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
package org.corant.modules.jms.shared.receive;

import static java.lang.String.format;
import static org.corant.context.Beans.resolve;
import static org.corant.shared.util.Objects.defaultObject;
import static org.corant.shared.util.Objects.max;
import static org.corant.shared.util.Threads.tryThreadSleep;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.corant.context.CDIs;
import org.corant.modules.jms.JMSNames;
import org.corant.modules.jms.marshaller.MessageMarshaller;
import org.corant.modules.jms.receive.ManagedMessageReceiveReplier;
import org.corant.modules.jms.receive.ManagedMessageReceiver;
import org.corant.modules.jms.receive.ManagedMessageReceivingHandler;
import org.corant.modules.jms.receive.ManagedMessageReceivingTask;
import org.corant.shared.retry.BackoffStrategy;

/**
 * corant-modules-jms-shared
 *
 * <p>
 * Default message receiving task, supports retry and circuit break, and supports various retry
 * interval compensation algorithms.
 *
 * <p>
 * Unfinished: use connection or session pool, commit ordering
 *
 * <p>
 * <a href = "https://developer.jboss.org/wiki/ShouldICacheJMSConnectionsAndJMSSessions"> Should I
 * cache JMS connections and JMS sessions</a>
 *
 * <p>
 * <a href = "https://www.atomikos.com/Documentation/CommitOrderingWithJms">Atomikos Commit
 * Ordering</a>
 *
 * <p>
 * <a href="https://developer.jboss.org/thread/274469">Narayana Commit Order</a>
 *
 * @author bingo 上午11:33:15
 */
public class DefaultMessageReceivingTask
    implements ManagedMessageReceivingTask, MessageReceivingMediator {

  public static final byte RUNNING = 0;
  public static final byte TRYING = 1;
  public static final byte BROKEN = 2;

  protected static final Logger logger =
      Logger.getLogger(DefaultMessageReceivingTask.class.getName());

  // configuration
  protected final MessageReceivingMetaData meta;

  // executor controller
  protected final AtomicBoolean cancellation = new AtomicBoolean();

  // control to reconnect JMS server
  protected final int jmsFailureThreshold;
  protected final AtomicInteger jmsFailureCounter = new AtomicInteger(0);

  // control circuit break
  protected final int failureThreshold;
  protected final BackoffStrategy backoffStrategy;
  protected final int tryThreshold;

  protected volatile byte state = RUNNING;
  protected volatile long brokenTimePoint;
  protected volatile long brokenMillis;
  protected volatile boolean inProgress;
  protected volatile boolean lastExecutionSuccessfully = false;

  protected final AtomicInteger failureCounter = new AtomicInteger(0);
  protected final AtomicInteger tryCounter = new AtomicInteger(0);
  protected final AtomicInteger tryFailureCounter = new AtomicInteger(0);

  // the workhorse
  protected final ManagedMessageReceiver messageReceiver;
  protected final ManagedMessageReceivingHandler messageHandler;
  protected final ManagedMessageReceiveReplier messageReplier;

  public DefaultMessageReceivingTask(MessageReceivingMetaData metaData) {
    this(metaData, metaData.getBrokenBackoffStrategy());
  }

  public DefaultMessageReceivingTask(MessageReceivingMetaData metaData,
      BackoffStrategy backoffStrategy) {
    meta = metaData;
    failureThreshold = metaData.getFailureThreshold();
    jmsFailureThreshold = max(failureThreshold / 2, 2);
    this.backoffStrategy = backoffStrategy;
    tryThreshold = metaData.getTryThreshold();
    messageReplier = new DefaultMessageReplier(meta, this);
    messageHandler = new DefaultMessageHandler(meta, this);
    messageReceiver = new DefaultMessageReceiver(metaData, messageHandler, this);
    logger.fine(() -> format("Create message receive task for %s.", metaData));
  }

  @Override
  public synchronized boolean cancel() {
    int tryTimes = 0;
    long waitMs = max(meta.getReceiveTimeout(), 500L);
    while (true) {
      final int t = tryTimes++;
      if (!isInProgress() || t > 128) {
        logger.info(() -> format("Cancel message receiving task try times %s, %s.", t, meta));
        return cancellation.compareAndSet(false, true);
      } else {
        logger.info(() -> format("Waiting for message receiving task completed, %s.", meta));
        tryThreadSleep(waitMs);
      }
    }
  }

  @Override
  public boolean checkCancelled() {
    if (cancellation.get()) {
      resetMonitors();
      messageReceiver.release(true);
      logger.log(Level.INFO, () -> format("The message receiving task was cancelled, %s.", meta));
      return true;
    }
    return false;
  }

  @Override
  public int compareTo(Delayed o) {
    if (o == this) {
      return 0;
    }
    return Long.compare(getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
  }

  @Override
  public long getDelay(TimeUnit unit) {
    if (unit == null || unit == TimeUnit.MILLISECONDS) {
      return brokenMillis;
    } else {
      return unit.convert(brokenMillis, TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public MessageMarshaller getMessageMarshaller(String schema) {
    return resolve(MessageMarshaller.class,
        NamedLiteral.of(defaultObject(schema, JMSNames.MSG_MARSHAL_SCHEMA_STD_JAVA)));
  }

  public MessageReceivingMetaData getMeta() {
    return meta;
  }

  public boolean isInProgress() {
    return inProgress;
  }

  @Override
  public void onPostMessageHandled(Message message, Session session, Object result)
      throws JMSException {
    messageReplier.reply(session, message, result);
  }

  @Override
  public void onReceivingException(Exception e) {
    if (e instanceof JMSException) {
      jmsFailureCounter.incrementAndGet();
    }
    if (lastExecutionSuccessfully) {
      failureCounter.set(1);
    } else {
      failureCounter.incrementAndGet();
    }
  }

  @Override
  public synchronized void run() {
    if (checkCancelled()) {
      return;
    }
    if (preRun()) {
      inProgress = true;
      lastExecutionSuccessfully = messageReceiver.receive();
      inProgress = false;
      postRun();
    }
  }

  protected void postRun() {
    try {
      if (state == RUNNING) {
        if (failureCounter.intValue() >= failureThreshold) {
          switchToBroken();
          return;
        }
      } else if (state == TRYING) {
        if (failureCounter.intValue() > 0) {
          tryFailureCounter.incrementAndGet();
          switchToBroken();
          return;
        } else {
          tryFailureCounter.set(0);
          backoffStrategy.reset();
          if (tryCounter.incrementAndGet() >= tryThreshold) {
            switchToRunning();
          }
        }
      }
      messageReceiver.release(jmsFailureCounter.compareAndSet(jmsFailureThreshold, 0)); // FIXME
    } catch (Exception e) {
      logger.log(Level.SEVERE, e, () -> format("Execution status occurred error, %s.", meta));
    }
  }

  protected boolean preRun() {
    if (!CDIs.isEnabled()) {
      logger.severe(() -> format("Task can't run, CDI not enabled, %s.", meta));
      return false;
    }
    if (state == BROKEN) {
      switchToTrying();
    }
    return true;
  }

  protected void resetMonitors() {
    lastExecutionSuccessfully = true;
    jmsFailureCounter.set(0);
    failureCounter.set(0);
    brokenMillis = 0;
    brokenTimePoint = 0;
    tryCounter.set(0);
  }

  protected void switchToBroken() {
    resetMonitors();
    brokenTimePoint = System.currentTimeMillis();
    brokenMillis = backoffStrategy.computeBackoffMillis(tryFailureCounter.get());
    state = BROKEN;
    logger.warning(format("Task enters broken state wait for [%s] ms, [%s]", brokenMillis, meta));
    messageReceiver.release(true);
  }

  protected void switchToRunning() {
    resetMonitors();
    state = RUNNING;
    logger.info(() -> format("Task enters running state, [%s]!", meta));
  }

  protected void switchToTrying() {
    resetMonitors();
    state = TRYING;
    logger.info(() -> format("Task enters trying state, [%s]!", meta));
  }
}
