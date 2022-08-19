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
public class Universe extends SpecificRecordBase implements SpecificRecord {
    private static final long serialVersionUID = -5294672875598560227L;
    public static final Schema SCHEMA$ = (new Schema.Parser()).parse("{\"type\":\"record\",\"name\":\"Universe\",\"namespace\":\"ch.ethz.infk.pps.shared.avro\",\"fields\":[{\"name\":\"universeId\",\"type\":\"long\"},{\"name\":\"firstWindow\",\"type\":{\"type\":\"record\",\"name\":\"Window\",\"fields\":[{\"name\":\"start\",\"type\":\"long\"},{\"name\":\"end\",\"type\":\"long\"}]}},{\"name\":\"members\",\"type\":{\"type\":\"array\",\"items\":\"long\"}},{\"name\":\"minimalSize\",\"type\":\"int\"},{\"name\":\"alpha\",\"type\":\"double\"},{\"name\":\"delta\",\"type\":\"double\"}]}");
    private static SpecificData MODEL$ = new SpecificData();
    private static final BinaryMessageEncoder<Universe> ENCODER;
    private static final BinaryMessageDecoder<Universe> DECODER;
    /** @deprecated */
    @Deprecated
    public long universeId;
    /** @deprecated */
    @Deprecated
    public Window firstWindow;
    /** @deprecated */
    @Deprecated
    public List<Long> members;
    /** @deprecated */
    @Deprecated
    public int minimalSize;
    /** @deprecated */
    @Deprecated
    public double alpha;
    /** @deprecated */
    @Deprecated
    public double delta;
    private static final DatumWriter<Universe> WRITER$;
    private static final DatumReader<Universe> READER$;

    public static Schema getClassSchema() {
        return SCHEMA$;
    }

    public static BinaryMessageEncoder<Universe> getEncoder() {
        return ENCODER;
    }

    public static BinaryMessageDecoder<Universe> getDecoder() {
        return DECODER;
    }

