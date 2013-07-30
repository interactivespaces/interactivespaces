/*
 * Copyright (C) 2012 Google Inc.
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

package org.ros.zeroconf.common.selector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.ros.exception.RosRuntimeException;
import org.ros.zeroconf.common.ZeroconfServiceInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ZeroconfServiceSelector} which is for lightweight services.
 *
 * <p>
 * Lightweight means that there are few providers for the service that change
 * infrequently.
 *
 * @author Keith M. Hughes
 */
public class LightweightZeroconfServiceSelector<T extends ZeroconfServiceInfo> implements
    ZeroconfServiceSelector<T> {

  /**
   * Services stored by priority.
   */
  private Map<Integer, Services<T>> servicesByPriority = Maps.newHashMap();

  /**
   * The services ordered by priority.
   */
  private PriorityQueue<Services<T>> priorityQueue;

  /**
   * A list of requests for a service which have been unfulfilled.
   */
  private List<ZeroconfServiceRequest<T>> pending = Lists.newArrayList();

  public LightweightZeroconfServiceSelector() {
    priorityQueue = new PriorityQueue<Services<T>>(10, new Comparator<Services<T>>() {
      @Override
      public int compare(Services<T> o1, Services<T> o2) {
        return o1.getPriority() - o2.getPriority();
      }
    });
  }

  @Override
  public synchronized void addService(T serviceInfo) {
    int priority = serviceInfo.getPriority();
    Services<T> services = servicesByPriority.get(priority);
    if (services == null) {
      services = new Services<T>(priority);
      servicesByPriority.put(priority, services);
      priorityQueue.offer(services);
    }

    services.addService(serviceInfo);

    // If pending wasn't empty, then we have no other service other than
    // what was given just now.
    if (!pending.isEmpty()) {
      for (ZeroconfServiceRequest<T> request : pending) {
        request.setService(serviceInfo);
      }
      pending.clear();
    }
  }

  @Override
  public synchronized void removeService(T serviceInfo) {
    Services<T> services = servicesByPriority.get(serviceInfo.getPriority());
    if (services != null) {
      services.removeService(serviceInfo);

      if (services.isEmpty()) {
        servicesByPriority.remove(serviceInfo.getPriority());
        priorityQueue.remove(services);
      }
    }
  }

  @Override
  public synchronized T selectService() {
    Services<T> services = priorityQueue.peek();
    if (services != null) {
      return services.selectService();
    } else {
      throw new RosRuntimeException("No zeroconf RoS services registered");
    }
  }

  @Override
  public synchronized T getService() {
    try {
      if (areServicesAvailable()) {
        return selectService();
      } else {
        return newServiceRequest().getService();
      }
    } catch (InterruptedException e) {
      throw new RosRuntimeException("Interrupted while getting a zeroconf service record");
    }
  }

  @Override
  public T getService(long timeout, TimeUnit unit) {
    try {
      if (areServicesAvailable()) {
        return selectService();
      } else {
        return newServiceRequest().getService(timeout, unit);
      }
    } catch (InterruptedException e) {
      throw new RosRuntimeException("Interrupted while getting a zeroconf service record");
    }
  }

  /**
   * Create and queue a new service request.
   *
   * @return the queued service request
   */
  private ZeroconfServiceRequest<T> newServiceRequest() {
    ZeroconfServiceRequest<T> request = new ZeroconfServiceRequest<T>();
    pending.add(request);
    return request;
  }

  @Override
  public synchronized boolean areServicesAvailable() {
    return !priorityQueue.isEmpty();
  }

  /**
   * A set of {@info ZeroconfServiceInfo} objects of a given priority.
   *
   * @author Keith M. Hughes
   */
  private static class Services<T extends ZeroconfServiceInfo> {

    /**
     * Random number generator for this class.
     */
    private Random random = new Random(System.currentTimeMillis());

    /**
     * The priority
     */
    private int priority;

    /**
     * All services in the collection.
     */
    private List<T> services = Lists.newArrayList();

    /**
     * The total weight of all services in the collection.
     */
    private int totalWeight = 0;

    public Services(int priority) {
      this.priority = priority;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
      return priority;
    }

    /**
     * Add the service into the collection.
     *
     * <p>
     * If the service was there already, it will be replaced with the new one
     * just in case some values have changed.
     *
     * @param serviceInfo
     *          the service to add.
     */
    public void addService(T serviceInfo) {
      // It could be considered awful to use an arraylist for this, but
      // there will never be so many services moving in and out of service
      // that this matters and using an arraylist makes the selection
      // process
      int i = services.indexOf(serviceInfo);
      if (i != -1) {
        T oldInfo = services.get(i);
        services.set(i, serviceInfo);
        totalWeight += serviceInfo.getWeight() - oldInfo.getWeight();
      } else {
        services.add(serviceInfo);
        totalWeight += serviceInfo.getWeight();
      }
    }

    /**
     * Remove the service from the collection.
     *
     * <p>
     * Does nothing if the service wasn't there.
     *
     * @param serviceInfo
     *          the service to add.
     */
    public void removeService(T serviceInfo) {
      services.remove(serviceInfo);
    }

    /**
     * Randomly select a service.
     *
     * @return the selected service
     */
    public T selectService() {
      int value = random.nextInt(totalWeight);

      long current = 0;
      for (T service : services) {
        current += service.getWeight();
        if (value < current) {
          return service;
        }
      }

      throw new RosRuntimeException("No service selected");
    }

    /**
     * Is the collection empty?
     *
     * @return {@code true} if there are no services.
     */
    public boolean isEmpty() {
      return services.isEmpty();
    }
  }
}
