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
public class CertificateTuple extends SpecificRecordBase implements SpecificRecord {
    private static final long serialVersionUID = -6967746068822473185L;
    public static final Schema SCHEMA$ = (new Schema.Parser()).parse("{\"type\":\"record\",\"name\":\"CertificateTuple\",\"namespace\":\"ch.ethz.infk.pps.shared.avro.tuple\",\"fields\":[{\"name\":\"producerId\",\"type\":\"long\"},{\"name\":\"certificate\",\"type\":\"bytes\"}]}");
    private static SpecificData MODEL$ = new SpecificData();
    private static final BinaryMessageEncoder<CertificateTuple> ENCODER;
    private static final BinaryMessageDecoder<CertificateTuple> DECODER;
    /** @deprecated */
    @Deprecated
    public long producerId;
    /** @deprecated */
    @Deprecated
    public ByteBuffer certificate;
    private static final DatumWriter<CertificateTuple> WRITER$;
    private static final DatumReader<CertificateTuple> READER$;

    public static Schema getClassSchema() {
        return SCHEMA$;
    }

    public static BinaryMessageEncoder<CertificateTuple> getEncoder() {
        return ENCODER;
    }

    public static BinaryMessageDecoder<CertificateTuple> getDecoder() {
        return DECODER;
    }

    public static BinaryMessageDecoder<CertificateTuple> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder(MODEL$, SCHEMA$, resolver);
    }

    public ByteBuffer toByteBuffer() throws IOException {
        return ENCODER.encode(this);
    }

    public static CertificateTuple fromByteBuffer(ByteBuffer b) throws IOException {
        return (CertificateTuple)DECODER.decode(b);
    }

    public CertificateTuple() {
    }

    public CertificateTuple(Long producerId, ByteBuffer certificate) {
        this.producerId = producerId;
        this.certificate = certificate;
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Builder other) {
        return other == null ? new Builder() : new Builder(other);
    }

    public static Builder newBuilder(CertificateTuple other) {
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
    }

    public void customDecode(ResolvingDecoder in) throws IOException {
        Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
        if (fieldOrder == null) {
            this.producerId = in.readLong();
            this.certificate = in.readBytes(this.certificate);
        } else {
            for(int i = 0; i < 2; ++i) {
                switch (fieldOrder[i].pos()) {
                    case 0:
                        this.producerId = in.readLong();
                        break;
                    case 1:
                        this.certificate = in.readBytes(this.certificate);
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

    public static class Builder extends SpecificRecordBuilderBase<CertificateTuple> implements RecordBuilder<CertificateTuple> {
        private long producerId;
        private ByteBuffer certificate;

        private Builder() {
            super(CertificateTuple.SCHEMA$);
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

        }

        private Builder(CertificateTuple other) {
            super(CertificateTuple.SCHEMA$);
            if (isValidValue(this.fields()[0], other.producerId)) {
                this.producerId = (Long)this.data().deepCopy(this.fields()[0].schema(), other.producerId);
                this.fieldSetFlags()[0] = true;
            }

            if (isValidValue(this.fields()[1], other.certificate)) {
                this.certificate = (ByteBuffer)this.data().deepCopy(this.fields()[1].schema(), other.certificate);
                this.fieldSetFlags()[1] = true;
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

        public CertificateTuple build() {
            try {
                CertificateTuple record = new CertificateTuple();
                record.producerId = this.fieldSetFlags()[0] ? this.producerId : (Long)this.defaultValue(this.fields()[0]);
                record.certificate = this.fieldSetFlags()[1] ? this.certificate : (ByteBuffer)this.defaultValue(this.fields()[1]);
                return record;
            } catch (AvroMissingFieldException var2) {
                throw var2;
            } catch (Exception var3) {
                throw new AvroRuntimeException(var3);
            }
        }
    }
}
