package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.JoAcceptanceDetail;
import com.noreco1.fireflyv2.repo.JoAcceptanceDetailRepo;
import com.noreco1.fireflyv2.controller.response.JoAcceptanceDetailDto;
import com.noreco1.fireflyv2.service.JoAcceptanceDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Personal on 7/9/2015.
 */
@Service
public class JoAcceptanceDetailServiceImpl implements JoAcceptanceDetailService {
    @Autowired
    JoAcceptanceDetailRepo joAcceptanceDetailRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<JoAcceptanceDetailDto> getJoaDetails(Integer joId) {
        List<JoAcceptanceDetail> joAcceptanceDetails = joAcceptanceDetailRepo.findAllByJoAcceptanceId(joId);

        List<JoAcceptanceDetailDto> joAcceptanceDetailDtos = new ArrayList<>();

        if (joAcceptanceDetails != null) {
            for (JoAcceptanceDetail line : joAcceptanceDetails) {
                JoAcceptanceDetailDto lineDto = new JoAcceptanceDetailDto();
                lineDto.setId(line.getId());
                lineDto.setJoDetailId(line.getJoDetail().getId());
                lineDto.setRvDetailId(line.getJoDetail().getPurchaseRequestDetail().getId());
                lineDto.setJoAcceptanceId(line.getJoAcceptance().getId());
                lineDto.setQuantity(line.getQuantity());
                if(line.getJoDetail().getPurchaseRequestDetail().getItem() != null) {
                    lineDto.setItemDescription(line.getJoDetail().getPurchaseRequestDetail().getItem().getDescription());
                }
                lineDto.setUnitCode(line.getJoDetail().getPurchaseRequestDetail().getUnitMeasure().getCode());
                lineDto.setUnitPrice(line.getJoDetail().getAmount());
                lineDto.setVat(line.getVat());
                lineDto.setDiscount(line.getDiscount());
                lineDto.setItemAmount(line.getAmount());
                lineDto.setAcceptedAmount(line.getJoDetail().getAcceptedAmount());
                lineDto.setRemainingAmount(line.getJoDetail().getAmount().subtract(line.getJoDetail().getAcceptedAmount()));
                lineDto.setJoDescription(line.getJoDetail().getPurchaseRequestDetail().getJoDescription() == null ? "" : line.getJoDetail().getPurchaseRequestDetail().getJoDescription());
                lineDto.setAdjustment(line.getAdjustment());
                lineDto.setNetAmount(line.getNetAmount());

                joAcceptanceDetailDtos.add(lineDto);
            }
        }

        return joAcceptanceDetailDtos;
    }
}
