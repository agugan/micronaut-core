package io.micronaut.jackson.modules.testclasses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.convert.value.ConvertibleMultiValues;

import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Introspected
public class HTTPCheck {
    private ConvertibleMultiValues<String> headers = ConvertibleMultiValues.empty();

    public ConvertibleMultiValues<String> getHeader() {
        return headers;
    }

    /**
     * @param headers The headers
     */
    @JsonProperty("Header")
    public void setHeaders(Map<CharSequence, List<String>> headers) {
        if (headers == null) {
            this.headers = ConvertibleMultiValues.empty();
        } else {
            this.headers = ConvertibleMultiValues.of(headers);
        }
    }
}
