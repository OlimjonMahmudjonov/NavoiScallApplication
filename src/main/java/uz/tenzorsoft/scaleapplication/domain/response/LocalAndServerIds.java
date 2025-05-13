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
public class LocalAndServerIds {
//            Local id - Server id
    private Map<Long, Long> weighing=new HashMap<>();
    private Map<Long, Long> action=new HashMap<>();
    private Map<Long, Long> attach=new HashMap<>();
    private Map<Long, Long> user=new HashMap<>();
    private Map<Long, Long> data=new HashMap<>();
    private Map<Long, Long> productIds=new HashMap<>();
}
