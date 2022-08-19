package ch.ethz.infk.pps.shared.avro;

import ch.ethz.infk.pps.zeph.shared.DigestOp;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ApplicationAdapter {
    public static final String MAIN_BASE_FIELD_NAME = "valueBase";
    public static final int MAIN_BASE_FIELD_BITS = 64;

    public ApplicationAdapter() {
    }

    public static Input add(Input a, Input b) {
        return new Input(a.getValue() + b.getValue(), (Long)Optional.fromNullable(a.getCount()).or(1L) + (Long)Optional.fromNullable(b.getCount()).or(1L));
    }

    public static Input empty() {
        return new Input(0L, 0L);
    }

    public static Input random() {
        long value = ThreadLocalRandom.current().nextLong(0L, 100L);
        return new Input(value, (Long)null);
    }

    public static Long getCount(Input input) {
        return input.getCount();
    }

    public static Long getSum(Input input) {
        return input.getValue();
    }

    public static Digest emptyDigest() {
        List<Long> valueBase = new ArrayList(Collections.nCopies(2, 0L));
        List<Long> count = new ArrayList(Collections.nCopies(1, 0L));
        HeacHeader header = null;
        return new Digest(valueBase, count, (HeacHeader)header);
    }

    public static Digest toDigest(Input input) {
        Digest digest = DigestOp.empty();
        digest.getCount().set(0, 1L);
        DigestOp.setBase(input.getValue(), digest.getValueBase());
        return digest;
    }

    public static Input fromCsv(String[] parts) {
        throw new IllegalStateException("reading from csv is not implemented for the standard application");
    }
}
