package ch.ethz.infk.pps.shared.avro;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import org.apache.avro.AvroMissingFieldException;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.data.RecordBuilder;
import org.apache.avro.generic.GenericData;
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
public class Digest extends SpecificRecordBase implements SpecificRecord {
    private static final long serialVersionUID = -8194957469593858927L;
    public static final Schema SCHEMA$ = (new Schema.Parser()).parse("{\"type\":\"record\",\"name\":\"Digest\",\"namespace\":\"ch.ethz.infk.pps.shared.avro\",\"fields\":[{\"name\":\"valueBase\",\"type\":{\"type\":\"array\",\"items\":\"long\"}},{\"name\":\"count\",\"type\":{\"type\":\"array\",\"items\":\"long\"}},{\"name\":\"header\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"HeacHeader\",\"fields\":[{\"name\":\"start\",\"type\":\"long\"},{\"name\":\"end\",\"type\":\"long\"}]}]}]}");
    private static SpecificData MODEL$ = new SpecificData();
    private static final BinaryMessageEncoder<Digest> ENCODER;
    private static final BinaryMessageDecoder<Digest> DECODER;
    /** @deprecated */
    @Deprecated
    public List<Long> valueBase;
    /** @deprecated */
    @Deprecated
    public List<Long> count;
    /** @deprecated */
    @Deprecated
    public HeacHeader header;
    private static final DatumWriter<Digest> WRITER$;
    private static final DatumReader<Digest> READER$;

    public static Schema getClassSchema() {
        return SCHEMA$;
    }

    public static BinaryMessageEncoder<Digest> getEncoder() {
        return ENCODER;
    }

    public static BinaryMessageDecoder<Digest> getDecoder() {
        return DECODER;
    }

