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
package org.corant.suites.jms.shared.send;

import static org.corant.kernel.util.Instances.resolveApply;
import java.util.logging.Logger;
import javax.jms.JMSContext;
import javax.jms.Message;
import org.corant.suites.jms.shared.context.JMSContextProducer;

/**
 * corant-suites-jms-shared
 *
 * @author bingo 上午9:34:56
 *
 */
public abstract class AbstractMessageSender implements MessageSender {

  protected final Logger logger = Logger.getLogger(this.getClass().getName());

  @Override
  public void send(Message message, String connectionFactoryId, String destination,
      boolean multicast, int sessionMode) {
    final JMSContext jmsc =
        resolveApply(JMSContextProducer.class, b -> b.create(connectionFactoryId, sessionMode));
    send(jmsc, message, destination, multicast);
  }

  protected void send(JMSContext jmsc, Message message, String destination, boolean multicast) {
    if (multicast) {
      jmsc.createProducer().send(jmsc.createTopic(destination), message);
    } else {
      jmsc.createProducer().send(jmsc.createQueue(destination), message);
    }
  }

}
