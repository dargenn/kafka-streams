package io.dargenn.streams;

import io.dargenn.streams.model.Stats;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class StreamsConfiguration {
    @Bean
    public KStream<String, Stats> kStreamJson(StreamsBuilder builder) {
        KStream<String, Stats> stream = builder.stream("stats-input", Consumed.with(Serdes.String(), new JsonSerde<>(Stats.class)));

        KTable<String, Stats> combinedDocuments = stream
                .map(new TestKeyValueMapper())
                .groupByKey()
                .reduce(new TestReducer(), Materialized.<String, Stats, KeyValueStore<Bytes, byte[]>>as("streams-json-store"));

        combinedDocuments.toStream().to("stats-output", Produced.with(Serdes.String(), new JsonSerde<>(Stats.class)));

        System.out.println("OUTPUT PROCESSED");

        return stream;
    }

    public static class TestKeyValueMapper implements KeyValueMapper<String, Stats, KeyValue<String, Stats>> {

        @Override
        public KeyValue<String, Stats> apply(String key, Stats value) {
            return new KeyValue<>(key, value);
        }

    }

    public static class TestReducer implements Reducer<Stats> {

        @Override
        public Stats apply(Stats value1, Stats value2) {
            double avgCpuLoad = (value1.getCpuUsage() + value2.getCpuUsage()) / 2;
            double avgMemLoad = (value1.getMemUsage() + value2.getMemUsage()) / 2;
            return new Stats(avgCpuLoad, avgMemLoad);
        }

    }
}
