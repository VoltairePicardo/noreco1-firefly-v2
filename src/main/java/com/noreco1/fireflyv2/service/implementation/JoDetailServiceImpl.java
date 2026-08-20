package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.JoDetail;
import com.noreco1.fireflyv2.repo.JoDetailRepo;
import com.noreco1.fireflyv2.controller.response.JoDetailDto;
import com.noreco1.fireflyv2.service.JoDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Personal on 6/18/2015.
 */
@Service
public class JoDetailServiceImpl implements JoDetailService {
    @Autowired
    JoDetailRepo joDetailRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<JoDetailDto> getJoDetails(Integer joId) {
        List<JoDetail> joDetails = joDetailRepo.findByJobOrderId(joId);

        List<JoDetailDto> joDetailDtos = new ArrayList<>();

        if (joDetails != null) {
            for (JoDetail line : joDetails) {
                JoDetailDto lineDto = new JoDetailDto();
                lineDto.setId(line.getId());
                lineDto.setRvDetailId(line.getPurchaseRequestDetail().getId());
                lineDto.setJobOrderId(line.getJobOrder().getId());
                lineDto.setQuantity(line.getQuantity());
                if(line.getPurchaseRequestDetail().getItem() != null) {
                    lineDto.setItemId(line.getPurchaseRequestDetail().getItem().getId());
                    lineDto.setItemCode(line.getPurchaseRequestDetail().getItem().getCode());
                    lineDto.setItemDescription(line.getPurchaseRequestDetail().getItem().getDescription());
                }
                lineDto.setUnitCode(line.getPurchaseRequestDetail().getUnitMeasure().getCode());
                lineDto.setUnitPrice(line.getUnitPrice());
                lineDto.setVat(line.getVat());
                lineDto.setDiscount(line.getDiscount());
                lineDto.setItemAmount(line.getAmount());
                lineDto.setAcceptedAmount(line.getAcceptedAmount());
                lineDto.setRvdQuantity(line.getPurchaseRequestDetail().getQuantity());
                lineDto.setJoDescription(line.getPurchaseRequestDetail().getJoDescription() == null ? "" : line.getPurchaseRequestDetail().getJoDescription());
                lineDto.setRvDescription(line.getPurchaseRequestDetail().getPurchaseRequest().getPurpose());

                joDetailDtos.add(lineDto);
            }
        }

        return joDetailDtos;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<JoDetailDto> getJoDetailsForJoa(Integer joId) {
        List<JoDetail> joDetails = joDetailRepo.findJoDetailsForJoa(joId, com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId());

        List<JoDetailDto> joDetailDtos = new ArrayList<>();

        if (joDetails != null) {
            for (JoDetail line : joDetails) {
                JoDetailDto lineDto = new JoDetailDto();
                lineDto.setId(line.getId());
                lineDto.setRvDetailId(line.getPurchaseRequestDetail().getId());
                lineDto.setJobOrderId(line.getJobOrder().getId());
                lineDto.setQuantity(line.getQuantity());
                if(line.getPurchaseRequestDetail().getItem() != null) {
                    lineDto.setItemCode(line.getPurchaseRequestDetail().getItem().getCode());
                    lineDto.setItemDescription(line.getPurchaseRequestDetail().getItem().getDescription());
                }
                lineDto.setUnitCode(line.getPurchaseRequestDetail().getUnitMeasure().getCode());
                lineDto.setUnitPrice(line.getUnitPrice());
                lineDto.setVat(line.getVat());
                lineDto.setDiscount(line.getDiscount());
                lineDto.setItemAmount(line.getAmount());
                lineDto.setAcceptedAmount(line.getAcceptedAmount());
                lineDto.setRvdQuantity(line.getPurchaseRequestDetail().getQuantity());
                lineDto.setJoDescription(line.getPurchaseRequestDetail().getJoDescription() == null ? "" : line.getPurchaseRequestDetail().getJoDescription());

                joDetailDtos.add(lineDto);
            }
        }

        return joDetailDtos;
    }
}