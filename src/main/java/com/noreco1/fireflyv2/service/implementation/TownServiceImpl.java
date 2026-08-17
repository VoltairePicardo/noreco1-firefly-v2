package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.model.Town;
import com.noreco1.fireflyv2.repo.TownRepo;
import com.noreco1.fireflyv2.service.TownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class TownServiceImpl implements TownService {

    @Autowired
    private TownRepo townRepo;

    @Override
    public List<Town> list() {
        return townRepo.findAll().stream()
                .sorted(Comparator.comparing(Town::getName))
                .toList();
    }
}
