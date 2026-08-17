package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Position;
import com.noreco1.fireflyv2.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping("/list")
    public List<Position> list() {
        return positionService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Position> getById(@PathVariable Integer id) {
        Position position = positionService.findById(id);
        return position != null ? ResponseEntity.ok(position) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody Position position) {
        return positionService.create(position);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody Position position) {
        return positionService.update(position);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return positionService.deleteById(id);
    }
}
