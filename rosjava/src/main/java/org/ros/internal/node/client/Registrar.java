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

package org.ros.internal.node.client;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.ros.concurrent.Holder;
import org.ros.concurrent.RetryingExecutorService;
import org.ros.exception.RosRuntimeException;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.NodeIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.server.master.MasterServer;
import org.ros.internal.node.service.DefaultServiceServer;
import org.ros.internal.node.service.ServiceManagerListener;
import org.ros.internal.node.topic.DefaultPublisher;
import org.ros.internal.node.topic.DefaultSubscriber;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.TopicManagerListener;

import com.google.common.base.Preconditions;

/**
 * Manages topic, and service registrations of a {@link SlaveServer} with the
 * {@link MasterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Registrar implements TopicManagerListener, ServiceManagerListener {

	private static final boolean DEBUG = false;
	private final Log log /* = LogFactory.getLog(Registrar.class) */;

	private static final int SHUTDOWN_TIMEOUT = 5;
	private static final TimeUnit SHUTDOWN_TIMEOUT_UNITS = TimeUnit.SECONDS;

	private final MasterClient masterClient;
	private final ScheduledExecutorService executorService;
	private final RetryingExecutorService retryingExecutorService;

	private NodeIdentifier nodeIdentifier;
	private boolean running;

	/**
	 * @param masterClient
	 *            a {@link MasterClient} for communicating with the ROS master
	 * @param executorService
	 *            a {@link ScheduledExecutorService} to be used for all
	 *            asynchronous operations
	 */
	public Registrar(MasterClient masterClient,
			ScheduledExecutorService executorService, Log log) {
		this.masterClient = masterClient;
		this.executorService = executorService;
		this.log = log;
		retryingExecutorService = new RetryingExecutorService(executorService);
		nodeIdentifier = null;
		running = false;
		if (log != null && log.isDebugEnabled()) {
			log.debug("MasterXmlRpcEndpoint URI: "
					+ masterClient.getRemoteUri());
		}
	}

	/**
	 * Failed registration actions are retried periodically until they succeed.
	 * This method adjusts the delay between successive retry attempts for any
	 * particular registration action.
	 * 
	 * @param delay
	 *            the delay in units of {@code unit} between retries
	 * @param unit
	 *            the unit of {@code delay}
	 */
	public void setRetryDelay(long delay, TimeUnit unit) {
		retryingExecutorService.setRetryDelay(delay, unit);
	}

	private boolean submit(Callable<Boolean> callable) {
		if (running) {
			retryingExecutorService.submit(callable);
			return true;
		}
		log.warn("Registrar no longer running, request ignored.");
		return false;
	}

	private <T> boolean callMaster(Callable<Response<T>> callable) {
		Preconditions.checkNotNull(nodeIdentifier, "Registrar not started.");
		boolean success;
		try {
			Response<T> response = callable.call();
			if (DEBUG) {
				log.info(response);
			}
			success = response.isSuccess();
		} catch (Exception e) {
			if (DEBUG) {
				log.error("Exception caught while communicating with master.",
						e);
			} else {
				log.error("Exception caught while communicating with master.");
			}
			success = false;
		}
		return success;
	}

	@Override
	public void onPublisherAdded(final DefaultPublisher<?> publisher) {
		registerPublisher(publisher);
	}

	/**
	 * Register the given publisher with the master.
	 * 
	 * @param publisher
	 *            the publisher to register
	 */
	public void registerPublisher(final DefaultPublisher<?> publisher) {
		if (log != null && log.isDebugEnabled()) {
			log.debug("Registering publisher: " + publisher);
		}
		boolean submitted = submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean success = callMaster(new Callable<Response<List<URI>>>() {
					@Override
					public Response<List<URI>> call() throws Exception {
						if (log != null && log.isDebugEnabled()) {
							log.debug(String
									.format("Master client registering publisher %s with master",
											publisher.getTopicName()));
						}
						Response<List<URI>> registerPublisher = masterClient
								.registerPublisher(publisher.toDefinition());
						if (log != null && log.isDebugEnabled()) {
							log.debug(String
									.format("Master client received response for registering publisher %s with master",
											publisher.getTopicName()));
						}
						return registerPublisher;
					}
				});
				if (success) {
					publisher.signalOnMasterRegistrationSuccess();
				} else {
					publisher.signalOnMasterRegistrationFailure();
				}
				return !success;
			}
		});
		if (!submitted) {
			if (log != null && log.isDebugEnabled()) {
				log.debug(String
						.format("Master client call not sent for registering subscriber %s with master",
								publisher.getTopicName()));
			}
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					publisher.signalOnMasterRegistrationFailure();
				}
			});
		}
		if (log != null && log.isDebugEnabled()) {
			log.debug("Done registering publisher: " + publisher);
		}
	}

	@Override
	public void onPublisherRemoved(final DefaultPublisher<?> publisher) {
		if (log != null && log.isDebugEnabled()) {
			log.debug("Unregistering publisher: " + publisher);
		}
		boolean submitted = submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean success = callMaster(new Callable<Response<Integer>>() {
					@Override
					public Response<Integer> call() throws Exception {
						if (log != null && log.isDebugEnabled()) {
							log.debug(String
									.format("Master client unregistering publisher %s with master",
											publisher.getTopicName()));
						}
						Response<Integer> unregisterPublisher = masterClient
								.unregisterPublisher(publisher.toIdentifier());
						if (log != null && log.isDebugEnabled()) {
							log.debug(String
									.format("Master client received response for unregistering publisher %s with master",
											publisher.getTopicName()));
						}
						return unregisterPublisher;
					}
				});
				if (success) {
					publisher.signalOnMasterUnregistrationSuccess();
				} else {
					publisher.signalOnMasterUnregistrationFailure();
				}
				return !success;
			}
		});
		if (!submitted) {
			if (log != null && log.isDebugEnabled()) {
				log.debug(String
						.format("Master client call not sent for unregistering publisher %s with master",
								publisher.getTopicName()));
			}
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					publisher.signalOnMasterUnregistrationFailure();
				}
			});
		}
		if (log != null && log.isDebugEnabled()) {
			log.debug("Done unregistering publisher: " + publisher);
		}
	}

	@Override
	public void onSubscriberAdded(final DefaultSubscriber<?> subscriber) {
		registerSubscriber(subscriber);
	}

	/**
	 * Register a subscriber with the master.
	 * 
	 * @param subscriber
	 */
	public void registerSubscriber(final DefaultSubscriber<?> subscriber) {
		if (log != null && log.isDebugEnabled()) {
			log.debug("Registering subscriber: " + subscriber);
		}
		boolean submitted = submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				final Holder<Response<List<URI>>> holder = Holder.newEmpty();
				boolean success = callMaster(new Callable<Response<List<URI>>>() {
					@Override
					public Response<List<URI>> call() throws Exception {
						if (log != null && log.isDebugEnabled()) {
							log.debug(String
									.format("Master client registering subscriber %s with master",
											subscriber.getTopicName()));
						}
						Response<List<URI>> set = holder
								.set(masterClient.registerSubscriber(
										nodeIdentifier, subscriber));
						if (log != null && log.isDebugEnabled()) {
							log.debug(String
									.format("Master client received response for registering subscriber %s with master",
											subscriber.getTopicName()));
						}
						return set;
					}
				});
				if (success) {
					Collection<PublisherIdentifier> publishers = PublisherIdentifier
							.newCollectionFromUris(holder.get().getResult(),
									subscriber.getTopicDefinition());
					subscriber.updatePublishers(publishers);
					subscriber.signalOnMasterRegistrationSuccess();
				} else {
					subscriber.signalOnMasterRegistrationFailure();
				}
				return !success;
			}
		});
		if (!submitted) {
			if (log != null && log.isDebugEnabled()) {
				log.debug(String
						.format("Master client call not sent for registering subscriber %s with master",
								subscriber.getTopicName()));
			}
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					subscriber.signalOnMasterRegistrationFailure();
				}
			});
		}
		if (log != null && log.isDebugEnabled()) {
			log.debug("Done registering subscriber: " + subscriber);
		}
	}

	@Override
	public void onSubscriberRemoved(final DefaultSubscriber<?> subscriber) {
		if (log != null && log.isDebugEnabled()) {
			log.debug("Unregistering subscriber: " + subscriber);
		}
		boolean submitted = submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean success = callMaster(new Callable<Response<Integer>>() {
					@Override
					public Response<Integer> call() throws Exception {
						if (log != null && log.isDebugEnabled()) {
							log.debug(String
									.format("Master client unregistering subscriber %s with master",
											subscriber.getTopicName()));
						}
						Response<Integer> unregisterSubscriber = masterClient
								.unregisterSubscriber(nodeIdentifier,
										subscriber);
						if (log != null && log.isDebugEnabled()) {
							log.debug(String
									.format("Master client received response for unregistering subscriber %s with master",
											subscriber.getTopicName()));
						}
						return unregisterSubscriber;
					}
				});
				if (success) {
					subscriber.signalOnMasterUnregistrationSuccess();
				} else {
					subscriber.signalOnMasterUnregistrationFailure();
				}
				return !success;
			}
		});
		if (!submitted) {
			if (log != null && log.isDebugEnabled()) {
				log.debug(String
						.format("Master client call not sent for unregistering subscriber %s with master",
								subscriber.getTopicName()));
			}
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					subscriber.signalOnMasterUnregistrationFailure();
				}
			});
		}
		if (log != null && log.isDebugEnabled()) {
			log.debug("Done unregistering subscriber: " + subscriber);
		}
	}

	@Override
	public void onServiceServerAdded(
			final DefaultServiceServer<?, ?> serviceServer) {
		if (DEBUG) {
			log.info("Registering service: " + serviceServer);
		}
		boolean submitted = submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean success = callMaster(new Callable<Response<Void>>() {
					@Override
					public Response<Void> call() throws Exception {
						return masterClient.registerService(nodeIdentifier,
								serviceServer);
					}
				});
				if (success) {
					serviceServer.signalOnMasterRegistrationSuccess();
				} else {
					serviceServer.signalOnMasterRegistrationFailure();
				}
				return !success;
			}
		});
		if (!submitted) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					serviceServer.signalOnMasterRegistrationFailure();
				}
			});
		}
	}

	@Override
	public void onServiceServerRemoved(
			final DefaultServiceServer<?, ?> serviceServer) {
		if (DEBUG) {
			log.info("Unregistering service: " + serviceServer);
		}
		boolean submitted = submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean success = callMaster(new Callable<Response<Integer>>() {
					@Override
					public Response<Integer> call() throws Exception {
						return masterClient.unregisterService(nodeIdentifier,
								serviceServer);
					}
				});
				if (success) {
					serviceServer.signalOnMasterUnregistrationSuccess();
				} else {
					serviceServer.signalOnMasterUnregistrationFailure();
				}
				return !success;
			}
		});
		if (!submitted) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					serviceServer.signalOnMasterUnregistrationFailure();
				}
			});
		}
	}

	/**
	 * Starts the {@link Registrar} for the {@link SlaveServer} identified by
	 * the given {@link NodeIdentifier}.
	 * 
	 * @param nodeIdentifier
	 *            the {@link NodeIdentifier} for the {@link SlaveServer} this
	 *            {@link Registrar} is responsible for
	 */
	public void start(NodeIdentifier nodeIdentifier) {
		Preconditions.checkNotNull(nodeIdentifier);
		Preconditions.checkState(this.nodeIdentifier == null,
				"Registrar already started.");
		this.nodeIdentifier = nodeIdentifier;
		running = true;
	}

	/**
	 * Shuts down the {@link Registrar}.
	 * 
	 * <p>
	 * No further registration requests will be accepted. All queued
	 * registration jobs have up to {@link #SHUTDOWN_TIMEOUT}
	 * {@link #SHUTDOWN_TIMEOUT_UNITS} to complete before being canceled.
	 * 
	 * <p>
	 * Calling {@link #shutdown()} more than once has no effect.
	 */
	public void shutdown() {
		if (!running) {
			return;
		}
		running = false;
		try {
			retryingExecutorService.shutdown(SHUTDOWN_TIMEOUT,
					SHUTDOWN_TIMEOUT_UNITS);
		} catch (InterruptedException e) {
			throw new RosRuntimeException(e);
		}
	}
}
