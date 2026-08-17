package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Position;

import java.util.List;

public interface PositionService {
    List<Position> list();
    Position findById(Integer id);
    PostResponse create(Position position);
    PostResponse update(Position position);
    PostResponse deleteById(Integer id);
}