    public static BinaryMessageDecoder<Digest> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder(MODEL$, SCHEMA$, resolver);
    }

    public ByteBuffer toByteBuffer() throws IOException {
        return ENCODER.encode(this);
    }

    public static Digest fromByteBuffer(ByteBuffer b) throws IOException {
        return (Digest)DECODER.decode(b);
    }

    public Digest() {
    }

    public Digest(List<Long> valueBase, List<Long> count, HeacHeader header) {
        this.valueBase = valueBase;
        this.count = count;
        this.header = header;
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
                return this.valueBase;
            case 1:
                return this.count;
            case 2:
                return this.header;
            default:
                throw new AvroRuntimeException("Bad index");
        }
    }

    public void put(int field$, Object value$) {
        switch (field$) {
            case 0:
                this.valueBase = (List)value$;
                break;
            case 1:
                this.count = (List)value$;
                break;
            case 2:
                this.header = (HeacHeader)value$;
                break;
            default:
                throw new AvroRuntimeException("Bad index");
        }

    }

    public List<Long> getValueBase() {
        return this.valueBase;
    }

    public void setValueBase(List<Long> value) {
        this.valueBase = value;
    }

    public List<Long> getCount() {
        return this.count;
    }

    public void setCount(List<Long> value) {
        this.count = value;
    }

    public HeacHeader getHeader() {
        return this.header;
    }

    public void setHeader(HeacHeader value) {
        this.header = value;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Builder other) {
        return other == null ? new Builder() : new Builder(other);
    }

    public static Builder newBuilder(Digest other) {
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
        long size0 = (long)this.valueBase.size();
        out.writeArrayStart();
        out.setItemCount(size0);
        long actualSize0 = 0L;
        Iterator var6 = this.valueBase.iterator();

        while(var6.hasNext()) {
            Long e0 = (Long)var6.next();
            ++actualSize0;
            out.startItem();
            out.writeLong(e0);
        }

        out.writeArrayEnd();
        if (actualSize0 != size0) {
            throw new ConcurrentModificationException("Array-size written was " + size0 + ", but element count was " + actualSize0 + ".");
        } else {
            long size1 = (long)this.count.size();
            out.writeArrayStart();
            out.setItemCount(size1);
            long actualSize1 = 0L;
            Iterator var10 = this.count.iterator();

            while(var10.hasNext()) {
                Long e1 = (Long)var10.next();
                ++actualSize1;
                out.startItem();
                out.writeLong(e1);
            }

            out.writeArrayEnd();
            if (actualSize1 != size1) {
                throw new ConcurrentModificationException("Array-size written was " + size1 + ", but element count was " + actualSize1 + ".");
            } else {
                if (this.header == null) {
                    out.writeIndex(0);
                    out.writeNull();
                } else {
                    out.writeIndex(1);
                    this.header.customEncode(out);
                }

            }
        }
    }

    public void customDecode(ResolvingDecoder in) throws IOException {
        Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
        Long var10000;
        if (fieldOrder == null) {
            long size0 = in.readArrayStart();
            List<Long> a0 = this.valueBase;
            if (a0 == null) {
                a0 = new GenericData.Array((int)size0, SCHEMA$.getField("valueBase").schema());
                this.valueBase = (List)a0;
            } else {
                ((List)a0).clear();
            }

            for(GenericData.Array<Long> ga0 = a0 instanceof GenericData.Array ? (GenericData.Array)a0 : null; 0L < size0; size0 = in.arrayNext()) {
                while(size0 != 0L) {
                    if (ga0 != null) {
                        var10000 = (Long)ga0.peek();
                    } else {
                        var10000 = null;
                    }

                    Long e0 = in.readLong();
                    ((List)a0).add(e0);
                    --size0;
                }
            }

            long size1 = in.readArrayStart();
            List<Long> a1 = this.count;
            if (a1 == null) {
                a1 = new GenericData.Array((int)size1, SCHEMA$.getField("count").schema());
                this.count = (List)a1;
            } else {
                ((List)a1).clear();
            }

            for(GenericData.Array<Long> ga1 = a1 instanceof GenericData.Array ? (GenericData.Array)a1 : null; 0L < size1; size1 = in.arrayNext()) {
                while(size1 != 0L) {
                    if (ga1 != null) {
                        var10000 = (Long)ga1.peek();
                    } else {
                        var10000 = null;
                    }

                    Long e1 = in.readLong();
                    ((List)a1).add(e1);
                    --size1;
                }
            }

            if (in.readIndex() != 1) {
                in.readNull();
                this.header = null;
            } else {
                if (this.header == null) {
                    this.header = new HeacHeader();
                }

                this.header.customDecode(in);
            }
        } else {
            label159:
            for(int i = 0; i < 3; ++i) {
                switch (fieldOrder[i].pos()) {
                    case 0:
                        long size0 = in.readArrayStart();
                        List<Long> a0 = this.valueBase;
                        if (a0 == null) {
                            a0 = new GenericData.Array((int)size0, SCHEMA$.getField("valueBase").schema());
                            this.valueBase = (List)a0;
                        } else {
                            ((List)a0).clear();
                        }

                        for(GenericData.Array<Long> ga0 = a0 instanceof GenericData.Array ? (GenericData.Array)a0 : null; 0L < size0; size0 = in.arrayNext()) {
                            while(size0 != 0L) {
                                if (ga0 != null) {
                                    var10000 = (Long)ga0.peek();
                                } else {
                                    var10000 = null;
                                }

                                Long e0 = in.readLong();
                                ((List)a0).add(e0);
                                --size0;
                            }
                        }
                        break;
                    case 1:
                        long size1 = in.readArrayStart();
                        List<Long> a1 = this.count;
                        if (a1 == null) {
                            a1 = new GenericData.Array((int)size1, SCHEMA$.getField("count").schema());
                            this.count = (List)a1;
                        } else {
                            ((List)a1).clear();
                        }

                        GenericData.Array<Long> ga1 = a1 instanceof GenericData.Array ? (GenericData.Array)a1 : null;

                        while(true) {
                            if (0L >= size1) {
                                continue label159;
                            }

                            while(size1 != 0L) {
                                if (ga1 != null) {
                                    var10000 = (Long)ga1.peek();
                                } else {
                                    var10000 = null;
                                }

                                Long e1 = in.readLong();
                                ((List)a1).add(e1);
                                --size1;
                            }

                            size1 = in.arrayNext();
                        }
                    case 2:
                        if (in.readIndex() != 1) {
                            in.readNull();
                            this.header = null;
                        } else {
                            if (this.header == null) {
                                this.header = new HeacHeader();
                            }

                            this.header.customDecode(in);
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

    public static class Builder extends SpecificRecordBuilderBase<Digest> implements RecordBuilder<Digest> {
        private List<Long> valueBase;
        private List<Long> count;
        private HeacHeader header;
        private HeacHeader.Builder headerBuilder;

        private Builder() {
            super(Digest.SCHEMA$);
        }

        private Builder(Builder other) {
            super(other);
            if (isValidValue(this.fields()[0], other.valueBase)) {
                this.valueBase = (List)this.data().deepCopy(this.fields()[0].schema(), other.valueBase);
                this.fieldSetFlags()[0] = other.fieldSetFlags()[0];
            }

            if (isValidValue(this.fields()[1], other.count)) {
                this.count = (List)this.data().deepCopy(this.fields()[1].schema(), other.count);
                this.fieldSetFlags()[1] = other.fieldSetFlags()[1];
            }

            if (isValidValue(this.fields()[2], other.header)) {
                this.header = (HeacHeader)this.data().deepCopy(this.fields()[2].schema(), other.header);
                this.fieldSetFlags()[2] = other.fieldSetFlags()[2];
            }

            if (other.hasHeaderBuilder()) {
                this.headerBuilder = HeacHeader.newBuilder(other.getHeaderBuilder());
            }

        }

        private Builder(Digest other) {
            super(Digest.SCHEMA$);
            if (isValidValue(this.fields()[0], other.valueBase)) {
                this.valueBase = (List)this.data().deepCopy(this.fields()[0].schema(), other.valueBase);
                this.fieldSetFlags()[0] = true;
            }

            if (isValidValue(this.fields()[1], other.count)) {
                this.count = (List)this.data().deepCopy(this.fields()[1].schema(), other.count);
                this.fieldSetFlags()[1] = true;
            }

            if (isValidValue(this.fields()[2], other.header)) {
                this.header = (HeacHeader)this.data().deepCopy(this.fields()[2].schema(), other.header);
                this.fieldSetFlags()[2] = true;
            }

            this.headerBuilder = null;
        }

        public List<Long> getValueBase() {
            return this.valueBase;
        }

        public Builder setValueBase(List<Long> value) {
            this.validate(this.fields()[0], value);
            this.valueBase = value;
            this.fieldSetFlags()[0] = true;
            return this;
        }

        public boolean hasValueBase() {
            return this.fieldSetFlags()[0];
        }

        public Builder clearValueBase() {
            this.valueBase = null;
            this.fieldSetFlags()[0] = false;
            return this;
        }

        public List<Long> getCount() {
            return this.count;
        }

        public Builder setCount(List<Long> value) {
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

        public HeacHeader getHeader() {
            return this.header;
        }

        public Builder setHeader(HeacHeader value) {
            this.validate(this.fields()[2], value);
            this.headerBuilder = null;
            this.header = value;
            this.fieldSetFlags()[2] = true;
            return this;
        }

        public boolean hasHeader() {
            return this.fieldSetFlags()[2];
        }

        public HeacHeader.Builder getHeaderBuilder() {
            if (this.headerBuilder == null) {
                if (this.hasHeader()) {
                    this.setHeaderBuilder(HeacHeader.newBuilder(this.header));
                } else {
                    this.setHeaderBuilder(HeacHeader.newBuilder());
                }
            }

            return this.headerBuilder;
        }

        public Builder setHeaderBuilder(HeacHeader.Builder value) {
            this.clearHeader();
            this.headerBuilder = value;
            return this;
        }

        public boolean hasHeaderBuilder() {
            return this.headerBuilder != null;
        }

        public Builder clearHeader() {
            this.header = null;
            this.headerBuilder = null;
            this.fieldSetFlags()[2] = false;
            return this;
        }

        public Digest build() {
            try {
                Digest record = new Digest();
                record.valueBase = this.fieldSetFlags()[0] ? this.valueBase : (List)this.defaultValue(this.fields()[0]);
                record.count = this.fieldSetFlags()[1] ? this.count : (List)this.defaultValue(this.fields()[1]);
                if (this.headerBuilder != null) {
                    try {
                        record.header = this.headerBuilder.build();
                    } catch (AvroMissingFieldException var3) {
                        var3.addParentField(record.getSchema().getField("header"));
                        throw var3;
                    }
                } else {
                    record.header = this.fieldSetFlags()[2] ? this.header : (HeacHeader)this.defaultValue(this.fields()[2]);
                }

                return record;
            } catch (AvroMissingFieldException var4) {
                throw var4;
            } catch (Exception var5) {
                throw new AvroRuntimeException(var5);
            }
        }
    }
}
