package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.controller.response.ItemStockDto;
import com.noreco1.fireflyv2.model.Employee;
import com.noreco1.fireflyv2.model.ItemStock;
import com.noreco1.fireflyv2.repo.EmployeeRepo;
import com.noreco1.fireflyv2.repo.ItemStockRepo;
import com.noreco1.fireflyv2.service.ItemStockService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@Service
public class ItemStockServiceImpl implements ItemStockService {

    @Autowired
    private ItemStockRepo itemStockRepo;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private Environment env;

    @Override
    public Page<ItemStock> getItemStockNotInvLoc(Integer invLocId, String query, Pageable pageable) {
        if (Checker.isStringNullOrEmpty(query)) {
            return itemStockRepo.findAllByInventoryLocationIdNotAndQuantityGreaterThanOrderByItemCode(invLocId, BigDecimal.ZERO, pageable);
        }
        return itemStockRepo.findAllByInventoryLocationIdNotAndQuantityGreaterThanAndItemCodeLikeOrItemDescriptionLikeOrderByItemCode(invLocId, BigDecimal.ZERO, "%" + query.toUpperCase() + "%", pageable);
    }

    @Override
    public Page<ItemStock> getItemStockInvLoc(Integer invLocId, String query, Pageable pageable) {
        Integer employeeDepartmentId = getLoggedInEmployeeDepartmentId();

        Page<ItemStock> stocks = Checker.isStringNullOrEmpty(query)
                ? itemStockRepo.findAllByInventoryLocationIdAndTotalQuantityGreaterThanOrderByItemCode(invLocId, BigDecimal.ZERO, pageable)
                : itemStockRepo.findAllByInventoryLocationIdAndTotalQuantityGreaterThanAndItemCodeIgnoreCaseLikeOrItemDescriptionIgnoreCaseLikeOrderByItemCode(invLocId, BigDecimal.ZERO, "%" + query.toUpperCase() + "%", pageable);

        return withBalanceAndImage(stocks, invLocId, employeeDepartmentId);
    }

    @Override
    public Page<ItemStock> getItemStocks(String query, Pageable pageable) {
        if (Checker.isStringNullOrEmpty(query)) {
            return itemStockRepo.findAllByQuantityGreaterThanOrderByItemCode(BigDecimal.ZERO, pageable);
        }
        return itemStockRepo.findAllByItemCodeLikeOrItemDescriptionLikeAndQuantityGreaterThanOrderByItemCode("%" + query.toUpperCase() + "%", BigDecimal.ZERO, pageable);
    }

    @Override
    public Page<ItemStock> getItemStocksWithZeroQuantity(String query, Pageable pageable) {
        if (Checker.isStringNullOrEmpty(query)) {
            return itemStockRepo.findAllByQuantityGreaterThanOrderByItemCode(new BigDecimal(-1), pageable);
        }
        return itemStockRepo.findAllByItemCodeLikeOrItemDescriptionLikeAndQuantityGreaterThanOrderByItemCode("%" + query.toUpperCase() + "%", new BigDecimal(-1), pageable);
    }

    @Override
    public Page<ItemStock> getItemStocksWithZeroQuantityInvLocInvCat(Integer invLocId, Integer invCatId, String query, Pageable pageable) {
        String safeQuery = Checker.isStringNullOrEmpty(query) ? "" : query;
        Integer employeeDepartmentId = getLoggedInEmployeeDepartmentId();

        Page<ItemStock> stocks = itemStockRepo.findAllItemStockByParams(invLocId, invCatId, new BigDecimal(-1), "%" + safeQuery.toUpperCase() + "%", employeeDepartmentId, pageable);

        return withBalanceAndImage(stocks, invLocId, employeeDepartmentId);
    }

    @Override
    public Page<ItemStock> getItemStocksInvLocWithZeroQuantity(Integer invLocId, String query, Pageable pageable) {
        Integer employeeDepartmentId = getLoggedInEmployeeDepartmentId();

        Page<ItemStock> stocks = Checker.isStringNullOrEmpty(query)
                ? itemStockRepo.findAllByInventoryLocationIdAndTotalQuantityGreaterThanOrderByItemCode(invLocId, new BigDecimal(-1), pageable)
                : itemStockRepo.findAllByInventoryLocationIdAndTotalQuantityGreaterThanAndItemCodeIgnoreCaseLikeOrItemDescriptionIgnoreCaseLikeOrderByItemCode(invLocId, new BigDecimal(-1), "%" + query.toUpperCase() + "%", pageable);

        return withBalanceAndImage(stocks, invLocId, employeeDepartmentId);
    }

    @Override
    public Page<ItemStockDto> getItemStocksInvLocWithZeroQuantityNoStock(Integer invLocId, String query, Pageable pageable) {
        Page<ItemStock> page = Checker.isStringNullOrEmpty(query)
                ? itemStockRepo.findAllByInventoryLocationIdAndQuantityGreaterThanAndNoStockOrderByItemCode(invLocId, pageable)
                : itemStockRepo.findAllByItemCodeLikeOrItemDescriptionLikeAndInventoryLocationIdAndQuantityGreaterThanAndNoStockOrderByItemCode("%" + query + "%", invLocId, pageable);

        return page.map(this::toDto);
    }

    private Integer getLoggedInEmployeeDepartmentId() {
        Employee employee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());
        return employee.getDepartment().getId();
    }

    /**
     * Populates each item's withdrawal balance and, when available, its attachment as a base64 image.
     */
    private Page<ItemStock> withBalanceAndImage(Page<ItemStock> stocks, Integer invLocId, Integer employeeDepartmentId) {
        List<ItemStock> pagedItemStock = stocks.getContent();

        for (ItemStock itemStock : pagedItemStock) {
            BigDecimal withdrawnBalance = itemStockRepo.ItemStockBalance(invLocId, itemStock.getItem().getId(), employeeDepartmentId);

            if (Checker.isAmountGreaterThanZero(withdrawnBalance)) {
                BigDecimal balance = itemStock.getTotalQuantity().subtract(withdrawnBalance);
                itemStock.setBalance(balance.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : balance);
            } else {
                itemStock.setBalance(itemStock.getTotalQuantity());
            }

            if (!Checker.isStringNullOrEmpty(itemStock.getItem().getFileName())) {
                try {
                    itemStock.setBase64Image("data:image/png;base64," + Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File(env.getProperty("path.attachments") + itemStock.getItem().getFileName()))));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return new PageImpl<>(pagedItemStock, stocks.getPageable(), stocks.getTotalElements());
    }

    private ItemStockDto toDto(ItemStock stock) {
        ItemStockDto dto = new ItemStockDto();
        dto.setId(stock.getId());
        dto.setItem(stock.getItem());
        dto.setInventoryLocation(stock.getInventoryLocation());
        dto.setTotalQuantity(stock.getTotalQuantity());
        dto.setTotalItemCost(stock.getTotalItemCost());
        return dto;
    }
}
