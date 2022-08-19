//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ch.ethz.infk.pps.shared.avro.tuple;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.AvroGenerated;
import org.apache.avro.specific.FixedSize;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificFixed;

@FixedSize(32)
@AvroGenerated
public class AESKey extends SpecificFixed {
    private static final long serialVersionUID = 6405172207158142081L;
    public static final Schema SCHEMA$ = (new Schema.Parser()).parse("{\"type\":\"fixed\",\"name\":\"AESKey\",\"namespace\":\"ch.ethz.infk.pps.shared.avro.tuple\",\"size\":32}");
    private static final DatumWriter<AESKey> WRITER$;
    private static final DatumReader<AESKey> READER$;

    public static Schema getClassSchema() {
        return SCHEMA$;
    }

    public Schema getSchema() {
        return SCHEMA$;
    }

    public AESKey() {
    }

    public AESKey(byte[] bytes) {
        super(bytes);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        WRITER$.write(this, SpecificData.getEncoder(out));
    }

    public void readExternal(ObjectInput in) throws IOException {
        READER$.read(this, SpecificData.getDecoder(in));
    }

    static {
        WRITER$ = new SpecificDatumWriter(SCHEMA$);
        READER$ = new SpecificDatumReader(SCHEMA$);
    }
}
