package uz.tenzorsoft.scaleapplication.service;

import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.ShlagbaumEntity;
import uz.tenzorsoft.scaleapplication.repository.ShlagbaumRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ShlagbaunStatusService {

    // Buyruq ID si va uning muvaffaqiyatli bajarilganlik holatini saqlaydi
    private final Map<Long, Boolean> commandStatuses = new ConcurrentHashMap<>();
    private final LogService logService;
    private final ShlagbaumRepository shlagbaumRepository;

    public ShlagbaunStatusService(LogService logService, ShlagbaumRepository shlagbaumRepository) {
        this.logService = logService;
        this.shlagbaumRepository = shlagbaumRepository;
    }

    /**
     * Buyruq natijasini saqlaydi.
     * @param commandId Buyruq ID si
     * @param success Muvaffaqiyatlilik holati
     */
//    public void recordStatusCommand(Long commandId, boolean success) {
//        if (commandId == null || commandId <= 0) {
////            System.err.println("Yaroqsiz commandId uchun status saqlanmadi: " + commandId);
//            logService.save(new LogEntity(5L, Instances.truckNumber, "ShlagbaunStatusService: Yaroqsiz commandId uchun status saqlanmadi: " + commandId));
//            return;
//        }
//        commandStatuses.put(commandId, success);
//        System.out.println("ShlagbaunStatusService: Buyruq ID=" + commandId + " uchun status saqlandi: " + success);
//        logService.save(new LogEntity(5L, Instances.truckNumber, "ShlagbaunStatusService: Buyruq ID=" + commandId + " uchun status saqlandi: " + success));
//
//        // Xotirani tozalab turish uchun eski yozuvlarni o'chirish logikasini qo'shish mumkin
//        // Masalan, ma'lum bir vaqtdan keyin yoki Map hajmi oshib ketganda
//        // cleanupOldStatuses(); // Bu metodni implementatsiya qilish kerak bo'ladi
//    }

    /**
     * Berilgan commandId bo'yicha buyruq statusini qaytaradi.
     * @param commandId Buyruq ID si
     * @return Optional<Boolean> holatida status. Agar ID topilmasa, Optional.empty() qaytaradi.
     */
    public Optional<Boolean> getStatusCommand(Long commandId) {
        if (commandId == null) return Optional.empty();
        return Optional.ofNullable(commandStatuses.get(commandId));
    }

    // Eski statuslarni o'chirish uchun (agar kerak bo'lsa)
//     private void cleanupOldStatuses() {
//        // Masalan, 1 soatdan eski yozuvlarni o'chirish
//     }
}