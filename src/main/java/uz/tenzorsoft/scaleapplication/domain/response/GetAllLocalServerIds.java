package uz.tenzorsoft.scaleapplication.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetAllLocalServerIds {

    private Map<Long, Long> webView = new HashMap<>();
    private Map<Long, Long> viewAttachment = new HashMap<>();
}
