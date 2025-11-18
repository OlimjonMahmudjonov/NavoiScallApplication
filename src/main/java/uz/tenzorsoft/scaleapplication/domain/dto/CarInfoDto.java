// CarInfoDto.java (o'zgartirilgan)
package uz.tenzorsoft.scaleapplication.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarInfoDto {
    @JsonProperty("carNumber")
    private String carNumber;

    @JsonProperty("ownerPinfl")
    private String ownerPinfl;

    @JsonProperty("model")
    private String model;

    @JsonProperty("quantity")
    private Double quantity;

    @JsonProperty("driverName")
    private String driverName;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("enterDate")
    private String enterDate; // String sifatida keladi, keyin convert qilamiz
}