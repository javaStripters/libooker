package ru.thecntgfy.libooker.utils;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public record TimeRange(LocalTime from, LocalTime toInclusive) implements Comparable<TimeRange> {
    public static List<TimeRange> ranges(LocalTime from, LocalTime toInclusive, Duration step) {
        LocalTime currFrom = from;
        LocalTime currTo  = from.plus(step);
        List<TimeRange> ranges = new ArrayList<>();

        while (currTo.compareTo(toInclusive) <= 0) {
            ranges.add(new TimeRange(currFrom, currTo));
            currFrom = currTo;
            currTo = currTo.plus(step);
        }

        return ranges;
    }

    public static TimeRange max(TimeRange first, TimeRange second) {
        return first.compareTo(second) >= 0 ? first : second;
    }

    public static TimeRange min(TimeRange first, TimeRange second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    public boolean includes(LocalTime time) {
        return (from.isBefore(time) || from.equals(time)) && (toInclusive.isAfter(time) || toInclusive.equals(time));
    }

    public boolean includes(TimeRange other) {
        return includes(other.from) && includes(other.toInclusive);
    }

    public Pair<TimeRange> differenceInside(TimeRange other) {
        if (!includes(other))
            throw new IllegalArgumentException("this must include other!");

        TimeRange left = new TimeRange(from, other.from);
        TimeRange right = new TimeRange(other.toInclusive, toInclusive);
        return new Pair<>(left, right);
    }

    public Stream<TimeRange> split(Duration duration) {
        List<TimeRange> ranges = new ArrayList<>();

        LocalTime fromSplitted = from;
        LocalTime toSplitted = from.plus(duration);

        while (toSplitted.compareTo(toInclusive) <= 0) {
            ranges.add(new TimeRange(fromSplitted, toSplitted));
            fromSplitted = toSplitted;
            toSplitted = toSplitted.plus(duration);
        }

        return ranges.stream();
    }

    public Optional<TimeRange> conjunction(TimeRange other) {
        if (!doesInterfere(other))
            return Optional.empty();

        LocalTime min = from.compareTo(other.from) <= 0 ? from : other.from;
        LocalTime max = toInclusive.compareTo(other.toInclusive) >= 0 ? toInclusive : other.toInclusive;

        return Optional.of(new TimeRange(min, max));
    }

    //TODO: Rework
    public boolean doesInterfere(TimeRange other) {
        return ((toInclusive.equals(other.toInclusive) || toInclusive.isAfter(other.toInclusive))
                        && (from.equals(other.toInclusive) || from.isBefore(other.toInclusive)))
                ||
                ((toInclusive.equals(other.from) || toInclusive.isAfter(other.from))
                        && (from.equals(other.from) || from.isBefore(other.from)));
    }

    public boolean doesInterfereExclusive(TimeRange other) {
        return (toInclusive.isAfter(other.toInclusive) && from.isBefore(other.toInclusive))
                ||
                ((toInclusive.isAfter(other.from)) && from.isBefore(other.from));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeRange timeRange)) return false;
        return Objects.equals(from, timeRange.from) && Objects.equals(toInclusive, timeRange.toInclusive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, toInclusive);
    }

    @Override
    public int compareTo(TimeRange o) {
        if (from.equals(o.from))
            return toInclusive.compareTo(o.toInclusive);
        return from.compareTo(o.from);
    }
}
