package uz.tenzorsoft.scaleapplication.sentDataNavoi;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ApiResponse {
    private List<DriverWithTransfersImport> content;
    private Pageable pageable;
    private boolean last;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;

    public boolean isLast() {
        return last;
    }
}