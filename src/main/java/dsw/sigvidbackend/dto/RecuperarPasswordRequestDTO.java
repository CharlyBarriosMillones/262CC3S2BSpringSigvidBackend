package dsw.sigvidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecuperarPasswordRequestDTO {
    private String email;
    private String dni;
    private String nuevaPassword;
}