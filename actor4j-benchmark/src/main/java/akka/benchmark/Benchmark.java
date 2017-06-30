/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package akka.benchmark;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import actor4j.benchmark.utils.MessageThroughputMeasurement;
import akka.actor.ActorSystem;

public class Benchmark {
	protected ActorSystem system;
	protected Supplier<Long> counter;
	protected long duration;
	protected long warmupIterations;
	
	public Benchmark(ActorSystem system, Supplier<Long> counter, long duration) {
		this(system, counter, 10, duration);
	}
	
	public Benchmark(ActorSystem system, Supplier<Long> counter, long warmupIterations, long duration) {
		super();
		
		this.system = system;
		this.counter = counter;
		this.warmupIterations = warmupIterations;
		this.duration = duration;
	}
	
	@SuppressWarnings("deprecation")
	public void start() {
		DescriptiveStatistics statistics = new DescriptiveStatistics();
		AtomicLong warmupCount = new AtomicLong();
		DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###");
		
		System.out.printf("Benchmark started (%s)...%n", system.name());

		MessageThroughputMeasurement messageTM = new MessageThroughputMeasurement(counter, warmupIterations, warmupCount, statistics, true);
		messageTM.start();
		
		try {
			Thread.sleep(duration+warmupIterations*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		messageTM.stop();
		system.shutdown();
		system.awaitTermination();
		
		System.out.printf("statistics::count         : %s%n", decimalFormat.format(counter.get()-warmupCount.get()));
		System.out.printf("statistics::mean::derived : %s msg/s%n", decimalFormat.format((counter.get()-warmupCount.get())/(duration/1000)));
		System.out.printf("statistics::mean          : %s msg/s%n", decimalFormat.format(statistics.getMean()));
		System.out.printf("statistics::sd            : %s msg/s%n", decimalFormat.format(statistics.getStandardDeviation()));
		System.out.printf("statistics::median        : %s msg/s%n", decimalFormat.format(statistics.getPercentile(50)));
		System.out.println("Benchmark finished...");
			
	}
}