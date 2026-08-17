package com.noreco1.fireflyv2.common.facade;


import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.DateRange;
import com.noreco1.fireflyv2.model.FactorPercentageDistro;
import com.noreco1.fireflyv2.repo.FactorPercentageDistroRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class FactorFacadeImpl implements FactorFacade {


    @Autowired
    FactorPercentageDistroRepo factorPercentageDistroRepo;

    @Override
    public Map getPercentageDistrosByValidity(Integer factorId) {

        Map distroMap = new HashMap();

        Set<FactorPercentageDistro> distros = factorPercentageDistroRepo.findByFactorId(factorId);
        if(Checker.collectionIsNotEmpty(distros)) {

            for (FactorPercentageDistro distro:distros) {

                DateRange validityDate = distro.getValidityDate();

                if(validityDate != null) {

                    String key = validityDate.getDescription();

                    List<FactorPercentageDistro> distoListInMap = new ArrayList<>();

                    Object distroListObj = distroMap.get(key);
                    if(distroListObj != null) {
                        distoListInMap = (List<FactorPercentageDistro>) distroListObj;
                    }

                    distro.setFactor(null); // get rid of extra data

                    distoListInMap.add(distro); // TODO: sort
                    distroMap.put(key, distoListInMap);

                }

            }
        }

        return distroMap;
    }
}
