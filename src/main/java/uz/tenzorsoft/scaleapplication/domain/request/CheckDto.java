package uz.tenzorsoft.scaleapplication.domain.request;

//package uz.tenzorsoft.scaleapplication.api.dto; // Yangi paket ochishingiz mumkin

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckDto {
    private Long id;
    private Boolean success; // Boolean o'rniga boolean ham ishlatsa bo'ladi, lekin serverda Boolean ekan
}