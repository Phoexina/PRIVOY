//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ch.ethz.infk.pps.shared.avro.tuple;

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
public class ProducerKeyTuple extends SpecificRecordBase implements SpecificRecord {
    private static final long serialVersionUID = -3979944597950854284L;
    public static final Schema SCHEMA$ = (new Schema.Parser()).parse("{\"type\":\"record\",\"name\":\"ProducerKeyTuple\",\"namespace\":\"ch.ethz.infk.pps.shared.avro.tuple\",\"fields\":[{\"name\":\"producerId\",\"type\":\"long\"},{\"name\":\"certificate\",\"type\":\"bytes\"},{\"name\":\"privateKey\",\"type\":\"bytes\"},{\"name\":\"sharedFileKey\",\"type\":{\"type\":\"fixed\",\"name\":\"AESKey\",\"size\":32}},{\"name\":\"dummyFileKey\",\"type\":\"AESKey\"},{\"name\":\"heacKey\",\"type\":\"AESKey\"}]}");
    private static SpecificData MODEL$ = new SpecificData();
    private static final BinaryMessageEncoder<ProducerKeyTuple> ENCODER;
    private static final BinaryMessageDecoder<ProducerKeyTuple> DECODER;
    /** @deprecated */
    @Deprecated
    public long producerId;
    /** @deprecated */
    @Deprecated
    public ByteBuffer certificate;
    /** @deprecated */
    @Deprecated
    public ByteBuffer privateKey;
    /** @deprecated */
    @Deprecated
    public AESKey sharedFileKey;
    /** @deprecated */
    @Deprecated
    public AESKey dummyFileKey;
    /** @deprecated */
    @Deprecated
    public AESKey heacKey;
    private static final DatumWriter<ProducerKeyTuple> WRITER$;
    private static final DatumReader<ProducerKeyTuple> READER$;

    public static Schema getClassSchema() {
        return SCHEMA$;
    }

    public static BinaryMessageEncoder<ProducerKeyTuple> getEncoder() {
        return ENCODER;
    }

    public static BinaryMessageDecoder<ProducerKeyTuple> getDecoder() {
        return DECODER;
    }

