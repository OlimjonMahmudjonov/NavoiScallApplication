package uz.tenzorsoft.scaleapplication.domain.response.sendData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSendResponse extends BaseResponse {
    private String username;
    private String code;
    private String password;
}