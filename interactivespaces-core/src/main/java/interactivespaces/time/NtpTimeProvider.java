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

package interactivespaces.time;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.ros.math.CollectionMath;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * A {@link TimeProvider} which uses NTP.
 * 
 * @author Keith M. Hughes
 */
public class NtpTimeProvider implements TimeProvider {

	/**
	 * The number of time samples.
	 */
	private static final int SAMPLE_SIZE = 11;

	/**
	 * The host for the NTP server.
	 */
	private final InetAddress host;

	/**
	 * The threadpool
	 */
	private final ScheduledExecutorService scheduledExecutorService;

	/**
	 * A local provider of time.
	 */
	private final LocalTimeProvider localTimeProvider;

	/**
	 * The NTP client
	 */
	private final NTPUDPClient ntpClient;

	/**
	 * The time drift offset
	 */
	private AtomicLong offset;

	/**
	 * Update period for the time provider
	 */
	private long updatePeriod;

	/**
	 * Time unit for the update period for the time provider.
	 */
	private TimeUnit updatePeriodTimeUnit;

	/**
	 * The thread reading the NTP client.
	 */
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * Logging for provider.
	 */
	private Log log;

	/**
	 * @param host
	 *            the NTP host to use
	 * @param updatePeriod
	 *            how often the time should be updated from NTP
	 * @param updatePeriodTimeUnit
	 *            time units for the update period
	 * @param scheduledExecutorService
	 *            thread pool to use
	 * @param log
	 *            logger for the provider
	 */
	public NtpTimeProvider(InetAddress host, long updatePeriod,
			TimeUnit updatePeriodTimeUnit,
			ScheduledExecutorService scheduledExecutorService, Log log) {
		this.host = host;
		this.scheduledExecutorService = scheduledExecutorService;
		this.log = log;

		localTimeProvider = new LocalTimeProvider();
		ntpClient = new NTPUDPClient();
		offset = new AtomicLong(0);
		scheduledFuture = null;
	}

	/**
	 * Update the current time offset from the configured NTP host.
	 * 
	 * @throws IOException
	 */
	public void updateTime() throws IOException {
		List<Long> offsets = Lists.newArrayList();
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			offsets.add(computeOffset());
		}
		offset.set(CollectionMath.median(offsets));

		if (log.isDebugEnabled()) {
			log.debug(String.format("NTP time offset: %d ms", offset));
		}
	}

	private long computeOffset() throws IOException {
		log.info("Updating time offset from NTP server: " + host.getHostName());
		try {
			TimeInfo time = ntpClient.getTime(host);
			time.computeDetails();

			return time.getOffset();
		} catch (IOException e) {
			log.error(
					"Failed to read time from NTP server: "
							+ host.getHostName(), e);
			throw e;
		}
	}

	@Override
	public void startup() {
		// Starts periodically updating the current time offset periodically.
		//
		// The first time update happens immediately.
		//
		// Note that errors thrown while periodically updating time will be
		// logged but not rethrown.
		scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(
				new Runnable() {
					@Override
					public void run() {
						try {
							updateTime();
						} catch (IOException e) {
							log.error("Periodic NTP update failed.", e);
						}
					}
				}, 0, updatePeriod, updatePeriodTimeUnit);
	}

	@Override
	public void shutdown() {
		Preconditions.checkNotNull(scheduledFuture);
		scheduledFuture.cancel(true);
		scheduledFuture = null;
	}

	@Override
	public long getCurrentTime() {
		long currentTime = localTimeProvider.getCurrentTime();
		return currentTime + offset.get();
	}
}
