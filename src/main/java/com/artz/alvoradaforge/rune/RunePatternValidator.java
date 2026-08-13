package com.artz.alvoradaforge.rune;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RunePatternValidator {
    public static final int MAX_TRACE_POINTS = 512;
    public static final int MAX_PACKED_TRACE_BYTES = MAX_TRACE_POINTS * 2;
    private static final int SAMPLE_COUNT = 64;

    private RunePatternValidator() {
    }

    public static List<Point> pattern(RuneType type) {
        return switch (type.family()) {
            case EMBER -> emberPattern(type.tier());
            case TIDE -> tidePattern(type.tier());
            case VERDANT -> verdantPattern(type.tier());
            case VOID -> voidPattern(type.tier());
        };
    }

    public static Result validate(RuneType type, byte[] packedPoints) {
        if (packedPoints.length < 16
                || packedPoints.length > MAX_PACKED_TRACE_BYTES
                || (packedPoints.length & 1) != 0) {
            return new Result(false, 0);
        }

        List<Point> drawn = new ArrayList<>();
        int minX = 255;
        int minY = 255;
        int maxX = 0;
        int maxY = 0;
        for (int i = 0; i < packedPoints.length; i += 2) {
            int x = Byte.toUnsignedInt(packedPoints[i]);
            int y = Byte.toUnsignedInt(packedPoints[i + 1]);
            drawn.add(new Point(x, y));
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        if (maxX - minX < 105 || maxY - minY < 105 || pathLength(drawn) < 260.0) {
            return new Result(false, 0);
        }

        List<Point> expectedSamples = resample(pattern(type), SAMPLE_COUNT);
        List<Point> drawnSamples = resample(drawn, SAMPLE_COUNT);
        double forward = averageDistance(expectedSamples, drawnSamples);
        Collections.reverse(drawnSamples);
        double reverse = averageDistance(expectedSamples, drawnSamples);
        double distance = Math.min(forward, reverse);
        int accuracy = (int)Math.round(Math.max(0.0, Math.min(100.0, 100.0 - distance * 1.8)));
        return new Result(distance <= type.passingDistance(), accuracy);
    }

    private static List<Point> emberPattern(int tier) {
        int turns = 5 + tier;
        int amplitude = 60 + tier * 2;
        List<Point> result = new ArrayList<>(turns * 2 + 3);
        result.add(new Point(128, 232));
        for (int step = 0; step <= turns; step++) {
            double progress = step / (double) turns;
            int x = 128 + (int)Math.round(Math.sin(step * 1.72 + tier * 0.31) * amplitude);
            int y = 218 - (int)Math.round(progress * 196.0);
            result.add(new Point(clampCoordinate(x), y));
        }
        for (int step = turns - 1; step >= 0; step--) {
            double progress = step / (double) turns;
            int x = 128 - (int)Math.round(Math.sin(step * 1.72 + tier * 0.31) * amplitude * 0.72);
            int y = 218 - (int)Math.round(progress * 196.0);
            result.add(new Point(clampCoordinate(x), y));
        }
        return List.copyOf(result);
    }

    private static List<Point> tidePattern(int tier) {
        int waves = 2 + (tier + 1) / 2;
        int samples = 20 + tier * 4;
        int amplitude = 58 + tier * 2;
        List<Point> result = new ArrayList<>(samples + 1);
        for (int step = 0; step <= samples; step++) {
            double progress = step / (double) samples;
            int x = 18 + (int)Math.round(progress * 220.0);
            int y = 128 + (int)Math.round(Math.sin(progress * Math.PI * 2.0 * waves + tier * 0.23) * amplitude);
            result.add(new Point(x, clampCoordinate(y)));
        }
        return List.copyOf(result);
    }

    private static List<Point> verdantPattern(int tier) {
        int petals = 2 + tier;
        int samples = petals * 10;
        List<Point> result = new ArrayList<>(samples + 1);
        for (int step = 0; step <= samples; step++) {
            double angle = Math.PI * 2.0 * step / samples;
            double radius = 57.0 + 49.0 * Math.cos(petals * angle);
            int x = 128 + (int)Math.round(Math.cos(angle + tier * 0.08) * radius);
            int y = 128 + (int)Math.round(Math.sin(angle + tier * 0.08) * radius);
            result.add(new Point(clampCoordinate(x), clampCoordinate(y)));
        }
        return List.copyOf(result);
    }

    private static List<Point> voidPattern(int tier) {
        int spikes = 4 + tier;
        int vertices = spikes * 2;
        List<Point> result = new ArrayList<>(vertices + 1);
        for (int step = 0; step <= vertices; step++) {
            double angle = -Math.PI / 2.0 + Math.PI * 2.0 * step / vertices + tier * 0.035;
            double radius = (step & 1) == 0 ? 106.0 : Math.max(31.0, 54.0 - tier * 2.0);
            int x = 128 + (int)Math.round(Math.cos(angle) * radius);
            int y = 128 + (int)Math.round(Math.sin(angle) * radius);
            result.add(new Point(clampCoordinate(x), clampCoordinate(y)));
        }
        return List.copyOf(result);
    }

    private static int clampCoordinate(int coordinate) {
        return Math.max(12, Math.min(243, coordinate));
    }

    private static List<Point> resample(List<Point> source, int count) {
        double totalLength = pathLength(source);
        if (totalLength <= 0.0) {
            return Collections.nCopies(count, source.getFirst());
        }
        List<Point> result = new ArrayList<>(count);
        for (int sample = 0; sample < count; sample++) {
            double wanted = totalLength * sample / (count - 1.0);
            double walked = 0.0;
            for (int i = 1; i < source.size(); i++) {
                Point start = source.get(i - 1);
                Point end = source.get(i);
                double segment = distance(start, end);
                if (walked + segment >= wanted || i == source.size() - 1) {
                    double fraction = segment <= 0.0 ? 0.0 : (wanted - walked) / segment;
                    result.add(new Point(
                            (int)Math.round(start.x() + (end.x() - start.x()) * fraction),
                            (int)Math.round(start.y() + (end.y() - start.y()) * fraction)
                    ));
                    break;
                }
                walked += segment;
            }
        }
        return result;
    }

    private static double pathLength(List<Point> points) {
        double length = 0.0;
        for (int i = 1; i < points.size(); i++) {
            length += distance(points.get(i - 1), points.get(i));
        }
        return length;
    }

    private static double averageDistance(List<Point> left, List<Point> right) {
        double total = 0.0;
        for (int i = 0; i < Math.min(left.size(), right.size()); i++) {
            total += distance(left.get(i), right.get(i));
        }
        return total / Math.min(left.size(), right.size());
    }

    private static double distance(Point left, Point right) {
        return Math.hypot(left.x() - right.x(), left.y() - right.y());
    }

    public record Point(int x, int y) {
    }

    public record Result(boolean success, int accuracy) {
    }
}
