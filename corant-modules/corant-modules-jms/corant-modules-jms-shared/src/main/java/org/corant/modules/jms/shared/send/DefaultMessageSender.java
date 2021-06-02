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
package org.corant.modules.jms.shared.send;

import static org.corant.context.Instances.findNamed;
import static org.corant.context.Instances.resolveApply;
import static org.corant.shared.util.Assertions.shouldNotNull;
import static org.corant.shared.util.Classes.tryAsClass;
import static org.corant.shared.util.Conversions.toObject;
import static org.corant.shared.util.Empties.isNotEmpty;
import static org.corant.shared.util.Streams.copy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSProducer;
import javax.jms.JMSSessionMode;
import javax.jms.Message;
import javax.jms.Queue;
import org.corant.config.Configs;
import org.corant.modules.jms.annotation.MessageDestination;
import org.corant.modules.jms.annotation.MessageSend;
import org.corant.modules.jms.marshaller.MessageMarshaller;
import org.corant.modules.jms.send.MessageSender;
import org.corant.modules.jms.shared.context.DefaultJMSContextService;
import org.corant.shared.exception.CorantRuntimeException;

/**
 * corant-modules-jms-shared
 *
 * @author bingo 下午5:52:36
 *
 */
public class DefaultMessageSender implements MessageSender {
  protected boolean multicast;
  protected String destination;
  protected String connectionFactoryId;
  protected boolean dupsOkAck = false;
  protected int deliveryMode = DeliveryMode.PERSISTENT;
  protected long deliveryDelay = -1;
  protected long timeToLive = -1;
  protected Map<String, Object> properties = new HashMap<>();

  public DefaultMessageSender(JMSDestinationDefinition dann, JMSSessionMode sann) {
    multicast = Queue.class.isAssignableFrom(tryAsClass(dann.description()));
    destination = shouldNotNull(Configs.assemblyStringConfigProperty(dann.destinationName()));
    connectionFactoryId = shouldNotNull(Configs.assemblyStringConfigProperty(dann.name()));
    if (sann.value() == JMSContext.DUPS_OK_ACKNOWLEDGE) {
      dupsOkAck = true;
    }
    deliveryMode = DeliveryMode.PERSISTENT;
  }

  public DefaultMessageSender(MessageSend annotation) {
    MessageDestination annDest = annotation.destination();
    multicast = annDest.multicast();
    destination = annDest.name();
    connectionFactoryId = annDest.connectionFactoryId();
    dupsOkAck = annotation.dupsOkAck();
    deliveryMode = annotation.deliveryMode();
    deliveryDelay = annotation.deliveryDelay();
    timeToLive = annotation.timeToLive();
    if (isNotEmpty(annotation.properties())) {
      Arrays.stream(annotation.properties())
          .forEach(p -> properties.put(p.name(), toObject(p.value(), p.type())));
    }
  }

  protected DefaultMessageSender() {

  }

  @Override
  public void send(byte[] message) {
    doSend(null, new Object[] {message});// FIXME
  }

  @Override
  public void send(InputStream message) throws IOException {
    try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
      copy(message, buffer);
      byte[] bytes = buffer.toByteArray();
      send(bytes);
    }
  }

  @Override
  public void send(Map<String, Object> message) {
    doSend(null, message);
  }

  @Override
  public void send(Serializable message) {
    doSend(null, message);
  }

  @Override
  public void send(String message) {
    doSend((String) null, message);
  }

  @Override
  public void send(String serialSchema, Object... messages) {
    doSend(serialSchema, messages);
  }

  protected void configure(JMSContext jmsc, JMSProducer producer) {
    producer.setDeliveryMode(deliveryMode);
    if (deliveryDelay > 0) {
      producer.setDeliveryDelay(deliveryDelay);
    }
    if (timeToLive > 0) {
      producer.setTimeToLive(timeToLive);
    }
    if (isNotEmpty(properties)) {
      properties.forEach(producer::setProperty);
    }
  }

  @SuppressWarnings("unchecked")
  protected void doSend(JMSContext jmsc, Destination d, JMSProducer p,
      MessageMarshaller marshaller, Object message) {
    try {
      if (marshaller != null) {
        p.send(d, marshaller.serialize(jmsc, message));
      } else if (message instanceof String) {
        p.send(d, (String) message);
      } else if (message instanceof Message) {
        p.send(d, (Message) message);
      } else if (message instanceof Map) {
        p.send(d, (Map<String, Object>) message);
      } else if (message instanceof byte[]) {
        p.send(d, (byte[]) message);
      } else if (message instanceof Serializable) {
        p.send(d, (Serializable) message);
      }
    } catch (Exception e) {
      throw new CorantRuntimeException(e);
    }
  }

  protected void doSend(JMSContext jmsc, String marshallerSchema, Object... messages) {
    if (isNotEmpty(messages)) {
      try {
        final MessageMarshaller serializer = marshaller(marshallerSchema);
        Destination d = multicast ? jmsc.createTopic(destination) : jmsc.createQueue(destination);
        JMSProducer p = jmsc.createProducer();
        configure(jmsc, p);
        for (Object message : messages) {
          doSend(jmsc, d, p, serializer, message);
        }
      } catch (Exception e) {
        throw new CorantRuntimeException(e);
      }
    }
  }

  protected void doSend(String marshallerSchema, Object... messages) {
    if (isNotEmpty(messages)) {
      final JMSContext jmsc = resolveApply(DefaultJMSContextService.class,
          b -> b.getJMSContext(connectionFactoryId, dupsOkAck));
      doSend(jmsc, marshallerSchema, messages);
    }
  }

  protected void doStreamingSend(JMSContext jmsc, Stream<?> messages, String marshallerSchema) {
    try {
      final MessageMarshaller serializer = marshaller(marshallerSchema);
      Destination d = multicast ? jmsc.createTopic(destination) : jmsc.createQueue(destination);
      JMSProducer p = jmsc.createProducer();
      configure(jmsc, p);
      messages.forEach(message -> doSend(jmsc, d, p, serializer, message));
    } catch (Exception e) {
      throw new CorantRuntimeException(e);
    }
  }

  protected MessageMarshaller marshaller(String marshallerSchema) {
    return findNamed(MessageMarshaller.class, marshallerSchema).orElse(null);
  }

}
