import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : hongqiangren.
 * @since: 2018/10/30 22:59
 */
@Data
public class TestSerializerBean {
    @JsonSerialize(using =ZeroToNullSerializer.class)
    private Integer number;
    private Integer age;
    private BigDecimal money;
}
