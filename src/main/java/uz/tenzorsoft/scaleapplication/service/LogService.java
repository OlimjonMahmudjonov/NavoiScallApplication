package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.repository.LogRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public LogEntity save(LogEntity logEntity) {
        return logRepository.save(logEntity);
    }

    public List<LogEntity> getNotSentLogs() {
        return logRepository.findTop10ByIdOnServer(null);
    }

    public void dataSent(List<LogEntity> notSentLogs, Map<Long, Long> logMap) {
        try{
            if (logMap == null || logMap.isEmpty()) {
                return;
            }
            notSentLogs.forEach(log -> {
                log.setIsSentToCloud(true);
                log.setIdOnServer(logMap.get(log.getId()));
                logRepository.save(log);
            });
        }catch (Exception e){
            System.out.println("Loglarni saqlashda xatolik: ");
            e.printStackTrace();
        }
    }
}
