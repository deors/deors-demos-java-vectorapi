package deors.demos.java.vectorapi;

import jdk.incubator.vector.*;
import org.openjdk.jmh.annotations.*;

public class VectorAPIExample {
    static final boolean VERBOSE = Boolean.valueOf(System.getProperty("verbose", "false"));
    static final int DATA_SIZE = 1_000_000;

    public static void main(String[] args) {
        simpleExampleClassic();
        simpleExampleVector();
        bigDataExampleClassic();
        bigDataExampleVector();
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public static void simpleExampleClassic() {
        var a = new float[] {1f, 2f, 3f, 5f};
        var b = new float[] {100f, 500f, 1000f, 5000f};
        var c = new float[4];

        for (var i = 0; i < a.length; i++) {
            c[i] = (a[i] * a[i] + b[i] * b[i]) * -1.0f;
        }

        if (VERBOSE) {
            System.out.println("simpleExampleClassic results:");
            for (var i = 0; i < c.length; i++) {
                System.out.printf("  sample %d: %f , %f => %f%n", i, a[i], b[i], c[i]);
            }
        }
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public static void simpleExampleVector() {
        var a = new float[] {1f, 2f, 3f, 5f};
        var b = new float[] {100f, 500f, 1000f, 5000f};
        var c = new float[4];

        var va = FloatVector.fromArray(FloatVector.SPECIES_128, a, 0);
        var vb = FloatVector.fromArray(FloatVector.SPECIES_128, b, 0);

        var result = va.pow(2).add(vb.pow(2)).neg();

        result.intoArray(c, 0);

        if (VERBOSE) {
            System.out.println("simpleExampleVector results:");
            for (var i = 0; i < c.length; i++) {
                System.out.printf("  sample %d: %f , %f => %f%n", i, a[i], b[i], c[i]);
            }
        }
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public static void bigDataExampleClassic() {
        var a = randomFloatArray(DATA_SIZE);
        var b = randomFloatArray(DATA_SIZE);
        var c = new float[DATA_SIZE];

        for (var i = 0; i < a.length; i++) {
            c[i] = (a[i] * a[i] + b[i] * b[i]) * -1.0f;
        }

        if (VERBOSE) {
            System.out.println("bigDataExampleClassic results (last 10 elements only):");
            for (var i = c.length - 10; i < c.length; i++) {
                System.out.printf("  sample %d: %f , %f => %f%n", i, a[i], b[i], c[i]);
            }
        }
    }

    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 2)
    @Measurement(iterations = 3)
    public static void bigDataExampleVector() {
        var species = FloatVector.SPECIES_PREFERRED;

        var a = randomFloatArray(DATA_SIZE);
        var b = randomFloatArray(DATA_SIZE);
        var c = new float[DATA_SIZE];

        for (var i = 0; i < a.length; i += species.length()) {
            var mask = species.indexInRange(i, a.length);
            var va = FloatVector.fromArray(species, a, i, mask);
            var vb = FloatVector.fromArray(species, b, i, mask);
            va.pow(2).add(vb.pow(2)).neg().intoArray(c, i, mask);
        }

        if (VERBOSE) {
            System.out.println("bigDataExampleVector results (last 10 elements only):");
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
