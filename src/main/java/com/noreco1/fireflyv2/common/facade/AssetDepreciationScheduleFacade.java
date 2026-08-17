package com.noreco1.fireflyv2.common.facade;

import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentType;

import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 8/11/2015.
 */
public interface AssetDepreciationScheduleFacade {

    void generateSchedule(Asset asset);
}
