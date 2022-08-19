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
public class Input extends SpecificRecordBase implements SpecificRecord {
    private static final long serialVersionUID = -2478731204108231940L;
    public static final Schema SCHEMA$ = (new Schema.Parser()).parse("{\"type\":\"record\",\"name\":\"Input\",\"namespace\":\"ch.ethz.infk.pps.shared.avro\",\"fields\":[{\"name\":\"value\",\"type\":\"long\"},{\"name\":\"count\",\"type\":[\"null\",\"long\"]}]}");
    private static SpecificData MODEL$ = new SpecificData();
    private static final BinaryMessageEncoder<Input> ENCODER;
    private static final BinaryMessageDecoder<Input> DECODER;
    /** @deprecated */
    @Deprecated
    public long value;
    /** @deprecated */
    @Deprecated
    public Long count;
    private static final DatumWriter<Input> WRITER$;
    private static final DatumReader<Input> READER$;

    public static Schema getClassSchema() {
        return SCHEMA$;
    }

    public static BinaryMessageEncoder<Input> getEncoder() {
        return ENCODER;
    }

    public static BinaryMessageDecoder<Input> getDecoder() {
        return DECODER;
    }

    public static BinaryMessageDecoder<Input> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder(MODEL$, SCHEMA$, resolver);
    }

    public ByteBuffer toByteBuffer() throws IOException {
        return ENCODER.encode(this);
    }

    public static Input fromByteBuffer(ByteBuffer b) throws IOException {
        return (Input)DECODER.decode(b);
    }

    public Input() {
    }

    public Input(Long value, Long count) {
        this.value = value;
        this.count = count;
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
                return this.value;
            case 1:
                return this.count;
            default:
                throw new AvroRuntimeException("Bad index");
        }
    }

    public void put(int field$, Object value$) {
        switch (field$) {
            case 0:
                this.value = (Long)value$;
                break;
            case 1:
                this.count = (Long)value$;
                break;
            default:
                throw new AvroRuntimeException("Bad index");
        }

    }

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Long getCount() {
        return this.count;
    }

    public void setCount(Long value) {
        this.count = value;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Builder other) {
        return other == null ? new Builder() : new Builder(other);
    }

    public static Builder newBuilder(Input other) {
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
        out.writeLong(this.value);
        if (this.count == null) {
            out.writeIndex(0);
            out.writeNull();
        } else {
            out.writeIndex(1);
            out.writeLong(this.count);
        }

    }

    public void customDecode(ResolvingDecoder in) throws IOException {
        Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
        if (fieldOrder == null) {
            this.value = in.readLong();
            if (in.readIndex() != 1) {
                in.readNull();
                this.count = null;
            } else {
                this.count = in.readLong();
            }
        } else {
            for(int i = 0; i < 2; ++i) {
                switch (fieldOrder[i].pos()) {
                    case 0:
                        this.value = in.readLong();
                        break;
                    case 1:
                        if (in.readIndex() != 1) {
                            in.readNull();
                            this.count = null;
                        } else {
                            this.count = in.readLong();
                        }
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

    public static class Builder extends SpecificRecordBuilderBase<Input> implements RecordBuilder<Input> {
        private long value;
        private Long count;

        private Builder() {
            super(Input.SCHEMA$);
        }

        private Builder(Builder other) {
            super(other);
            if (isValidValue(this.fields()[0], other.value)) {
                this.value = (Long)this.data().deepCopy(this.fields()[0].schema(), other.value);
                this.fieldSetFlags()[0] = other.fieldSetFlags()[0];
            }

            if (isValidValue(this.fields()[1], other.count)) {
                this.count = (Long)this.data().deepCopy(this.fields()[1].schema(), other.count);
                this.fieldSetFlags()[1] = other.fieldSetFlags()[1];
            }

        }

        private Builder(Input other) {
            super(Input.SCHEMA$);
            if (isValidValue(this.fields()[0], other.value)) {
                this.value = (Long)this.data().deepCopy(this.fields()[0].schema(), other.value);
                this.fieldSetFlags()[0] = true;
            }

            if (isValidValue(this.fields()[1], other.count)) {
                this.count = (Long)this.data().deepCopy(this.fields()[1].schema(), other.count);
                this.fieldSetFlags()[1] = true;
            }

        }

        public long getValue() {
            return this.value;
        }

        public Builder setValue(long value) {
            this.validate(this.fields()[0], value);
            this.value = value;
            this.fieldSetFlags()[0] = true;
            return this;
        }

        public boolean hasValue() {
            return this.fieldSetFlags()[0];
        }

        public Builder clearValue() {
            this.fieldSetFlags()[0] = false;
            return this;
        }

        public Long getCount() {
            return this.count;
        }

        public Builder setCount(Long value) {
            this.validate(this.fields()[1], value);
            this.count = value;
            this.fieldSetFlags()[1] = true;
            return this;
        }

        public boolean hasCount() {
            return this.fieldSetFlags()[1];
        }

        public Builder clearCount() {
            this.count = null;
            this.fieldSetFlags()[1] = false;
            return this;
        }

        public Input build() {
            try {
                Input record = new Input();
                record.value = this.fieldSetFlags()[0] ? this.value : (Long)this.defaultValue(this.fields()[0]);
                record.count = this.fieldSetFlags()[1] ? this.count : (Long)this.defaultValue(this.fields()[1]);
                return record;
            } catch (AvroMissingFieldException var2) {
                throw var2;
            } catch (Exception var3) {
                throw new AvroRuntimeException(var3);
            }
        }
    }
}
