package io.dargenn.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import io.dargenn.producer.model.Stats;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
@RequiredArgsConstructor
public class Producer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private Runtime runtime = Runtime.getRuntime();

    @Scheduled(fixedDelay = 2000)
    public void produce() throws JsonProcessingException {
        Stats stats = getStats();
        String json = new ObjectMapper().writeValueAsString(stats);
        System.out.println("Producing message: " + json);
        kafkaTemplate.send("stats-input", json);
    }

    private Stats getStats() {
        double cpuLoad = osBean.getSystemCpuLoad();
        double memUsage = (double) (osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / osBean.getTotalPhysicalMemorySize();
        return new Stats(cpuLoad, memUsage);
    }
}
