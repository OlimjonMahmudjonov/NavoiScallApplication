package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pageable {
    private int pageNumber;
    private int pageSize;
    private Sort sort;  // Bizning o‘zimizning Sort class
    private long offset;
    private boolean paged;
    private boolean unpaged;
}