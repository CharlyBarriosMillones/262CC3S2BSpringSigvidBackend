package dsw.sigvidbackend.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectResponse<T> {
    private Boolean success;
    private String message;
    private T object;
}