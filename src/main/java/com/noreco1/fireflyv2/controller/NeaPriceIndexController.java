package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.NeaPriceIndex;
import com.noreco1.fireflyv2.model.NeaPriceIndexDetail;
import com.noreco1.fireflyv2.service.NeaPriceIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nea-price-index")
public class NeaPriceIndexController {

    @Autowired
    private NeaPriceIndexService neaPriceIndexService;

    @GetMapping("/list")
    public Page<NeaPriceIndex> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return neaPriceIndexService.list(q, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NeaPriceIndex> getById(@PathVariable Integer id) {
        NeaPriceIndex npi = neaPriceIndexService.findById(id);
        return npi != null ? ResponseEntity.ok(npi) : ResponseEntity.notFound().build();
    }

    @GetMapping("/with-price-only/{id}")
    public ResponseEntity<NeaPriceIndex> getWithPriceOnly(@PathVariable Integer id) {
        NeaPriceIndex npi = neaPriceIndexService.findByIdWithPrice(id);
        return npi != null ? ResponseEntity.ok(npi) : ResponseEntity.notFound().build();
    }

    @GetMapping("/get-by/{itemId}")
    public ResponseEntity<NeaPriceIndexDetail> getByItem(@PathVariable Integer itemId) {
        NeaPriceIndexDetail detail = neaPriceIndexService.getItemNeaPriceIndex(itemId);
        return detail != null ? ResponseEntity.ok(detail) : ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public PostResponse create(@RequestBody NeaPriceIndex neaPriceIndex) {
        return neaPriceIndexService.create(neaPriceIndex);
    }

    @PostMapping("/update")
    public PostResponse update(@RequestBody NeaPriceIndex neaPriceIndex) {
        return neaPriceIndexService.update(neaPriceIndex);
    }

    @PostMapping("/delete/{id}")
    public PostResponse delete(@PathVariable Integer id) {
        return neaPriceIndexService.deleteById(id);
    }
}
