package io.dargenn.streams;

import io.dargenn.streams.model.Stats;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Reducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.Properties;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class StreamsConfiguration {
    @Bean
    public KStream<String, Stats> kStreamJson(StreamsBuilder builder) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stats-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.99.100:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, new JsonSerde<>(Stats.class).getClass());

        KStream<String, Stats> stream = builder.stream("stats-input", Consumed.with(Serdes.String(), new JsonSerde<>(Stats.class)))
                .filter((s, stats) -> stats.getCpuUsage() > 0.8);
        stream.to("stats-output");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

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
