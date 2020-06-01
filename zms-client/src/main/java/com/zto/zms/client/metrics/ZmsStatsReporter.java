/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zto.zms.client.metrics;

import com.zto.zms.client.consumer.Consumer;
import com.zto.zms.client.consumer.ConsumerFactory;
import com.zto.zms.client.producer.Producer;
import com.zto.zms.client.producer.ProducerFactory;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.utils.ExecutorServiceUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class ZmsStatsReporter implements Runnable {
	private final ScheduledExecutorService executor;

	private volatile boolean running = false;

	public ZmsStatsReporter() {
		this.executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
			AtomicLong threadIndex = new AtomicLong(0);
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "ZmsConnectionManager-JmxReporter-" + threadIndex.incrementAndGet());
				t.setUncaughtExceptionHandler((t1, e) -> ZmsLogger.log.error("uncaughtException in thread: {}", t1.getName(), e));
				return t;
			}
		});
	}

	public void start(long period, TimeUnit unit) {
		running = true;
		executor.scheduleWithFixedDelay(this, period, period, unit);
	}

	public void shutdown() {
		running = false;
		ExecutorServiceUtils.gracefullyShutdown(executor);
	}

	@Override
	public void run() {
		if (running) {
			for (Producer producer : ProducerFactory.getProducers()) {
				producer.statistics();
			}
			for (Consumer consumer : ConsumerFactory.getConsumers()) {
				consumer.statistics();
			}
		}
	}
}

