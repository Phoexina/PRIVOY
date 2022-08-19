package ch.ethz.infk.pps.shared.avro;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import org.apache.avro.AvroMissingFieldException;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.data.RecordBuilder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.ResolvingDecoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.SchemaStore;
import org.apache.avro.specific.AvroGenerated;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.specific.SpecificRecordBuilderBase;

@AvroGenerated
public class HeacHeader extends SpecificRecordBase implements SpecificRecord {
    private static final long serialVersionUID = -5075543840481645905L;
    public static final Schema SCHEMA$ = (new Schema.Parser()).parse("{\"type\":\"record\",\"name\":\"HeacHeader\",\"namespace\":\"ch.ethz.infk.pps.shared.avro\",\"fields\":[{\"name\":\"start\",\"type\":\"long\"},{\"name\":\"end\",\"type\":\"long\"}]}");
    private static SpecificData MODEL$ = new SpecificData();
    private static final BinaryMessageEncoder<HeacHeader> ENCODER;
    private static final BinaryMessageDecoder<HeacHeader> DECODER;
    /** @deprecated */
    @Deprecated
    public long start;
    /** @deprecated */
    @Deprecated
    public long end;
    private static final DatumWriter<HeacHeader> WRITER$;
    private static final DatumReader<HeacHeader> READER$;

    public static Schema getClassSchema() {
        return SCHEMA$;
    }

    public static BinaryMessageEncoder<HeacHeader> getEncoder() {
        return ENCODER;
    }

    public static BinaryMessageDecoder<HeacHeader> getDecoder() {
        return DECODER;
    }

    public static BinaryMessageDecoder<HeacHeader> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder(MODEL$, SCHEMA$, resolver);
    }

    public ByteBuffer toByteBuffer() throws IOException {
        return ENCODER.encode(this);
    }

    public static HeacHeader fromByteBuffer(ByteBuffer b) throws IOException {
        return (HeacHeader)DECODER.decode(b);
    }

    public HeacHeader() {
    }

    public HeacHeader(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public SpecificData getSpecificData() {
        return MODEL$;
    }

    public Schema getSchema() {
        return SCHEMA$;
    }

    public Object get(int field$) {
        switch (field$) {
            case 0:
                return this.start;
            case 1:
                return this.end;
            default:
                throw new AvroRuntimeException("Bad index");
        }
    }

    public void put(int field$, Object value$) {
        switch (field$) {
            case 0:
                this.start = (Long)value$;
                break;
            case 1:
                this.end = (Long)value$;
                break;
            default:
                throw new AvroRuntimeException("Bad index");
        }

    }

    public long getStart() {
        return this.start;
    }

    public void setStart(long value) {
        this.start = value;
    }

    public long getEnd() {
        return this.end;
    }

    public void setEnd(long value) {
        this.end = value;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Builder other) {
        return other == null ? new Builder() : new Builder(other);
    }

    public static Builder newBuilder(HeacHeader other) {
        return other == null ? new Builder() : new Builder(other);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        WRITER$.write(this, SpecificData.getEncoder(out));
    }

    public void readExternal(ObjectInput in) throws IOException {
        READER$.read(this, SpecificData.getDecoder(in));
    }

    protected boolean hasCustomCoders() {
        return true;
    }

    public void customEncode(Encoder out) throws IOException {
        out.writeLong(this.start);
        out.writeLong(this.end);
    }

    public void customDecode(ResolvingDecoder in) throws IOException {
        Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
        if (fieldOrder == null) {
            this.start = in.readLong();
            this.end = in.readLong();
        } else {
            for(int i = 0; i < 2; ++i) {
                switch (fieldOrder[i].pos()) {
                    case 0:
                        this.start = in.readLong();
                        break;
                    case 1:
                        this.end = in.readLong();
                        break;
                    default:
                        throw new IOException("Corrupt ResolvingDecoder.");
                }
            }
        }

    }

    static {
        ENCODER = new BinaryMessageEncoder(MODEL$, SCHEMA$);
        DECODER = new BinaryMessageDecoder(MODEL$, SCHEMA$);
        WRITER$ = MODEL$.createDatumWriter(SCHEMA$);
        READER$ = MODEL$.createDatumReader(SCHEMA$);
    }

    public static class Builder extends SpecificRecordBuilderBase<HeacHeader> implements RecordBuilder<HeacHeader> {
        private long start;
        private long end;

        private Builder() {
            super(HeacHeader.SCHEMA$);
        }

        private Builder(Builder other) {
            super(other);
            if (isValidValue(this.fields()[0], other.start)) {
                this.start = (Long)this.data().deepCopy(this.fields()[0].schema(), other.start);
                this.fieldSetFlags()[0] = other.fieldSetFlags()[0];
            }

            if (isValidValue(this.fields()[1], other.end)) {
                this.end = (Long)this.data().deepCopy(this.fields()[1].schema(), other.end);
                this.fieldSetFlags()[1] = other.fieldSetFlags()[1];
            }

        }

        private Builder(HeacHeader other) {
            super(HeacHeader.SCHEMA$);
            if (isValidValue(this.fields()[0], other.start)) {
                this.start = (Long)this.data().deepCopy(this.fields()[0].schema(), other.start);
                this.fieldSetFlags()[0] = true;
            }

            if (isValidValue(this.fields()[1], other.end)) {
                this.end = (Long)this.data().deepCopy(this.fields()[1].schema(), other.end);
                this.fieldSetFlags()[1] = true;
            }

        }

        public long getStart() {
            return this.start;
        }

        public Builder setStart(long value) {
            this.validate(this.fields()[0], value);
            this.start = value;
            this.fieldSetFlags()[0] = true;
            return this;
        }

        public boolean hasStart() {
            return this.fieldSetFlags()[0];
        }

        public Builder clearStart() {
            this.fieldSetFlags()[0] = false;
            return this;
        }

        public long getEnd() {
            return this.end;
        }

        public Builder setEnd(long value) {
            this.validate(this.fields()[1], value);
            this.end = value;
            this.fieldSetFlags()[1] = true;
            return this;
        }

        public boolean hasEnd() {
            return this.fieldSetFlags()[1];
        }

        public Builder clearEnd() {
            this.fieldSetFlags()[1] = false;
            return this;
        }

        public HeacHeader build() {
            try {
                HeacHeader record = new HeacHeader();
                record.start = this.fieldSetFlags()[0] ? this.start : (Long)this.defaultValue(this.fields()[0]);
                record.end = this.fieldSetFlags()[1] ? this.end : (Long)this.defaultValue(this.fields()[1]);
                return record;
            } catch (AvroMissingFieldException var2) {
                throw var2;
            } catch (Exception var3) {
                throw new AvroRuntimeException(var3);
            }
        }
    }
}
