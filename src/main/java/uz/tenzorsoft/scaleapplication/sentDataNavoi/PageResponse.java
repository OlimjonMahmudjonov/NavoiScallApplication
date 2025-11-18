package uz.tenzorsoft.scaleapplication.sentDataNavoi;// PageResponse.java

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uz.tenzorsoft.scaleapplication.domain.dto.CarInfoDto;

import java.util.List;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse {

    @JsonProperty("content")
    private List<CarInfoDto> content;

    @JsonProperty("last")
    private boolean last;

    @JsonProperty("number")
    private int number;
}