import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : hongqiangren.
 * @since: 2018/10/30 22:59
 */
@Data
public class TestDeSerializerBean{
    private Integer number;
    @JsonDeserialize(using = NullToZeroDeserializer.class)
    private BigDecimal money;
    @JsonDeserialize(using = NullToZeroDeserializer.class)
    private Long testLong;
    private Long id;
    private BigDecimal balance;
}
