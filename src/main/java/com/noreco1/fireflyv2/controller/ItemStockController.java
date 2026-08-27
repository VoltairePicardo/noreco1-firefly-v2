package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.controller.response.ItemStockDto;
import com.noreco1.fireflyv2.model.ItemStock;
import com.noreco1.fireflyv2.service.ItemStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/item-stock")
public class ItemStockController {

    private final ItemStockService itemStockService;

    public ItemStockController(ItemStockService itemStockService) {
        this.itemStockService = itemStockService;
    }

    @GetMapping(value = "/item-stock/list-paged-not-inv-loc/{invLocId}")
    public Page<ItemStock> getItemStockNotInvLoc(Pageable pageable, @PathVariable Integer invLocId, @RequestParam(value = "q", required = false) String query) {
        return itemStockService.getItemStockNotInvLoc(invLocId, query, pageable);
    }

    @GetMapping(value = "/item-stock/list-paged-inv-loc/{invLocId}")
    public Page<ItemStock> getItemStockInvLoc(Pageable pageable, @PathVariable Integer invLocId, @RequestParam(value = "q", required = false) String query) {
        return itemStockService.getItemStockInvLoc(invLocId, query, pageable);
    }

    @GetMapping(value = "/item-stock/list-paged")
    public Page<ItemStock> getItemStocks(Pageable pageable, @RequestParam(value = "q", required = false) String query) {
        return itemStockService.getItemStocks(query, pageable);
    }

    @GetMapping(value = "/item-stock/list-paged-with-zero-quantity")
    public Page<ItemStock> getItemStocksWithZeroQuantity(Pageable pageable, @RequestParam(value = "q", required = false) String query) {
        return itemStockService.getItemStocksWithZeroQuantity(query, pageable);
    }

    @GetMapping(value = "/item-stock/list-paged-with-zero-quantity/inv-loc/inv-cat/{invLocId}/{invCatId}")
    public Page<ItemStock> getItemStocksWithZeroQuantityInvLocInvCat(@PathVariable Integer invLocId, @PathVariable Integer invCatId,
                                                                     @RequestParam(value = "q", required = false) String query,
                                                                     Pageable pageable) {
        return itemStockService.getItemStocksWithZeroQuantityInvLocInvCat(invLocId, invCatId, query, pageable);
    }

    @GetMapping(value = "/item-stock/list-paged-inv-loc-with-zero-quantity/{invLocId}")
    public Page<ItemStock> getItemStocksInvLocWithZeroQuantity(Pageable pageable,
                                                               @PathVariable Integer invLocId,
                                                               @RequestParam(value = "q", required = false) String query) {
        return itemStockService.getItemStocksInvLocWithZeroQuantity(invLocId, query, pageable);
    }

    @GetMapping(value = "/item-stock/list-paged-inv-loc-with-zero-quantity-no-stock/{invLocId}")
    public Page<ItemStockDto> getItemStocksInvLocWithZeroQuantityNoStock(Pageable pageable,
                                                                         @PathVariable Integer invLocId,
                                                                         @RequestParam(value = "q", required = false) String query) {
        return itemStockService.getItemStocksInvLocWithZeroQuantityNoStock(invLocId, query, pageable);
    }
}
