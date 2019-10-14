package io.reactive.common.util;

import com.google.common.collect.*;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @author Denis Gabaydulin
 * @since 13/11/2015
 */
public class GuavaCollectors {
    private GuavaCollectors() {
    }

    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
        Supplier<ImmutableList.Builder<T>> supplier = ImmutableList.Builder::new;
        BiConsumer<ImmutableList.Builder<T>, T> accumulator = ImmutableList.Builder::add;
        BinaryOperator<ImmutableList.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
        Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher = ImmutableList.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
        Supplier<ImmutableSet.Builder<T>> supplier = ImmutableSet.Builder::new;
        BiConsumer<ImmutableSet.Builder<T>, T> accumulator = ImmutableSet.Builder::add;
        BinaryOperator<ImmutableSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
        Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher = ImmutableSet.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    public static <T> Collector<T, ?, ImmutableSortedSet<T>> toImmutableSortedSet(Comparator<T> comparator) {
        Supplier<ImmutableSortedSet.Builder<T>> supplier = () -> new ImmutableSortedSet.Builder<>(comparator);
        BiConsumer<ImmutableSortedSet.Builder<T>, T> accumulator = ImmutableSortedSet.Builder::add;
        BinaryOperator<ImmutableSortedSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
        Function<ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>> finisher = ImmutableSortedSet.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        Supplier<ImmutableMap.Builder<K, V>> supplier = ImmutableMap.Builder::new;
        BiConsumer<ImmutableMap.Builder<K, V>, T> accumulator = (b, t) -> b.put(keyMapper.apply(t), valueMapper.apply(t));
        BinaryOperator<ImmutableMap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());
        Function<ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> finisher = ImmutableMap.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    public static <T, K, V> Collector<T, ?, ImmutableMultimap<K, V>> toImmutableMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        Supplier<ImmutableMultimap.Builder<K, V>> supplier = ImmutableMultimap.Builder::new;
        BiConsumer<ImmutableMultimap.Builder<K, V>, T> accumulator = (b, t) -> b.put(keyMapper.apply(t), valueMapper.apply(t));
        BinaryOperator<ImmutableMultimap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());
        Function<ImmutableMultimap.Builder<K, V>, ImmutableMultimap<K, V>> finisher = ImmutableMultimap.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> toImmutableSetMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        Supplier<ImmutableSetMultimap.Builder<K, V>> supplier = ImmutableSetMultimap.Builder::new;
        BiConsumer<ImmutableSetMultimap.Builder<K, V>, T> accumulator = (b, t) -> b.put(keyMapper.apply(t), valueMapper.apply(t));
        BinaryOperator<ImmutableSetMultimap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());
        Function<ImmutableSetMultimap.Builder<K, V>, ImmutableSetMultimap<K, V>> finisher = ImmutableSetMultimap.Builder::build;

        return Collector.of(supplier, accumulator, combiner, finisher);
    }


    public static <T, K> Collector<T, ?, ImmutableMultimap<K, T>> toImmutableMultimap(
            Function<? super T, ? extends K> keyMapper
    ) {
        return toImmutableMultimap(keyMapper, Function.<T>identity());
    }
}
