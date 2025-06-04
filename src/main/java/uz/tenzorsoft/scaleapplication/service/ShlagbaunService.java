package uz.tenzorsoft.scaleapplication.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.CommandsEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.ShlagbaumEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.StatusEntity;
import uz.tenzorsoft.scaleapplication.domain.request.ShlagbaunDto;
import uz.tenzorsoft.scaleapplication.repository.CommandsRepository;
import uz.tenzorsoft.scaleapplication.repository.ShlagbaumRepository;
import uz.tenzorsoft.scaleapplication.repository.StatusRepository;
import uz.tenzorsoft.scaleapplication.ui.ButtonController;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity; // LogEntity importi
import uz.tenzorsoft.scaleapplication.domain.Instances;      // Instances importi

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ShlagbaunService {

    private final ButtonController buttonController;
    private final StatusRepository statusRepository;
    private final ShlagbaumRepository shlagbaumRepository;
    private final LogService logService; // Log yozish uchun

    public void handleShlagbaunCommand(ShlagbaunDto shlagbaunDto) {
        if (shlagbaunDto == null || shlagbaunDto.getNumber() == null || shlagbaunDto.getStatus() == null || shlagbaunDto.getId() == null) {
            System.err.println("[" + LocalDateTime.now() + "] ShlagbaunDto noto'g'ri yoki bo'sh keldi (id, number, yoki status null).");
            if (logService != null) logService.save(new LogEntity(5L, Instances.truckNumber, "ShlagbaunDto noto'g'ri yoki bo'sh keldi: " + shlagbaunDto));
            return;
        }

        Long actualCommandId = shlagbaunDto.getId(); // Bu haqiqiy commandId

        System.out.println("[" + LocalDateTime.now() + "] Shlagbaun buyrug'i qabul qilindi: CommandID=" + actualCommandId +
                ", Raqam=" + shlagbaunDto.getNumber() +
                ", Status=" + shlagbaunDto.getStatus());

        // Haqiqiy commandId bo'yicha mavjud ShlagbaumEntity ni qidiramiz
//        Optional<ShlagbaumEntity> existingEntityOptional = shlagbaumRepository.findByCommandId(actualCommandId);

        ShlagbaumEntity entityToProcess = new ShlagbaumEntity();
//        if (existingEntityOptional.isPresent()) {
//            entityToProcess = existingEntityOptional.get();
//            System.out.println("[" + LocalDateTime.now() + "] Mavjud ShlagbaumEntity topildi (CommandID: " + actualCommandId + ", DB ID: " + entityToProcess.getId() + "). Yangilanmoqda...");
//        } else {
//            entityToProcess = new ShlagbaumEntity();
//            entityToProcess.setCommandId(actualCommandId); // Yangi yozuv uchun commandId o'rnatiladi
//            System.out.println("[" + LocalDateTime.now() + "] Yangi ShlagbaumEntity yaratilmoqda (CommandID: " + actualCommandId + ")");
//        }

        // Umumiy maydonlarni DTO dan o'rnatish/yangilash
        entityToProcess.setNumber(shlagbaunDto.getNumber());
        entityToProcess.setStatus(shlagbaunDto.getStatus()); // DTO dan kelgan status (ochish/yopish buyrug'i)
        entityToProcess.setServerid(actualCommandId); // serverid ham commandId bilan bir xil bo'lsa

        ShlagbaumEntity savedEntity = shlagbaumRepository.save(entityToProcess);
        System.out.println("[" + LocalDateTime.now() + "] ShlagbaumEntity saqlandi/yangilandi: DB ID=" + savedEntity.getId() + ", CommandID=" + savedEntity.getCommandId());

        // ButtonController ga HAQIQIY commandId ni (DTO dan kelgan ID) yuboramiz
        buttonController.handleShlagbaunAction(
                savedEntity.getNumber(),    // Yoki shlagbaunDto.getNumber()
                savedEntity.getStatus(),    // Yoki shlagbaunDto.getStatus() - qaysi statusni yuborish kerakligiga qarab
                savedEntity.getCommandId()  // BU ENG MUHIMI: savedEntity.getCommandId() actualCommandId ni qaytaradi
        );
    }
}