    public static BinaryMessageDecoder<ProducerKeyTuple> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder(MODEL$, SCHEMA$, resolver);
    }

    public ByteBuffer toByteBuffer() throws IOException {
        return ENCODER.encode(this);
    }

    public static ProducerKeyTuple fromByteBuffer(ByteBuffer b) throws IOException {
        return (ProducerKeyTuple)DECODER.decode(b);
    }

    public ProducerKeyTuple() {
    }

    public ProducerKeyTuple(Long producerId, ByteBuffer certificate, ByteBuffer privateKey, AESKey sharedFileKey, AESKey dummyFileKey, AESKey heacKey) {
        this.producerId = producerId;
        this.certificate = certificate;
        this.privateKey = privateKey;
        this.sharedFileKey = sharedFileKey;
        this.dummyFileKey = dummyFileKey;
        this.heacKey = heacKey;
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
                return this.producerId;
            case 1:
                return this.certificate;
            case 2:
                return this.privateKey;
            case 3:
                return this.sharedFileKey;
            case 4:
                return this.dummyFileKey;
            case 5:
                return this.heacKey;
            default:
                throw new AvroRuntimeException("Bad index");
        }
    }

    public void put(int field$, Object value$) {
        switch (field$) {
            case 0:
                this.producerId = (Long)value$;
                break;
            case 1:
                this.certificate = (ByteBuffer)value$;
                break;
            case 2:
                this.privateKey = (ByteBuffer)value$;
                break;
            case 3:
                this.sharedFileKey = (AESKey)value$;
                break;
            case 4:
                this.dummyFileKey = (AESKey)value$;
                break;
            case 5:
                this.heacKey = (AESKey)value$;
                break;
            default:
                throw new AvroRuntimeException("Bad index");
        }

    }

    public long getProducerId() {
        return this.producerId;
    }

    public void setProducerId(long value) {
        this.producerId = value;
    }

    public ByteBuffer getCertificate() {
        return this.certificate;
    }

    public void setCertificate(ByteBuffer value) {
        this.certificate = value;
    }

    public ByteBuffer getPrivateKey() {
        return this.privateKey;
    }

    public void setPrivateKey(ByteBuffer value) {
        this.privateKey = value;
    }

    public AESKey getSharedFileKey() {
        return this.sharedFileKey;
    }

    public void setSharedFileKey(AESKey value) {
        this.sharedFileKey = value;
    }

    public AESKey getDummyFileKey() {
        return this.dummyFileKey;
    }

    public void setDummyFileKey(AESKey value) {
        this.dummyFileKey = value;
    }

    public AESKey getHeacKey() {
        return this.heacKey;
    }

    public void setHeacKey(AESKey value) {
        this.heacKey = value;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Builder other) {
        return other == null ? new Builder() : new Builder(other);
    }

    public static Builder newBuilder(ProducerKeyTuple other) {
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
        out.writeLong(this.producerId);
        out.writeBytes(this.certificate);
        out.writeBytes(this.privateKey);
        out.writeFixed(this.sharedFileKey.bytes(), 0, 32);
        out.writeFixed(this.dummyFileKey.bytes(), 0, 32);
        out.writeFixed(this.heacKey.bytes(), 0, 32);
    }

    public void customDecode(ResolvingDecoder in) throws IOException {
        Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
        if (fieldOrder == null) {
            this.producerId = in.readLong();
            this.certificate = in.readBytes(this.certificate);
            this.privateKey = in.readBytes(this.privateKey);
            if (this.sharedFileKey == null) {
                this.sharedFileKey = new AESKey();
            }

            in.readFixed(this.sharedFileKey.bytes(), 0, 32);
            if (this.dummyFileKey == null) {
                this.dummyFileKey = new AESKey();
            }

            in.readFixed(this.dummyFileKey.bytes(), 0, 32);
            if (this.heacKey == null) {
                this.heacKey = new AESKey();
            }

            in.readFixed(this.heacKey.bytes(), 0, 32);
        } else {
            for(int i = 0; i < 6; ++i) {
                switch (fieldOrder[i].pos()) {
                    case 0:
                        this.producerId = in.readLong();
                        break;
                    case 1:
                        this.certificate = in.readBytes(this.certificate);
                        break;
                    case 2:
                        this.privateKey = in.readBytes(this.privateKey);
                        break;
                    case 3:
                        if (this.sharedFileKey == null) {
                            this.sharedFileKey = new AESKey();
                        }

                        in.readFixed(this.sharedFileKey.bytes(), 0, 32);
                        break;
                    case 4:
                        if (this.dummyFileKey == null) {
                            this.dummyFileKey = new AESKey();
                        }

                        in.readFixed(this.dummyFileKey.bytes(), 0, 32);
                        break;
                    case 5:
                        if (this.heacKey == null) {
                            this.heacKey = new AESKey();
                        }

                        in.readFixed(this.heacKey.bytes(), 0, 32);
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

    public static class Builder extends SpecificRecordBuilderBase<ProducerKeyTuple> implements RecordBuilder<ProducerKeyTuple> {
        private long producerId;
        private ByteBuffer certificate;
        private ByteBuffer privateKey;
        private AESKey sharedFileKey;
        private AESKey dummyFileKey;
        private AESKey heacKey;

        private Builder() {
            super(ProducerKeyTuple.SCHEMA$);
        }

        private Builder(Builder other) {
            super(other);
            if (isValidValue(this.fields()[0], other.producerId)) {
                this.producerId = (Long)this.data().deepCopy(this.fields()[0].schema(), other.producerId);
                this.fieldSetFlags()[0] = other.fieldSetFlags()[0];
            }

            if (isValidValue(this.fields()[1], other.certificate)) {
                this.certificate = (ByteBuffer)this.data().deepCopy(this.fields()[1].schema(), other.certificate);
                this.fieldSetFlags()[1] = other.fieldSetFlags()[1];
            }

            if (isValidValue(this.fields()[2], other.privateKey)) {
                this.privateKey = (ByteBuffer)this.data().deepCopy(this.fields()[2].schema(), other.privateKey);
                this.fieldSetFlags()[2] = other.fieldSetFlags()[2];
            }

            if (isValidValue(this.fields()[3], other.sharedFileKey)) {
                this.sharedFileKey = (AESKey)this.data().deepCopy(this.fields()[3].schema(), other.sharedFileKey);
                this.fieldSetFlags()[3] = other.fieldSetFlags()[3];
            }

            if (isValidValue(this.fields()[4], other.dummyFileKey)) {
                this.dummyFileKey = (AESKey)this.data().deepCopy(this.fields()[4].schema(), other.dummyFileKey);
                this.fieldSetFlags()[4] = other.fieldSetFlags()[4];
            }

            if (isValidValue(this.fields()[5], other.heacKey)) {
                this.heacKey = (AESKey)this.data().deepCopy(this.fields()[5].schema(), other.heacKey);
                this.fieldSetFlags()[5] = other.fieldSetFlags()[5];
            }

        }

        private Builder(ProducerKeyTuple other) {
            super(ProducerKeyTuple.SCHEMA$);
            if (isValidValue(this.fields()[0], other.producerId)) {
                this.producerId = (Long)this.data().deepCopy(this.fields()[0].schema(), other.producerId);
                this.fieldSetFlags()[0] = true;
            }

            if (isValidValue(this.fields()[1], other.certificate)) {
                this.certificate = (ByteBuffer)this.data().deepCopy(this.fields()[1].schema(), other.certificate);
                this.fieldSetFlags()[1] = true;
            }

            if (isValidValue(this.fields()[2], other.privateKey)) {
                this.privateKey = (ByteBuffer)this.data().deepCopy(this.fields()[2].schema(), other.privateKey);
                this.fieldSetFlags()[2] = true;
            }

            if (isValidValue(this.fields()[3], other.sharedFileKey)) {
                this.sharedFileKey = (AESKey)this.data().deepCopy(this.fields()[3].schema(), other.sharedFileKey);
                this.fieldSetFlags()[3] = true;
            }

            if (isValidValue(this.fields()[4], other.dummyFileKey)) {
                this.dummyFileKey = (AESKey)this.data().deepCopy(this.fields()[4].schema(), other.dummyFileKey);
                this.fieldSetFlags()[4] = true;
            }

            if (isValidValue(this.fields()[5], other.heacKey)) {
                this.heacKey = (AESKey)this.data().deepCopy(this.fields()[5].schema(), other.heacKey);
                this.fieldSetFlags()[5] = true;
            }

        }

        public long getProducerId() {
            return this.producerId;
        }

        public Builder setProducerId(long value) {
            this.validate(this.fields()[0], value);
            this.producerId = value;
            this.fieldSetFlags()[0] = true;
            return this;
        }

        public boolean hasProducerId() {
            return this.fieldSetFlags()[0];
        }

        public Builder clearProducerId() {
            this.fieldSetFlags()[0] = false;
            return this;
        }

        public ByteBuffer getCertificate() {
            return this.certificate;
        }

        public Builder setCertificate(ByteBuffer value) {
            this.validate(this.fields()[1], value);
            this.certificate = value;
            this.fieldSetFlags()[1] = true;
            return this;
        }

        public boolean hasCertificate() {
            return this.fieldSetFlags()[1];
        }

        public Builder clearCertificate() {
            this.certificate = null;
            this.fieldSetFlags()[1] = false;
            return this;
        }

        public ByteBuffer getPrivateKey() {
            return this.privateKey;
        }

        public Builder setPrivateKey(ByteBuffer value) {
            this.validate(this.fields()[2], value);
            this.privateKey = value;
            this.fieldSetFlags()[2] = true;
            return this;
        }

        public boolean hasPrivateKey() {
            return this.fieldSetFlags()[2];
        }

        public Builder clearPrivateKey() {
            this.privateKey = null;
            this.fieldSetFlags()[2] = false;
            return this;
        }

        public AESKey getSharedFileKey() {
            return this.sharedFileKey;
        }

        public Builder setSharedFileKey(AESKey value) {
            this.validate(this.fields()[3], value);
            this.sharedFileKey = value;
            this.fieldSetFlags()[3] = true;
            return this;
        }

        public boolean hasSharedFileKey() {
            return this.fieldSetFlags()[3];
        }

        public Builder clearSharedFileKey() {
            this.sharedFileKey = null;
            this.fieldSetFlags()[3] = false;
            return this;
        }

        public AESKey getDummyFileKey() {
            return this.dummyFileKey;
        }

        public Builder setDummyFileKey(AESKey value) {
            this.validate(this.fields()[4], value);
            this.dummyFileKey = value;
            this.fieldSetFlags()[4] = true;
            return this;
        }

        public boolean hasDummyFileKey() {
            return this.fieldSetFlags()[4];
        }

        public Builder clearDummyFileKey() {
            this.dummyFileKey = null;
            this.fieldSetFlags()[4] = false;
            return this;
        }

        public AESKey getHeacKey() {
            return this.heacKey;
        }

        public Builder setHeacKey(AESKey value) {
            this.validate(this.fields()[5], value);
            this.heacKey = value;
            this.fieldSetFlags()[5] = true;
            return this;
        }

        public boolean hasHeacKey() {
            return this.fieldSetFlags()[5];
        }

        public Builder clearHeacKey() {
            this.heacKey = null;
            this.fieldSetFlags()[5] = false;
            return this;
        }

        public ProducerKeyTuple build() {
            try {
                ProducerKeyTuple record = new ProducerKeyTuple();
                record.producerId = this.fieldSetFlags()[0] ? this.producerId : (Long)this.defaultValue(this.fields()[0]);
                record.certificate = this.fieldSetFlags()[1] ? this.certificate : (ByteBuffer)this.defaultValue(this.fields()[1]);
                record.privateKey = this.fieldSetFlags()[2] ? this.privateKey : (ByteBuffer)this.defaultValue(this.fields()[2]);
                record.sharedFileKey = this.fieldSetFlags()[3] ? this.sharedFileKey : (AESKey)this.defaultValue(this.fields()[3]);
                record.dummyFileKey = this.fieldSetFlags()[4] ? this.dummyFileKey : (AESKey)this.defaultValue(this.fields()[4]);
                record.heacKey = this.fieldSetFlags()[5] ? this.heacKey : (AESKey)this.defaultValue(this.fields()[5]);
                return record;
            } catch (AvroMissingFieldException var2) {
                throw var2;
            } catch (Exception var3) {
                throw new AvroRuntimeException(var3);
            }
        }
    }
}
