package com.noreco1.fireflyv2.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ItemHistoryService {

    List<Map> getItemHistory(String serialNumber);

}
