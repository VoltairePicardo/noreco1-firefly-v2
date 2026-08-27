package com.noreco1.fireflyv2;

import com.noreco1.fireflyv2.model.ItemStock;
import com.noreco1.fireflyv2.repo.ItemStockRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

@SpringBootTest
public class DiagPagingTest {

    @Autowired
    private ItemStockRepo itemStockRepo;

    @Test
    void diagnosePaging() {
        Page<ItemStock> page = itemStockRepo.findAllItemStockByParams(
                1, 1, new BigDecimal(-1), "%%", 1, PageRequest.of(0, 10));

        System.out.println("=== DIAG RESULT (no #pageable marker) ===");
        System.out.println("content.size() = " + page.getContent().size());
        System.out.println("totalElements  = " + page.getTotalElements());
        System.out.println("totalPages     = " + page.getTotalPages());
        System.out.println("pageable       = " + page.getPageable());
        System.out.println("=== END DIAG ===");

        Page<ItemStock> page2 = itemStockRepo.findAllByInventoryLocationIdAndItemInventoryCategoryIdAndTotalQuantityGreaterThanAndItemCodeLikeOrItemDescriptionLikeOrderByItemCode(
                1, 1, new BigDecimal(-1), "%%", PageRequest.of(0, 10));

        System.out.println("=== DIAG RESULT 2 (WITH #pageable marker) ===");
        System.out.println("content.size() = " + page2.getContent().size());
        System.out.println("totalElements  = " + page2.getTotalElements());
        System.out.println("totalPages     = " + page2.getTotalPages());
        System.out.println("pageable       = " + page2.getPageable());
        System.out.println("=== END DIAG 2 ===");
    }
}
