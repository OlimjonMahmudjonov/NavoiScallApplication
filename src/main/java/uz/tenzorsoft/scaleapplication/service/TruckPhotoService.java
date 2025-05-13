package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.repository.TruckPhotoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TruckPhotoService {

    @Autowired
    private TruckPhotoRepository truckPhotosRepository;

    public TruckPhotosEntity findById(Long id) {
        return truckPhotosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Truck photo not found with ID: " + id));
    }

    public AttachStatus findAttachStatus(AttachEntity attach) {
        List<TruckPhotosEntity> list = truckPhotosRepository.findByTruckPhotoOrderByCreatedAtDesc(attach);
        if (list.isEmpty()) return null;
        return list.get(0).getAttachStatus();
    }

    public TruckPhotosEntity findByAttach(AttachEntity attach) {
        List<TruckPhotosEntity> list = truckPhotosRepository.findByTruckPhotoOrderByCreatedAtDesc(attach);
        if (list.isEmpty()) return null;
        return list.get(0);
    }
}
