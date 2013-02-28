/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.transport.queue;

import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.ros.concurrent.CancellableLoop;
import org.ros.concurrent.MessageBlockingQueue;
import org.ros.concurrent.MessageBlockingQueueFactory;
import org.ros.internal.message.MessageBufferPool;
import org.ros.internal.message.MessageBuffers;
import org.ros.log.RosLogFactory;
import org.ros.message.MessageSerializer;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class OutgoingMessageQueue<T> {

  private static final Log log = RosLogFactory.getLog(OutgoingMessageQueue.class);

  private static final int DEQUE_CAPACITY = 512;

  private final MessageSerializer<T> serializer;
  private final MessageBlockingQueue<T> deque;
  private final ChannelGroup channelGroup;
  private final Writer writer;
  private final MessageBufferPool messageBufferPool;
  private final ChannelBuffer latchedBuffer;
  private final Object mutex;

  private boolean latchMode;
  private T latchedMessage;

  private final class Writer extends CancellableLoop {
	  
	@Override
	protected void setup() {
		Thread.currentThread().setName("ros-outgoing-writer-thread");
	}

	@Override
    public void loop() throws InterruptedException {
      T message = deque.take();
      final ChannelBuffer buffer = messageBufferPool.acquire();
      serializer.serialize(message, buffer);
      if (log.isDebugEnabled()) {
        log.debug(String.format("Writing %d bytes to %d channels.", buffer.readableBytes(),
            channelGroup.size()));
      }
      // Note that the buffer is automatically "duplicated" by Netty to avoid
      // race conditions. However, the duplicated buffer and the original buffer
      // share the same backing array. So, we have to wait until the write
      // operation is complete before returning the buffer to the pool.
      channelGroup.write(buffer).addListener(new ChannelGroupFutureListener() {
        @Override
        public void operationComplete(ChannelGroupFuture future) throws Exception {
          messageBufferPool.release(buffer);
        }
      });
    }
  }

  public OutgoingMessageQueue(MessageSerializer<T> serializer, ExecutorService executorService) {
    this.serializer = serializer;
    deque = MessageBlockingQueueFactory.newMessageBlockingQueue(DEQUE_CAPACITY, false);
    channelGroup = new DefaultChannelGroup();
    writer = new Writer();
    messageBufferPool = new MessageBufferPool();
    latchedBuffer = MessageBuffers.dynamicBuffer();
    mutex = new Object();
    latchMode = false;
    executorService.execute(writer);
  }

  public void setLatchMode(boolean enabled) {
    latchMode = enabled;
  }

  public boolean getLatchMode() {
    return latchMode;
  }

  /**
   * @param message
   *          the message to add to the queue
   */
  public void add(T message) {
    try {
		deque.put(message);
		setLatchedMessage(message);
	} catch (InterruptedException e) {
		// Fon't care
	}
  }

  private void setLatchedMessage(T message) {
    synchronized (mutex) {
      latchedMessage = message;
    }
  }

  /**
   * Stop writing messages and close all outgoing connections.
   */
  public void shutdown() {
    writer.cancel();
    channelGroup.close().awaitUninterruptibly();
  }

  /**
   * @param channel
   *          added to this {@link OutgoingMessageQueue}'s {@link ChannelGroup}
   */
  public void addChannel(Channel channel) {
    if (!writer.isRunning()) {
      log.warn("Failed to add channel. Cannot add channels after shutdown.");
      return;
    }
    if (latchMode && latchedMessage != null) {
      writeLatchedMessage(channel);
    }
    channelGroup.add(channel);
  }

  // TODO(damonkohler): Avoid re-serializing the latched message if it hasn't
  // changed.
  private void writeLatchedMessage(Channel channel) {
    synchronized (mutex) {
      latchedBuffer.clear();
      serializer.serialize(latchedMessage, latchedBuffer);
      channel.write(latchedBuffer);
    }
  }

  /**
   * @return the number of {@link Channel}s which have been added to this queue
   */
  public int getNumberOfChannels() {
    return channelGroup.size();
  }

  @VisibleForTesting
  public ChannelGroup getChannelGroup() {
    return channelGroup;
  }
}
