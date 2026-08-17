package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.CanvassDetail;
import com.noreco1.fireflyv2.model.Item;
import com.noreco1.fireflyv2.repo.CanvassDetailRepo;
import com.noreco1.fireflyv2.controller.response.CanvassDetailDto;
import com.noreco1.fireflyv2.service.CanvassDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Personal on 5/14/2015.
 */
@Service
public class CanvassDetailServiceImpl implements CanvassDetailService {
    @Autowired
    CanvassDetailRepo canvassDetailRepo;

    @Override
    public List<CanvassDetailDto> getCanvassDetails(Integer canvassId) {
        List<CanvassDetail> canvassDetails = canvassDetailRepo.findByCanvassId(canvassId);

        List<CanvassDetailDto> canvassDetailDtos = new ArrayList<>();
        Map detailsMap = new HashMap<>();

        if (!canvassDetails.isEmpty()) {

            for (CanvassDetail line : canvassDetails) {

                Object o = detailsMap.get(line.getPurchaseRequestDetail().getId());
                if(o != null) {
                    CanvassDetail detailFromMap  = (CanvassDetail) o;

                    if(detailFromMap.getPriceSupplier2() == null || detailFromMap.getPriceSupplier2().compareTo(BigDecimal.ZERO) == 0) {

                        detailFromMap.setPriceSupplier2(line.getUnitPrice());

                    } else if(detailFromMap.getPriceSupplier3() == null || detailFromMap.getPriceSupplier3().compareTo(BigDecimal.ZERO) == 0) {
                        detailFromMap.setPriceSupplier3(line.getUnitPrice());
                    }

                    detailsMap.put(line.getPurchaseRequestDetail().getId(), detailFromMap); // put back to the map

                } else {
                    if(line.getUnitPrice().compareTo(BigDecimal.ZERO ) > 0) {
                        line.setPriceSupplier1(line.getUnitPrice());
                    }
                    detailsMap.put(line.getPurchaseRequestDetail().getId(), line);
                }
            }

            Iterator it = detailsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                CanvassDetail line = (CanvassDetail) pair.getValue();

                CanvassDetailDto lineDto = new CanvassDetailDto();

                lineDto.setId(line.getId());
                lineDto.setRvDetailId(line.getPurchaseRequestDetail().getId());
                lineDto.setCanvassId(line.getCanvass().getId());
                lineDto.setRvNumber(line.getPurchaseRequestDetail().getPurchaseRequest().getCode());
                lineDto.setQuantity(line.getPurchaseRequestDetail().getQuantity());

                Item item = line.getPurchaseRequestDetail().getItem();
                if(item != null) {
                    lineDto.setItemCode(item.getCode());
                    lineDto.setItemDescription(item.getDescription());
                } else {
                    lineDto.setItemDescription(line.getPurchaseRequestDetail().getJoDescription());
                }

                lineDto.setUnitCode(line.getPurchaseRequestDetail().getUnitMeasure().getCode());
                lineDto.setPriceSupplier1(line.getPriceSupplier1());
                lineDto.setPriceSupplier2(line.getPriceSupplier2());
                lineDto.setPriceSupplier3(line.getPriceSupplier3());

                // add with proper ordering by rvDetailId
                if(canvassDetailDtos.isEmpty()){
                    canvassDetailDtos.add(lineDto);
                } else {
                    int insertIndex = 0;    // next item in the list
                    for (CanvassDetailDto dto:canvassDetailDtos) {

                        if(canvassDetailDtos.size() > 1) {
                            insertIndex = canvassDetailDtos.indexOf(dto);
                        }
                        // if new item rvDetailId is  smaller than in the list, break loop, overwrite its value. List will auto-adjust
                        if(lineDto.getRvDetailId() < dto.getRvDetailId()) {
                            break;
                        } else {    // otherwise push item to the list
                            insertIndex++;
                        }
                    }

                    canvassDetailDtos.add(insertIndex, lineDto);
                }

                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        return canvassDetailDtos;
    }
}
