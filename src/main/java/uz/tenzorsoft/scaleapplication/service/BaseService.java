package uz.tenzorsoft.scaleapplication.service;

import uz.tenzorsoft.scaleapplication.domain.entity.BaseEntity;

public interface BaseService<E extends BaseEntity, Resp, Req> {

    Resp entityToResponse(E entity);

    E requestToEntity(Req request);

}
