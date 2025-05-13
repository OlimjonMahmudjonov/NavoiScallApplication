package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.ScaleLog;
import uz.tenzorsoft.scaleapplication.repository.ScaleLogRepository;

@Service
@RequiredArgsConstructor
public class ScaleLogService {


    private final ScaleLogRepository scaleLogRepository;

    public ScaleLog save(String data, String value) {
        return scaleLogRepository.save(new ScaleLog(data, value));
    }
}
