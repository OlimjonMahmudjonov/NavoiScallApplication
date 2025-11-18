package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.repository.AttachRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckPhotoRepository;
import uz.tenzorsoft.scaleapplication.repository.TruckRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TruckPhotoService {

    @Autowired
    private TruckPhotoRepository truckPhotosRepository;
    private final TruckRepository truckRepository;
    private final AttachRepository attachRepository;
    private final TruckPhotoRepository truckPhotoRepository;

    /**
     * Kirish fotosini qo'shish
     *
     * @param truckId Truck ID
     * @param attachId Attach ID (S3 URL bilan)
     */
    @Transactional
    public void addEntrancePhoto(Long truckId, Long attachId) {
        log.info("📸 Kirish fotosini qo'shish: TruckID={}, AttachID={}", truckId, attachId);

        try {
            TruckEntity truck = truckRepository.findById(truckId)
                    .orElseThrow(() -> new RuntimeException("Truck topilmadi: " + truckId));

            AttachEntity attach = attachRepository.findById(attachId)
                    .orElseThrow(() -> new RuntimeException("Attach topilmadi: " + attachId));

            TruckPhotosEntity photo = new TruckPhotosEntity();
            photo.setTruckPhoto(attach);
            photo.setAttachStatus(AttachStatus.ENTRANCE_PHOTO);
            photo.setTruck(truck);

            TruckPhotosEntity saved = truckPhotoRepository.save(photo);

            log.info("✅ Kirish fotosi saqlandi: PhotoID={}, TruckID={}, AttachID={}, S3_URL={}",
                    saved.getId(), truckId, attachId, attach.getPath());

        } catch (Exception e) {
            log.error("❌ Kirish fotosini saqlashda xatolik: TruckID={}, AttachID={}, Error={}",
                    truckId, attachId, e.getMessage(), e);
            throw new RuntimeException("Kirish fotosini saqlashda xatolik: " + e.getMessage(), e);
        }
    }

    /**
     * Chiqish fotosini qo'shish
     *
     * @param truckId Truck ID
     * @param attachId Attach ID (S3 URL bilan)
     */
    @Transactional
    public void addExitPhoto(Long truckId, Long attachId) {
        log.info("📸 Chiqish fotosini qo'shish: TruckID={}, AttachID={}", truckId, attachId);

        try {
            TruckEntity truck = truckRepository.findById(truckId)
                    .orElseThrow(() -> new RuntimeException("Truck topilmadi: " + truckId));

            AttachEntity attach = attachRepository.findById(attachId)
                    .orElseThrow(() -> new RuntimeException("Attach topilmadi: " + attachId));

            TruckPhotosEntity photo = new TruckPhotosEntity();
            photo.setTruckPhoto(attach);
            photo.setAttachStatus(AttachStatus.EXIT_PHOTO);
            photo.setTruck(truck);

            TruckPhotosEntity saved = truckPhotoRepository.save(photo);

            log.info("✅ Chiqish fotosi saqlandi: PhotoID={}, TruckID={}, AttachID={}, S3_URL={}",
                    saved.getId(), truckId, attachId, attach.getPath());

        } catch (Exception e) {
            log.error("❌ Chiqish fotosini saqlashda xatolik: TruckID={}, AttachID={}, Error={}",
                    truckId, attachId, e.getMessage(), e);
            throw new RuntimeException("Chiqish fotosini saqlashda xatolik: " + e.getMessage(), e);
        }
    }

    /**
     * TruckPhotosEntity ni ID bo'yicha topish
     *
     * @param id TruckPhotosEntity ID
     * @return TruckPhotosEntity
     */
    public TruckPhotosEntity findById(Long id) {
        log.debug("🔍 TruckPhotosEntity qidirilmoqda: ID={}", id);

        TruckPhotosEntity photo = truckPhotosRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ TruckPhoto topilmadi: ID={}", id);
                    return new RuntimeException("Truck photo not found with ID: " + id);
                });

        log.debug("✅ TruckPhotosEntity topildi: ID={}, Status={}", id, photo.getAttachStatus());
        return photo;
    }

    /**
     * AttachEntity bo'yicha AttachStatus ni topish
     *
     * @param attach AttachEntity
     * @return AttachStatus (ENTRANCE_PHOTO, EXIT_PHOTO, ...)
     */
    public AttachStatus findAttachStatus(AttachEntity attach) {
        if (attach == null) {
            log.warn("⚠️ AttachEntity null");
            return null;
        }

        log.debug("🔍 AttachStatus qidirilmoqda: AttachID={}", attach.getId());

        List<TruckPhotosEntity> list = truckPhotosRepository.findByTruckPhotoOrderByCreatedAtDesc(attach);

        if (list.isEmpty()) {
            log.warn("⚠️ TruckPhotos topilmadi: AttachID={}", attach.getId());
            return null;
        }

        AttachStatus status = list.get(0).getAttachStatus();
        log.debug("✅ AttachStatus topildi: AttachID={}, Status={}", attach.getId(), status);
        return status;
    }

    /**
     * AttachEntity bo'yicha TruckPhotosEntity ni topish
     *
     * @param attach AttachEntity
     * @return TruckPhotosEntity (eng yangi)
     */
    public TruckPhotosEntity findByAttach(AttachEntity attach) {
        if (attach == null) {
            log.warn("⚠️ AttachEntity null");
            return null;
        }

        log.debug("🔍 TruckPhotosEntity qidirilmoqda: AttachID={}", attach.getId());

        List<TruckPhotosEntity> list = truckPhotosRepository.findByTruckPhotoOrderByCreatedAtDesc(attach);

        if (list.isEmpty()) {
            log.warn("⚠️ TruckPhotos topilmadi: AttachID={}", attach.getId());
            return null;
        }

        TruckPhotosEntity photo = list.get(0);
        log.debug("✅ TruckPhotosEntity topildi: PhotoID={}, AttachID={}, Status={}",
                photo.getId(), attach.getId(), photo.getAttachStatus());
        return photo;
    }

    /**
     * Truck bo'yicha barcha fotolarni olish
     *
     * @param truckId Truck ID
     * @return List<TruckPhotosEntity>
     */
    public List<TruckPhotosEntity> findByTruckId(Long truckId) {
        log.debug("🔍 Truck fotolari qidirilmoqda: TruckID={}", truckId);

        TruckEntity truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck topilmadi: " + truckId));

        List<TruckPhotosEntity> photos = truck.getTruckPhotos();
        log.debug("✅ {} ta foto topildi: TruckID={}", photos.size(), truckId);
        return photos;
    }

    /**
     * Truck bo'yicha kirish fotolarini olish
     *
     * @param truckId Truck ID
     * @return List<TruckPhotosEntity>
     */
    public List<TruckPhotosEntity> findEntrancePhotosByTruckId(Long truckId) {
        log.debug("🔍 Kirish fotolari qidirilmoqda: TruckID={}", truckId);

        List<TruckPhotosEntity> photos = findByTruckId(truckId).stream()
                .filter(p -> p.getAttachStatus() == AttachStatus.ENTRANCE_PHOTO)
                .toList();

        log.debug("✅ {} ta kirish fotosi topildi: TruckID={}", photos.size(), truckId);
        return photos;
    }

    /**
     * Truck bo'yicha chiqish fotolarini olish
     *
     * @param truckId Truck ID
     * @return List<TruckPhotosEntity>
     */
    public List<TruckPhotosEntity> findExitPhotosByTruckId(Long truckId) {
        log.debug("🔍 Chiqish fotolari qidirilmoqda: TruckID={}", truckId);

        List<TruckPhotosEntity> photos = findByTruckId(truckId).stream()
                .filter(p -> p.getAttachStatus() == AttachStatus.EXIT_PHOTO)
                .toList();

        log.debug("✅ {} ta chiqish fotosi topildi: TruckID={}", photos.size(), truckId);
        return photos;
    }
}