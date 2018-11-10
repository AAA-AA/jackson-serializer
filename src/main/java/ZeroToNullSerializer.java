import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author : hongqiangren.
 * @since: 2018/10/30 23:00
 */
@JacksonStdImpl
public class ZeroToNullSerializer extends JsonSerializer implements ContextualSerializer {

    private Class<?> type;

    public ZeroToNullSerializer() {

    }

    public ZeroToNullSerializer(final JavaType type) {
        this.type = type == null ? Object.class : type.getRawClass();
    }

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        if (o instanceof Short) {
            if (((Short) o).compareTo((short)0) == 0) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeNumber(((Short) o).shortValue());
            }
        }
        if (o instanceof Integer) {
            if (((Integer) o).intValue() == 0) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeNumber(((Integer) o).intValue());
            }
        }
        if (o instanceof Float) {
            if (((Float) o).compareTo(0f) == 0) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeNumber(((Float) o).floatValue());
            }
        }

        if (o instanceof Double) {
            if (((Double) o).compareTo(0D) == 0) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeNumber(((Double) o).doubleValue());
            }
        }

        if (o instanceof Long) {
            if (((Long) o).compareTo(0L) == 0) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeNumber(((Long) o).longValue());
            }
        }
        if (o instanceof BigDecimal) {
            if (((BigDecimal) o).compareTo(BigDecimal.ZERO) == 0) {
                jsonGenerator.writeNull();
            }else {
                jsonGenerator.writeNumber((BigDecimal) o);
            }
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        return new ZeroToNullSerializer(property.getType());
    }
}
