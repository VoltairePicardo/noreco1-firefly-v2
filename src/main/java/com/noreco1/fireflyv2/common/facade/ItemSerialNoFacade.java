package com.noreco1.fireflyv2.common.facade;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.util.List;
import java.util.Map;

public interface ItemSerialNoFacade {
    JRBeanCollectionDataSource itemsInPrint(List<Map> details);
}
