import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author : hongqiangren.
 * @since: 2018/11/10 10:25
 */
public class CoverTest {


    static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
// to allow serialization of "empty" POJOs (no properties to serialize)
// (without this setting, an exception is thrown in those cases)
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
// to write java.util.Date, Calendar as number (timestamp):
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

// DeserializationFeature for changing how JSON is read as POJOs:

// to prevent exception when encountering unknown property:
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
// to allow coercion of JSON empty String ("") to null Object value:
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }


    @Test
    public void testDataBind() throws IOException {

        MyBean myBean = new MyBean();
        myBean.setTheName("testName");

        String string = mapper.writeValueAsString(myBean);

        MyBean myBean1 = mapper.readValue(string, MyBean.class);
        System.out.println(myBean1);

    }

    @Test
    public void testSerializer() throws JsonProcessingException {
        TestSerializerBean serializerBean = new TestSerializerBean();
        serializerBean.setNumber(0);
        serializerBean.setAge(0);
        serializerBean.setMoney(new BigDecimal(20));

        String string = mapper.writeValueAsString(serializerBean);

        System.out.println(string);
    }

    @Test
    public void testDeSerializer() throws IOException {
        TestDeSerializerBean serializerBean = new TestDeSerializerBean();
        serializerBean.setNumber(5);
        serializerBean.setMoney(new BigDecimal(20));

        String string = mapper.writeValueAsString(serializerBean);

        String testStr = "{\n" +
                "    \"number\": 0,\n" +
                "    \"money\": \"0\",\n" +
                "    \"testLong\": null,\n" +
                "    \"balance\": \"20\",\n" +
                "    \"id\": 0\n" +
                "}";

        TestDeSerializerBean deSerializerBean = mapper.readValue(testStr, TestDeSerializerBean.class);

        System.out.println(deSerializerBean);
    }
}
