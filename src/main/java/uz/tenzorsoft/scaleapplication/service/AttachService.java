package uz.tenzorsoft.scaleapplication.service;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.AttachmentDto;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.AttachmentResponse;
import uz.tenzorsoft.scaleapplication.repository.AttachRepository;
import uz.tenzorsoft.scaleapplication.sentDataNavoi.S3service.S3Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AttachService implements BaseService<AttachEntity, AttachResponse, Object> {

    private final AttachRepository attachRepository;
    private final TruckService truckService;
    private final TruckPhotoService truckPhotoService;
    private final LogService logService;

    private final S3Service s3Service;

    private static final Logger log = LoggerFactory.getLogger(AttachService.class);

    // Kamera rasmlari uchun: to'g'ridan S3 ga yuklash
    @Transactional(propagation =  Propagation.REQUIRES_NEW)
    public AttachResponse saveToSystem(byte[] file) {
        try {
            if (file == null || file.length == 0) {
                throw new IllegalArgumentException("File bo'sh yoki null");
            }

            // 1. S3 ga yuklash → toza URL
            String s3Url = s3Service.uploadFile(file);

            // 2. AttachEntity yaratish — faqat kerakli fieldlar!
            AttachEntity entity = new AttachEntity();
            entity.setFileName("cam_" + System.currentTimeMillis() + ".jpg");  // bitta nom yetarli
            entity.setPath(s3Url);                                        // S3 URL
            entity.setSize((long) file.length);
            entity.setContentType("image/jpeg");
            entity.setIsSentToCloud(false);

            // 3. DB ga saqlash
            AttachEntity saved = attachRepository.save(entity);

            // 4. Log
            log.info("Kamera rasmi S3 ga yuklandi → AttachID: {}, URL: {}", saved.getId(), s3Url);

            // 5. Response — ID ni qaytarish shart!
            AttachResponse response = new AttachResponse();
            response.setId(saved.getId());      // saveTruck uchun kerak!
            response.setPath(s3Url);            // agar kerak bo‘lsa

            return response;

        } catch (Exception e) {
            log.error("S3 yuklashda xato: {}", e.getMessage(), e);
            logService.save(new LogEntity(5L, Instances.truckNumber, "S3 upload error: " + e.getMessage()));
            throw new RuntimeException("S3 ga yuklanmadi: " + e.getMessage(), e);
        }
    }

    // Agar boshqa joydan MultipartFile kelsa (masalan, frontenddan) — hozircha ishlatilmaydi
    // public AttachResponse saveToSystem(MultipartFile file) { ... } — o'chirilgan

    public AttachEntity findById(Long attachId) {
        return attachRepository.findById(attachId).orElse(null);
    }

    public List<AttachmentResponse> getNotSentData() {
        List<AttachmentResponse> result = new ArrayList<>();
        List<AttachEntity> notSentData = attachRepository.findByIsSentToCloud(false);

        for (AttachEntity attach : notSentData) {
            TruckEntity truck = truckService.findByTruckPhoto(truckPhotoService.findByAttach(attach));
            byte[] imageBytes = getImageBytes(attach.getPath());
            if (imageBytes.length == 0) continue;

            AttachmentResponse response = new AttachmentResponse(
                    truck == null ? null : truck.getIdOnServer(),
                    attach.getOriginalName(),
                    attach.getSize(),
                    attach.getType(),
                    attach.getContentType(),
                    attach.getPath(),
                    null,
                    truckPhotoService.findAttachStatus(attach),
                    attach.getCreatedAt(),
                    imageBytes
            );
            response.setId(attach.getId());
            response.setIdOnServer(attach.getIdOnServer());
            result.add(response);
            break; // Faqat bitta rasm jo'natish uchun
        }
        return result;
    }

    public List<AttachmentDto> getNotSentDataNew() {
        List<AttachmentDto> result = new ArrayList<>();
        List<AttachEntity> notSentData = attachRepository.findByIsSentToCloud(false);

        for (AttachEntity attach : notSentData) {
            TruckEntity truck = truckService.findByTruckPhoto(truckPhotoService.findByAttach(attach));
            byte[] imageBytes = getImageBytes(attach.getPath());
            if (imageBytes.length == 0) continue;

            AttachmentDto response = AttachmentDto.builder()
                    .localId(attach.getId())
                    .originName(attach.getOriginalName())
                    .size(attach.getSize())
                    .type(attach.getType())
                    .contentType(attach.getContentType())
                    .attachStatus(truckPhotoService.findAttachStatus(attach))
                    .bytes(imageBytes)
                    .webViewServerId(truck == null ? null : truck.getIdOnServer())
                    .build();

            result.add(response);
        }
        return result;
    }

    // Faqat S3 dan o'qish
    private byte[] getImageBytes(String path) {
        if (path == null || !path.contains("s3.tenzorsoft.uz")) {
            log.warn("S3 URL emas: {}", path);
            return new byte[0];
        }
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        return s3Service.downloadFile(fileName);
    }

    public byte[] getBytesById(Long id) {
        AttachEntity attach = attachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attach topilmadi: " + id));

        String path = attach.getPath();
        if (path == null || !path.contains("s3.tenzorsoft.uz")) {
            return new byte[0];
        }

        String fileName = path.substring(path.lastIndexOf("/") + 1);
        return s3Service.downloadFile(fileName);
    }

    public void dataSentNew(List<AttachmentDto> notSentData, Map<Long, Long> attachMap, boolean status) {
        if (attachMap == null || attachMap.isEmpty()) return;

        notSentData.forEach(attach -> {
            Long serverId = attachMap.get(attach.getLocalId());
            AttachEntity entity = attachRepository.findById(attach.getLocalId())
                    .orElseThrow(() -> new RuntimeException("Attach topilmadi: " + attach.getLocalId()));
            entity.setIsSentToCloud(status);
            entity.setIdOnServer(serverId);
            attachRepository.save(entity);
        });
    }

    @Override
    public AttachResponse entityToResponse(AttachEntity entity) {
        return new AttachResponse(
                entity.getId(),
                entity.getOriginalName(),
                entity.getFileName(),
                entity.getSize(),
                entity.getType(),
                entity.getContentType(),
                entity.getPath(),
                entity.getCreatedAt()
        );
    }

    @Override
    public AttachEntity requestToEntity(Object request) {
        return null;
    }

    // Test uchun: mahalliy fayl emas, S3 dan foydalaning
    public AttachResponse getTestingImages() {
        throw new RuntimeException("Test rejimida S3 dan foydalaning");
    }

    public AttachResponse getCameraImgTesting() {
        throw new RuntimeException("Test rejimida S3 dan foydalaning");
    }
}
