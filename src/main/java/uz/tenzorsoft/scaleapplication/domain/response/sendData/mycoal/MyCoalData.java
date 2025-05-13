package uz.tenzorsoft.scaleapplication.domain.response.sendData.mycoal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MyCoalData {
    private Long id;
    private Long np;
    private Long tarozi_id;
    private String rfid;
    private String avto_number;
    private String ful_name;
    private String tex_pass_number;
    private String org_name_buyer;
    private String org_name_seller;
    private ProductResponse product;
    private CheckResponse check;
    private AccordResponse accord;
    private Doverennost doverennost;
    private Heft heft;
}
