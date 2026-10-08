package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.JobOrderAcceptanceDetail;
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
        List<JobOrderAcceptanceDetail> jobOrderAcceptanceDetails = joAcceptanceDetailRepo.findAllByJoAcceptanceId(joId);

        List<JoAcceptanceDetailDto> joAcceptanceDetailDtos = new ArrayList<>();

        if (jobOrderAcceptanceDetails != null) {
            for (JobOrderAcceptanceDetail line : jobOrderAcceptanceDetails) {
                JoAcceptanceDetailDto lineDto = new JoAcceptanceDetailDto();
                lineDto.setId(line.getId());
                lineDto.setJoDetailId(line.getJobOrderDetail().getId());
                lineDto.setRvDetailId(line.getJobOrderDetail().getPurchaseRequestDetail().getId());
                lineDto.setJoAcceptanceId(line.getJobOrderAcceptance().getId());
                lineDto.setQuantity(line.getQuantity());
                if(line.getJobOrderDetail().getPurchaseRequestDetail().getItem() != null) {
                    lineDto.setItemDescription(line.getJobOrderDetail().getPurchaseRequestDetail().getItem().getDescription());
                }
                lineDto.setUnitCode(line.getJobOrderDetail().getPurchaseRequestDetail().getUnitMeasure().getCode());
                lineDto.setUnitPrice(line.getJobOrderDetail().getAmount());
                lineDto.setVat(line.getVat());
                lineDto.setDiscount(line.getDiscount());
                lineDto.setItemAmount(line.getAmount());
                lineDto.setAcceptedAmount(line.getJobOrderDetail().getAcceptedAmount());
                lineDto.setRemainingAmount(line.getJobOrderDetail().getAmount().subtract(line.getJobOrderDetail().getAcceptedAmount()));
                lineDto.setJoDescription(line.getJobOrderDetail().getPurchaseRequestDetail().getJoDescription() == null ? "" : line.getJobOrderDetail().getPurchaseRequestDetail().getJoDescription());
                lineDto.setAdjustment(line.getAdjustment());
                lineDto.setNetAmount(line.getNetAmount());

                joAcceptanceDetailDtos.add(lineDto);
            }
        }

        return joAcceptanceDetailDtos;
    }
}
