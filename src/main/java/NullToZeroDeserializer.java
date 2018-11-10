import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * @author : hongqiangren.
 * @since: 2018/10/30 23:10
 */
@JacksonStdImpl
public class NullToZeroDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private Class<?> type;

    private JavaType javaType;

    public Class<?> handledType() {
        return this.type;
    }

    public NullToZeroDeserializer() {
    }

    public NullToZeroDeserializer(Class<?> vc) {
        this.type = vc;
    }

    public NullToZeroDeserializer(JavaType valueType) {
        javaType = valueType;
        this.type = valueType == null ? Object.class : valueType.getRawClass();
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this.type == Integer.class) {
            return handleInteger(p, ctxt);
        }
        if (this.type == Long.class) {
            return handleLong(p, ctxt);
        }
        if (this.type == BigDecimal.class) {
            return handleBigDecimal(p, ctxt);
        }
        if (this.type == Double.class) {
            return handleDouble(p, ctxt);
        }
        if (this.type == Float.class) {
            return handleFloat(p, ctxt);
        }
        if (this.type == Short.class) {
            return handleShort(p, ctxt);
        }
        throw new RuntimeException("反序列化错误，类型" + type.toString() + "+不支持数值类型的反序列化");
    }

    private Object handleShort(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return p.getShortValue();
        }
        if (t == JsonToken.VALUE_STRING) { // let's do implicit re-parse
            String text = p.getText().trim();
            int len = text.length();
            if (len == 0) {
                return 0;
            }
            if (Clean.isBlank(text)) {
                return 0;
            }
            int value;
            try {
                value = NumberInput.parseInt(text);
            } catch (IllegalArgumentException iae) {
                return (Short) ctxt.handleWeirdStringValue(type, text,
                        "not a valid Short value");
            }
            // So far so good: but does it fit?
            if (_shortOverflow(value)) {
                return (Short) ctxt.handleWeirdStringValue(type, text,
                        "overflow, value cannot be represented as 16-bit value");
            }
            return Short.valueOf((short) value);
        }
        if (t == JsonToken.VALUE_NUMBER_FLOAT) {
            if (!ctxt.isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT)) {
                _failDoubleToIntCoercion(p, ctxt, "Short");
            }
            return p.getShortValue();
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0;
        }
        if (t == JsonToken.START_ARRAY) {
            throw new RuntimeException("NullToZeroDeserializer handleShort error, encounter token " + JsonTokenId.ID_START_ARRAY);
        }
        return (Short) ctxt.handleUnexpectedToken(type, p);
    }

    private Object handleFloat(JsonParser p, DeserializationContext ctxt) throws IOException {
        // We accept couple of different types; obvious ones first:
        JsonToken t = p.getCurrentToken();

        if (t == JsonToken.VALUE_NUMBER_FLOAT || t == JsonToken.VALUE_NUMBER_INT) { // coercing should work too
            return p.getFloatValue();
        }
        // And finally, let's allow Strings to be converted too
        if (t == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if ((text.length() == 0)) {
                return 0F;
            }
            if (Clean.isBlank(text)) {
                return 0F;
            }
            switch (text.charAt(0)) {
                case 'I':
                    if (_isPosInf(text)) {
                        return Float.POSITIVE_INFINITY;
                    }
                    break;
                case 'N':
                    if (_isNaN(text)) {
                        return Float.NaN;
                    }
                    break;
                case '-':
                    if (_isNegInf(text)) {
                        return Float.NEGATIVE_INFINITY;
                    }
                    break;
            }
            try {
                return Float.parseFloat(text);
            } catch (IllegalArgumentException iae) {
            }
            return (Float) ctxt.handleWeirdStringValue(type, text,
                    "not a valid Float value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0F;
        }
        if (t == JsonToken.START_ARRAY) {
            throw new RuntimeException("NullToZeroDeserializer handleFloat error, encounter token " + JsonTokenId.ID_START_ARRAY);
        }
        // Otherwise, no can do:
        return (Float) ctxt.handleUnexpectedToken(type, p);
    }

    private Object handleDouble(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) { // coercing should work too
            return p.getDoubleValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = p.getText().trim();
            if ((text.length() == 0)) {
                return 0D;
            }
            if (Clean.isBlank(text)) {
                return 0D;
            }
            switch (text.charAt(0)) {
                case 'I':
                    if (_isPosInf(text)) {
                        return Double.POSITIVE_INFINITY;
                    }
                    break;
                case 'N':
                    if (_isNaN(text)) {
                        return Double.NaN;
                    }
                    break;
                case '-':
                    if (_isNaN(text)) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    break;
            }
            try {
                if (NumberInput.NASTY_SMALL_DOUBLE.equals(text)) {
                    return Double.MIN_NORMAL; // since 2.7; was MIN_VALUE prior
                }
                return Double.parseDouble(text);
            } catch (IllegalArgumentException iae) {
            }
            return (Double) ctxt.handleWeirdStringValue(type, text,
                    "not a valid Double value");
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0L;
        }
        if (t == JsonToken.START_ARRAY) {
            throw new RuntimeException("NullToZeroDeserializer handleDouble error, encounter token " + JsonTokenId.ID_START_ARRAY);
        }
        // Otherwise, no can do:
        return (Double) ctxt.handleUnexpectedToken(type, p);
    }

    private Object handleLong(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return p.getLongValue();
        }
        switch (p.getCurrentTokenId()) {
            // NOTE: caller assumed to usually check VALUE_NUMBER_INT in fast path
            case JsonTokenId.ID_NUMBER_INT:
                return p.getLongValue();
            case JsonTokenId.ID_NUMBER_FLOAT:
                if (!ctxt.isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT)) {
                    _failDoubleToIntCoercion(p, ctxt, "Long");
                }
                return p.getValueAsLong();
            case JsonTokenId.ID_STRING:
                String text = p.getText().trim();
                if (text.length() == 0) {
                    return 0;
                }
                if (Clean.isBlank(text)) {
                    return 0L;
                }
                // let's allow Strings to be converted too
                try {
                    return Long.valueOf(NumberInput.parseLong(text));
                } catch (IllegalArgumentException iae) {
                }
                return (Long) ctxt.handleWeirdStringValue(type, text,
                        "not a valid Long value");
            // fall-through
            case JsonTokenId.ID_NULL:
                return 0L;
            case JsonTokenId.ID_START_ARRAY:
                throw new RuntimeException("NullToZeroDeserializer handleLong error, encounter token " + JsonTokenId.ID_START_ARRAY);
        }
        // Otherwise, no can do:
        return (Long) ctxt.handleUnexpectedToken(type, p);
    }

    private Object handleInteger(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return p.getIntValue();
        }
        switch (p.getCurrentTokenId()) {
            // NOTE: caller assumed to usually check VALUE_NUMBER_INT in fast path
            case JsonTokenId.ID_NUMBER_INT:
                return Integer.valueOf(p.getIntValue());
            case JsonTokenId.ID_NUMBER_FLOAT: // coercing may work too
                if (!ctxt.isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT)) {
                    _failDoubleToIntCoercion(p, ctxt, "Integer");
                }
                return Integer.valueOf(p.getValueAsInt());
            case JsonTokenId.ID_STRING: // let's do implicit re-parse
                String text = p.getText().trim();
                int len = text.length();
                if (len == 0) {
                    return 0;
                }
                if (Clean.isBlank(text)) {
                    return 0;
                }
                try {
                    if (len > 9) {
                        long l = Long.parseLong(text);
                        return Integer.valueOf((int) l);
                    }
                    return Integer.valueOf(NumberInput.parseInt(text));
                } catch (IllegalArgumentException iae) {
                    return (Integer) ctxt.handleWeirdStringValue(type, text,
                            "not a valid Integer value");
                }
            case JsonTokenId.ID_NULL:
                return 0;
            case JsonTokenId.ID_START_ARRAY:
                throw new RuntimeException("NullToZeroDeserializer handleInteger error, encounter token " + JsonTokenId.ID_START_ARRAY);
        }
        // Otherwise, no can do:
        return (Integer) ctxt.handleUnexpectedToken(type, p);
    }

    private Object handleBigDecimal(JsonParser p, DeserializationContext ctxt) throws IOException {
        switch (p.getCurrentTokenId()) {
            case JsonTokenId.ID_NUMBER_INT:
            case JsonTokenId.ID_NUMBER_FLOAT:
                return p.getDecimalValue();
            case JsonTokenId.ID_STRING:
                String text = p.getText().trim();
                // note: no need to call `coerce` as this is never primitive
                if (text == null || text.length() == 0) {
                    return getNullValue(ctxt);
                }
                try {
                    return new BigDecimal(text);
                } catch (IllegalArgumentException iae) {
                }
                return (BigDecimal) ctxt.handleWeirdStringValue(type, text,
                        "not a valid representation");
            case JsonTokenId.ID_START_ARRAY:
                throw new RuntimeException("NullToZeroDeserializer handleBigDecimal error, encounter token " + JsonTokenId.ID_START_ARRAY);
        }
        // Otherwise, no can do:
        return (BigDecimal) ctxt.handleUnexpectedToken(type, p);
    }

    public Object getEmptyValue(DeserializationContext ctxt) {
        return handNullAndEmpty();
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return handNullAndEmpty();
    }

    private Object handNullAndEmpty() {
        if (this.type == BigDecimal.class) {
            return BigDecimal.ZERO;
        }
        if (this.type == Long.class) {
            return 0l;
        }
        if (this.type == Short.class) {
            return (short)0;
        }
        if (this.type == Float.class) {
            return 0f;
        }
        if (this.type == Double.class) {
            return 0d;
        }
        return 0;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {

        return new NullToZeroDeserializer(beanProperty.getType());
    }

    protected void _failDoubleToIntCoercion(JsonParser p, DeserializationContext ctxt,
                                            String type) throws IOException {
        ctxt.reportInputMismatch(handledType(),
                "Cannot coerce a floating-point value ('%s') into %s (enable `DeserializationFeature.ACCEPT_FLOAT_AS_INT` to allow)",
                p.getValueAsString(), type);
    }


    protected void _reportFailedNullCoerce(DeserializationContext ctxt, boolean state, Enum<?> feature,
                                           String inputDesc) throws JsonMappingException {
        String enableDesc = state ? "enable" : "disable";
        ctxt.reportInputMismatch(this, "Cannot coerce %s to Null value %s (%s `%s.%s` to allow)",
                inputDesc, _coercedTypeDesc(), enableDesc, feature.getClass().getSimpleName(), feature.name());
    }

    protected String _coercedTypeDesc() {
        boolean structured;
        String typeDesc;

        JavaType t = javaType;
        if ((t != null) && !t.isPrimitive()) {
            structured = (t.isContainerType() || t.isReferenceType());
            // 21-Jul-2017, tatu: Probably want to change this (JavaType.toString() not very good) but...
            typeDesc = "'" + t.toString() + "'";
        } else {
            Class<?> cls = handledType();
            structured = cls.isArray() || Collection.class.isAssignableFrom(cls)
                    || Map.class.isAssignableFrom(cls);
            typeDesc = ClassUtil.nameOf(cls);
        }
        if (structured) {
            return "as content of type " + typeDesc;
        }
        return "for type " + typeDesc;
    }

    protected final boolean _isNegInf(String text) {
        return "-Infinity".equals(text) || "-INF".equals(text);
    }

    protected final boolean _isPosInf(String text) {
        return "Infinity".equals(text) || "INF".equals(text);
    }

    protected final boolean _isNaN(String text) {
        return "NaN".equals(text);
    }

    protected final boolean _shortOverflow(int value) {
        return (value < Short.MIN_VALUE || value > Short.MAX_VALUE);
    }

}
