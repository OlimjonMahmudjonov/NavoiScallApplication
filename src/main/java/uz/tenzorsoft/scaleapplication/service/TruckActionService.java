package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.repository.TruckActionRepository;

@Service
@RequiredArgsConstructor
public class TruckActionService {

    private final TruckActionRepository truckActionRepository;

    public TruckActionEntity save(TruckActionEntity truckActionEntity) {
        return truckActionRepository.save(truckActionEntity);
    }
}
