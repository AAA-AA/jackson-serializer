import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author : hongqiangren.
 * @since: 2018/10/30 22:55
 */
public class MyBean {
    private String _name;

    // without annotation, we'd get "theName", but we want "name":
    @JsonProperty("name")
    public String getTheName() { return _name; }

    // note: it is enough to add annotation on just getter OR setter;
    // so we can omit it here
    public void setTheName(String n) { _name = n; }
}