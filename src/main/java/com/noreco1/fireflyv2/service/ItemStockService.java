package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.ItemStockDto;
import com.noreco1.fireflyv2.model.ItemStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemStockService {
    Page<ItemStock> getItemStockNotInvLoc(Integer invLocId, String query, Pageable pageable);
    Page<ItemStock> getItemStockInvLoc(Integer invLocId, String query, Pageable pageable);
    Page<ItemStock> getItemStocks(String query, Pageable pageable);
    Page<ItemStock> getItemStocksWithZeroQuantity(String query, Pageable pageable);
    Page<ItemStock> getItemStocksWithZeroQuantityInvLocInvCat(Integer invLocId, Integer invCatId, String query, Pageable pageable);
    Page<ItemStock> getItemStocksInvLocWithZeroQuantity(Integer invLocId, String query, Pageable pageable);
    Page<ItemStockDto> getItemStocksInvLocWithZeroQuantityNoStock(Integer invLocId, String query, Pageable pageable);
}
