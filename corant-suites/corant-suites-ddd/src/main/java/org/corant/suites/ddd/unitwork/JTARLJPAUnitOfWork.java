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
package org.corant.suites.ddd.unitwork;

import static org.corant.shared.util.Empties.sizeOf;
import static org.corant.shared.util.Objects.defaultObject;
import java.util.LinkedList;
import javax.transaction.Transaction;
import org.corant.shared.exception.CorantRuntimeException;
import org.corant.suites.ddd.message.Message;
import org.corant.suites.ddd.message.MessageStorage;
import org.corant.suites.ddd.saga.SagaService;

/**
 * corant-suites-ddd
 *
 * <pre>
 * All entityManager from this unit of work are SynchronizationType.SYNCHRONIZED,
 * and must be in transactional.
 * </pre>
 *
 * @author bingo 上午11:38:39
 *
 */
public class JTARLJPAUnitOfWork extends AbstractJTAJPAUnitOfWork {

  protected final MessageStorage messageStorage;
  protected final SagaService sagaService; // FIXME Is it right to do so?
  protected final LinkedList<Message> stroagedMessages = new LinkedList<>();

  protected JTARLJPAUnitOfWork(JTARLJPAUnitOfWorksManager manager, Transaction transaction) {
    super(manager, transaction);
    messageStorage = manager.getMessageStorage();
    sagaService = defaultObject(manager.getSagaService(), SagaService::empty);
    messageDispatcher.prepare();
    messageStorage.prepare();
    sagaService.prepare();
    logger.fine(() -> String.format("Begin unit of work [%s].", transaction.toString()));
  }

  @Override
  public void complete(boolean success) {
    if (success) {
      int messageSize = sizeOf(stroagedMessages);
      messageDispatcher.accept(stroagedMessages.toArray(new Message[messageSize]));
    }
    super.complete(success);
  }

  @Override
  protected void clear() {
    try {
      stroagedMessages.clear();
    } finally {
      super.clear();
    }
  }

  @Override
  protected JTARLJPAUnitOfWorksManager getManager() {
    return (JTARLJPAUnitOfWorksManager) super.getManager();
  }

  @Override
  protected void handleMessage() {
    logger.fine(() -> String.format(
        "Sorted the flushed messages and trigger them if nessuary, store them to the message stroage, before %s completion.",
        transaction.toString()));
    LinkedList<Message> messages = new LinkedList<>();
    extractMessages(messages);
    int cycles = 128;
    while (!messages.isEmpty()) {
      Message msg = messages.pop();
      stroagedMessages.add(messageStorage.apply(msg));
      sagaService.trigger(msg);// FIXME Is it right to do so?
      if (extractMessages(messages) && --cycles < 0) {
        throw new CorantRuntimeException("Can not handle messages!");
      }
    }
  }

}