    public static BinaryMessageDecoder<Universe> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder(MODEL$, SCHEMA$, resolver);
    }

    public ByteBuffer toByteBuffer() throws IOException {
        return ENCODER.encode(this);
    }

    public static Universe fromByteBuffer(ByteBuffer b) throws IOException {
        return (Universe)DECODER.decode(b);
    }

    public Universe() {
    }

    public Universe(Long universeId, Window firstWindow, List<Long> members, Integer minimalSize, Double alpha, Double delta) {
        this.universeId = universeId;
        this.firstWindow = firstWindow;
        this.members = members;
        this.minimalSize = minimalSize;
        this.alpha = alpha;
        this.delta = delta;
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
                return this.universeId;
            case 1:
                return this.firstWindow;
            case 2:
                return this.members;
            case 3:
                return this.minimalSize;
            case 4:
                return this.alpha;
            case 5:
                return this.delta;
            default:
                throw new AvroRuntimeException("Bad index");
        }
    }

    public void put(int field$, Object value$) {
        switch (field$) {
            case 0:
                this.universeId = (Long)value$;
                break;
            case 1:
                this.firstWindow = (Window)value$;
                break;
            case 2:
                this.members = (List)value$;
                break;
            case 3:
                this.minimalSize = (Integer)value$;
                break;
            case 4:
                this.alpha = (Double)value$;
                break;
            case 5:
                this.delta = (Double)value$;
                break;
            default:
                throw new AvroRuntimeException("Bad index");
        }

    }

    public long getUniverseId() {
        return this.universeId;
    }

    public void setUniverseId(long value) {
        this.universeId = value;
    }

    public Window getFirstWindow() {
        return this.firstWindow;
    }

    public void setFirstWindow(Window value) {
        this.firstWindow = value;
    }

    public List<Long> getMembers() {
        return this.members;
    }

    public void setMembers(List<Long> value) {
        this.members = value;
    }

    public int getMinimalSize() {
        return this.minimalSize;
    }

    public void setMinimalSize(int value) {
        this.minimalSize = value;
    }

    public double getAlpha() {
        return this.alpha;
    }

    public void setAlpha(double value) {
        this.alpha = value;
    }

    public double getDelta() {
        return this.delta;
    }

    public void setDelta(double value) {
        this.delta = value;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Builder other) {
        return other == null ? new Builder() : new Builder(other);
    }

    public static Builder newBuilder(Universe other) {
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
        out.writeLong(this.universeId);
        this.firstWindow.customEncode(out);
        long size0 = (long)this.members.size();
        out.writeArrayStart();
        out.setItemCount(size0);
        long actualSize0 = 0L;
        Iterator var6 = this.members.iterator();

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
            out.writeInt(this.minimalSize);
            out.writeDouble(this.alpha);
            out.writeDouble(this.delta);
        }
    }

    public void customDecode(ResolvingDecoder in) throws IOException {
        Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
        Long var10000;
        if (fieldOrder == null) {
            this.universeId = in.readLong();
            if (this.firstWindow == null) {
                this.firstWindow = new Window();
            }

            this.firstWindow.customDecode(in);
            long size0 = in.readArrayStart();
            List<Long> a0 = this.members;
            if (a0 == null) {
                a0 = new GenericData.Array((int)size0, SCHEMA$.getField("members").schema());
                this.members = (List)a0;
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

            this.minimalSize = in.readInt();
            this.alpha = in.readDouble();
            this.delta = in.readDouble();
        } else {
            for(int i = 0; i < 6; ++i) {
                switch (fieldOrder[i].pos()) {
                    case 0:
                        this.universeId = in.readLong();
                        break;
                    case 1:
                        if (this.firstWindow == null) {
                            this.firstWindow = new Window();
                        }

                        this.firstWindow.customDecode(in);
                        break;
                    case 2:
                        long size0 = in.readArrayStart();
                        List<Long> a0 = this.members;
                        if (a0 == null) {
                            a0 = new GenericData.Array((int)size0, SCHEMA$.getField("members").schema());
                            this.members = (List)a0;
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
                    case 3:
                        this.minimalSize = in.readInt();
                        break;
                    case 4:
                        this.alpha = in.readDouble();
                        break;
                    case 5:
                        this.delta = in.readDouble();
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

    public static class Builder extends SpecificRecordBuilderBase<Universe> implements RecordBuilder<Universe> {
        private long universeId;
        private Window firstWindow;
        private Window.Builder firstWindowBuilder;
        private List<Long> members;
        private int minimalSize;
        private double alpha;
        private double delta;

        private Builder() {
            super(Universe.SCHEMA$);
        }

        private Builder(Builder other) {
            super(other);
            if (isValidValue(this.fields()[0], other.universeId)) {
                this.universeId = (Long)this.data().deepCopy(this.fields()[0].schema(), other.universeId);
                this.fieldSetFlags()[0] = other.fieldSetFlags()[0];
            }

            if (isValidValue(this.fields()[1], other.firstWindow)) {
                this.firstWindow = (Window)this.data().deepCopy(this.fields()[1].schema(), other.firstWindow);
                this.fieldSetFlags()[1] = other.fieldSetFlags()[1];
            }

            if (other.hasFirstWindowBuilder()) {
                this.firstWindowBuilder = Window.newBuilder(other.getFirstWindowBuilder());
            }

            if (isValidValue(this.fields()[2], other.members)) {
                this.members = (List)this.data().deepCopy(this.fields()[2].schema(), other.members);
                this.fieldSetFlags()[2] = other.fieldSetFlags()[2];
            }

            if (isValidValue(this.fields()[3], other.minimalSize)) {
                this.minimalSize = (Integer)this.data().deepCopy(this.fields()[3].schema(), other.minimalSize);
                this.fieldSetFlags()[3] = other.fieldSetFlags()[3];
            }

            if (isValidValue(this.fields()[4], other.alpha)) {
                this.alpha = (Double)this.data().deepCopy(this.fields()[4].schema(), other.alpha);
                this.fieldSetFlags()[4] = other.fieldSetFlags()[4];
            }

            if (isValidValue(this.fields()[5], other.delta)) {
                this.delta = (Double)this.data().deepCopy(this.fields()[5].schema(), other.delta);
                this.fieldSetFlags()[5] = other.fieldSetFlags()[5];
            }

        }

        private Builder(Universe other) {
            super(Universe.SCHEMA$);
            if (isValidValue(this.fields()[0], other.universeId)) {
                this.universeId = (Long)this.data().deepCopy(this.fields()[0].schema(), other.universeId);
                this.fieldSetFlags()[0] = true;
            }

            if (isValidValue(this.fields()[1], other.firstWindow)) {
                this.firstWindow = (Window)this.data().deepCopy(this.fields()[1].schema(), other.firstWindow);
                this.fieldSetFlags()[1] = true;
            }

            this.firstWindowBuilder = null;
            if (isValidValue(this.fields()[2], other.members)) {
                this.members = (List)this.data().deepCopy(this.fields()[2].schema(), other.members);
                this.fieldSetFlags()[2] = true;
            }

            if (isValidValue(this.fields()[3], other.minimalSize)) {
                this.minimalSize = (Integer)this.data().deepCopy(this.fields()[3].schema(), other.minimalSize);
                this.fieldSetFlags()[3] = true;
            }

            if (isValidValue(this.fields()[4], other.alpha)) {
                this.alpha = (Double)this.data().deepCopy(this.fields()[4].schema(), other.alpha);
                this.fieldSetFlags()[4] = true;
            }

            if (isValidValue(this.fields()[5], other.delta)) {
                this.delta = (Double)this.data().deepCopy(this.fields()[5].schema(), other.delta);
                this.fieldSetFlags()[5] = true;
            }

        }

        public long getUniverseId() {
            return this.universeId;
        }

        public Builder setUniverseId(long value) {
            this.validate(this.fields()[0], value);
            this.universeId = value;
            this.fieldSetFlags()[0] = true;
            return this;
        }

        public boolean hasUniverseId() {
            return this.fieldSetFlags()[0];
        }

        public Builder clearUniverseId() {
            this.fieldSetFlags()[0] = false;
            return this;
        }

        public Window getFirstWindow() {
            return this.firstWindow;
        }

        public Builder setFirstWindow(Window value) {
            this.validate(this.fields()[1], value);
            this.firstWindowBuilder = null;
            this.firstWindow = value;
            this.fieldSetFlags()[1] = true;
            return this;
        }

        public boolean hasFirstWindow() {
            return this.fieldSetFlags()[1];
        }

        public Window.Builder getFirstWindowBuilder() {
            if (this.firstWindowBuilder == null) {
                if (this.hasFirstWindow()) {
                    this.setFirstWindowBuilder(Window.newBuilder(this.firstWindow));
                } else {
                    this.setFirstWindowBuilder(Window.newBuilder());
                }
            }

            return this.firstWindowBuilder;
        }

        public Builder setFirstWindowBuilder(Window.Builder value) {
            this.clearFirstWindow();
            this.firstWindowBuilder = value;
            return this;
        }

        public boolean hasFirstWindowBuilder() {
            return this.firstWindowBuilder != null;
        }

        public Builder clearFirstWindow() {
            this.firstWindow = null;
            this.firstWindowBuilder = null;
            this.fieldSetFlags()[1] = false;
            return this;
        }

        public List<Long> getMembers() {
            return this.members;
        }

        public Builder setMembers(List<Long> value) {
            this.validate(this.fields()[2], value);
            this.members = value;
            this.fieldSetFlags()[2] = true;
            return this;
        }

        public boolean hasMembers() {
            return this.fieldSetFlags()[2];
        }

        public Builder clearMembers() {
            this.members = null;
            this.fieldSetFlags()[2] = false;
            return this;
        }

        public int getMinimalSize() {
            return this.minimalSize;
        }

        public Builder setMinimalSize(int value) {
            this.validate(this.fields()[3], value);
            this.minimalSize = value;
            this.fieldSetFlags()[3] = true;
            return this;
        }

        public boolean hasMinimalSize() {
            return this.fieldSetFlags()[3];
        }

        public Builder clearMinimalSize() {
            this.fieldSetFlags()[3] = false;
            return this;
        }

        public double getAlpha() {
            return this.alpha;
        }

        public Builder setAlpha(double value) {
            this.validate(this.fields()[4], value);
            this.alpha = value;
            this.fieldSetFlags()[4] = true;
            return this;
        }

        public boolean hasAlpha() {
            return this.fieldSetFlags()[4];
        }

        public Builder clearAlpha() {
            this.fieldSetFlags()[4] = false;
            return this;
        }

        public double getDelta() {
            return this.delta;
        }

        public Builder setDelta(double value) {
            this.validate(this.fields()[5], value);
            this.delta = value;
            this.fieldSetFlags()[5] = true;
            return this;
        }

        public boolean hasDelta() {
            return this.fieldSetFlags()[5];
        }

        public Builder clearDelta() {
            this.fieldSetFlags()[5] = false;
            return this;
        }

        public Universe build() {
            try {
                Universe record = new Universe();
                record.universeId = this.fieldSetFlags()[0] ? this.universeId : (Long)this.defaultValue(this.fields()[0]);
                if (this.firstWindowBuilder != null) {
                    try {
                        record.firstWindow = this.firstWindowBuilder.build();
                    } catch (AvroMissingFieldException var3) {
                        var3.addParentField(record.getSchema().getField("firstWindow"));
                        throw var3;
                    }
                } else {
                    record.firstWindow = this.fieldSetFlags()[1] ? this.firstWindow : (Window)this.defaultValue(this.fields()[1]);
                }

                record.members = this.fieldSetFlags()[2] ? this.members : (List)this.defaultValue(this.fields()[2]);
                record.minimalSize = this.fieldSetFlags()[3] ? this.minimalSize : (Integer)this.defaultValue(this.fields()[3]);
                record.alpha = this.fieldSetFlags()[4] ? this.alpha : (Double)this.defaultValue(this.fields()[4]);
                record.delta = this.fieldSetFlags()[5] ? this.delta : (Double)this.defaultValue(this.fields()[5]);
                return record;
            } catch (AvroMissingFieldException var4) {
                throw var4;
            } catch (Exception var5) {
                throw new AvroRuntimeException(var5);
            }
        }
    }
}

