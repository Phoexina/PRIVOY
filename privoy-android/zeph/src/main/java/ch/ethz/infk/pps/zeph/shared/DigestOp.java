package ch.ethz.infk.pps.zeph.shared;

import ch.ethz.infk.pps.shared.avro.ApplicationAdapter;
import ch.ethz.infk.pps.shared.avro.Digest;
import ch.ethz.infk.pps.shared.avro.HeacHeader;
import ch.ethz.infk.pps.shared.avro.Input;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.avro.Schema.Type;

public class DigestOp {
    public static final List<String> DIGEST_AGG_FIELDS = (List)Digest.getClassSchema().getFields().stream().filter((field) -> {
        return field.schema().getType() == Type.ARRAY;
    }).map((field) -> {
        return field.name();
    }).collect(Collectors.toList());
    public static final int NUM_DIGEST_AGG_FIELDS;

    public DigestOp() {
    }

    public static void main(String[] args) {
        Input input = ApplicationAdapter.random();
        System.out.println("Input: " + input);
        PrintStream var10000 = System.out;
        int var10001 = DIGEST_AGG_FIELDS.size();
        var10000.println("\nEncoding     Number of Fields: " + var10001 + "   Number of Longs: " + getTotalNumberOfElements());
    }

    public static long getTotalNumberOfElements() {
        Digest digest = empty();
        return DIGEST_AGG_FIELDS.stream().map((name) -> {
            List<Long> elements = (List)digest.get(name);
            return elements;
        }).flatMap((elements) -> {
            return elements.stream();
        }).count();
    }

    public static Digest add(Digest d1, Digest d2) {
        Digest result = empty();

        for(int i = 0; i < NUM_DIGEST_AGG_FIELDS; ++i) {
            String fieldName = (String)DIGEST_AGG_FIELDS.get(i);
            List<Long> elements1 = (List)d1.get(fieldName);
            List<Long> elements2 = (List)d2.get(fieldName);
            List<Long> resultElements = (List)result.get(fieldName);
            int nElements = elements1.size();

            assert nElements == elements2.size();

            for(int j = 0; j < nElements; ++j) {
                long res = (Long)elements1.get(j) + (Long)elements2.get(j);
                resultElements.set(j, res);
            }
        }

        HeacHeader header = null;
        if (d1.getHeader() != null && d2.getHeader() != null) {
            long start = Math.min(d1.getHeader().getStart(), d2.getHeader().getStart());
            long end = Math.max(d1.getHeader().getEnd(), d2.getHeader().getEnd());
            header = new HeacHeader(start, end);
        }

        result.setHeader(header);
        return result;
    }

    public static Digest subtract(Digest d1, Digest d2) {
        Digest result = empty();

        for(int i = 0; i < NUM_DIGEST_AGG_FIELDS; ++i) {
            String fieldName = (String)DIGEST_AGG_FIELDS.get(i);
            List<Long> elements1 = (List)d1.get(fieldName);
            List<Long> elements2 = (List)d2.get(fieldName);
            List<Long> resultElements = (List)result.get(fieldName);
            int nElements = elements1.size();

            assert nElements == elements2.size();

            for(int j = 0; j < nElements; ++j) {
                long res = (Long)elements1.get(j) - (Long)elements2.get(j);
                resultElements.set(j, res);
            }
        }

        result.setHeader((HeacHeader)null);
        return result;
    }

    public static Long getCount(Digest digest) {
        long count = (Long)digest.getCount().get(0);
        return count;
    }

    public static Long getSum(Digest digest) {
        List<Long> hist = (List)digest.get("valueBase");
        long sum = (Long)formatHist(hist, 64).get(0);
        return sum;
    }

    public static Double getMean(Digest digest) {
        double count = (double)(Long)digest.getCount().get(0);
        if (count == 0.0) {
            return null;
        } else {
            long sum = (Long)((List)digest.get("valueBase")).get(0);
            return (double)sum / count;
        }
    }

    public static Double getVariance(Digest digest) {
        double count = (double)(Long)digest.getCount().get(0);
        if (count == 0.0) {
            return null;
        } else {
            long sum = (Long)((List)digest.get("valueBase")).get(0);
            long sumOfSquares = (Long)((List)digest.get("valueBase")).get(1);
            return (double)sumOfSquares / count - Math.pow((double)sum / count, 2.0);
        }
    }

    public static Double getStdDev(Digest digest) {
        Double variance = getVariance(digest);
        return variance == null ? null : Math.sqrt(variance);
    }

    public static void setHist(long value, long binValue, List<Long> hist, int bits, long minValue, Long maxValue) {
        int binsPerLong = 64 / bits;
        int idx = (int)((value - minValue) / (long)binsPerLong);
        int shift = (int)(value % (long)binsPerLong * (long)bits);
        hist.set(idx, binValue << shift);
    }

    public static List<Long> formatHist(List<Long> hist, int bits) {
        if (bits == 64) {
            return hist;
        } else {
            List<Long> expandedHist = new ArrayList(hist.size() * 64 / bits);
            long mask = (1L << bits) - 1L;
            int numBinsPerLong = 64 / bits;

            assert 64 % bits == 0;

            Iterator var6 = hist.iterator();

            while(var6.hasNext()) {
                Long elem = (Long)var6.next();

                for(int i = 0; i < numBinsPerLong; ++i) {
                    expandedHist.add(elem >> i * bits & mask);
                }
            }

            return expandedHist;
        }
    }

    public static void setBase(long value, List<Long> base) {
        base.set(0, value);
        base.set(1, (long)Math.pow((double)value, 2.0));
    }

    public static Digest empty() {
        return ApplicationAdapter.emptyDigest();
    }

    public static Digest random() {
        Input value = ApplicationAdapter.random();
        return ApplicationAdapter.toDigest(value);
    }

    public static Digest of(long[] fields) {
        Digest digest = empty();
        int fieldIdx = 0;

        for(int i = 0; i < NUM_DIGEST_AGG_FIELDS; ++i) {
            String fieldName = (String)DIGEST_AGG_FIELDS.get(i);
            List<Long> elements = (List)digest.get(fieldName);
            int size = elements.size();

            for(int j = 0; j < size; ++j) {
                elements.set(j, fields[fieldIdx]);
                ++fieldIdx;
            }
        }

        return digest;
    }


    static {
        NUM_DIGEST_AGG_FIELDS = DIGEST_AGG_FIELDS.size();
    }
}
