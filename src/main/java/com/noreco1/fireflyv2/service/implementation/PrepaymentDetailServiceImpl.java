package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.PrepaymentDetail;
import com.noreco1.fireflyv2.repo.PrepaymentDetailRepo;
import com.noreco1.fireflyv2.controller.response.PrepaymentDetailDto;
import com.noreco1.fireflyv2.service.PrepaymentDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Personal on 11/9/2015.
 */
@Service
public class PrepaymentDetailServiceImpl implements PrepaymentDetailService {
    @Autowired
    PrepaymentDetailRepo prepaymentDetailRepo;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public List<PrepaymentDetailDto> getPrepaymentDetails(Integer ppId) {
        List<PrepaymentDetail> details = prepaymentDetailRepo.findByPrepaymentId(ppId);

        List<PrepaymentDetailDto> prepaymentDetailDtos = new ArrayList<>();

        if (details != null) {
            for (PrepaymentDetail line : details) {
                PrepaymentDetailDto lineDto = new PrepaymentDetailDto();
                lineDto.setId(line.getId());
                lineDto.setPpId(line.getPrepayment().getId());
                lineDto.setAccountNo(line.getAccountNumber());
                lineDto.setYear(line.getYear());
                lineDto.setMonth(line.getMonth());
                lineDto.setAmount(line.getAmount());
                lineDto.setBalance(line.getBalance());

                prepaymentDetailDtos.add(lineDto);
            }
        }

        return prepaymentDetailDtos;
    }
}
