package deors.demos.java.vectorapi;

import jdk.incubator.vector.*;
import org.openjdk.jmh.annotations.*;

public class VectorAPIExample {
    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
    static final boolean VERBOSE = Boolean.valueOf(System.getProperty("verbose", "false"));
    static final int DATA_SIZE = 100_000;

    public static void main(String[] args) {
        classicExample();
        vectorExample();
        classicBigDataExample();
        vectorBigDataExample();
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public static void classicExample() {
        var a = new float[] {1f, 2f, 3f, 5f};
        var b = new float[] {100f, 500f, 1000f, 5000f};
        var c = new float[4];

        for (var i = 0; i < a.length; i++) {
            c[i] = (a[i] * a[i] + b[i] * b[i]) * -1.0f;
        }

        if (VERBOSE) {
            System.out.println("classicExample results:");
            for (var i = 0; i < c.length; i++) {
                System.out.printf("  sample %d: %f , %f => %f%n", i, a[i], b[i], c[i]);
            }
        }
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public static void vectorExample() {
        var a = new float[] {1f, 2f, 3f, 5f};
        var b = new float[] {100f, 500f, 1000f, 5000f};
        var c = new float[4];

        var va = FloatVector.fromArray(FloatVector.SPECIES_128, a, 0);
        var vb = FloatVector.fromArray(FloatVector.SPECIES_128, b, 0);

        var result = va.pow(2).add(vb.pow(2)).neg();

        result.intoArray(c, 0);

        if (VERBOSE) {
            System.out.println("vectorExample results:");
            for (var i = 0; i < c.length; i++) {
                System.out.printf("  sample %d: %f , %f => %f%n", i, a[i], b[i], c[i]);
            }
        }
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public static void classicBigDataExample() {
        var a = randomFloatArray(DATA_SIZE);
        var b = randomFloatArray(DATA_SIZE);
        var c = new float[DATA_SIZE];

        for (var i = 0; i < a.length; i++) {
            c[i] = (a[i] * a[i] + b[i] * b[i]) * -1.0f;
        }

        if (VERBOSE) {
            System.out.println("classicBigDataExample results (last 10 elements only):");
            for (var i = c.length - 10; i < c.length; i++) {
                System.out.printf("  sample %d: %f , %f => %f%n", i, a[i], b[i], c[i]);
            }
        }
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public static void vectorBigDataExample() {
        var a = randomFloatArray(DATA_SIZE);
        var b = randomFloatArray(DATA_SIZE);
        var c = new float[DATA_SIZE];

        for (var i = 0; i < a.length; i += SPECIES.length()) {
            var mask = SPECIES.indexInRange(i, a.length);
            var va = FloatVector.fromArray(SPECIES, a, i, mask);
            var vb = FloatVector.fromArray(SPECIES, b, i, mask);
            va.pow(2).add(vb.pow(2)).neg().intoArray(c, i, mask);
        }

        if (VERBOSE) {
            System.out.println("vectorBigDataExample results (last 10 elements only):");
            for (var i = c.length - 10; i < c.length; i++) {
                System.out.printf("  sample %d: %f , %f => %f%n", i, a[i], b[i], c[i]);
            }
        }
    }

    public static float[] randomFloatArray(int size) {
        var array = new float[size];
        for (var i = 0; i < size; i++) {
            array[i] = (float) Math.random() * 10_000;
        }
        return array;
    }
}
