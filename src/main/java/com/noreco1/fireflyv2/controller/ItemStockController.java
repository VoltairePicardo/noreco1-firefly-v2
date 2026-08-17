package com.noreco1.fireflyv2.controller;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentStatus;
import com.noreco1.fireflyv2.repo.EmployeeRepo;
import com.noreco1.fireflyv2.repo.ItemStockRepo;
import com.noreco1.fireflyv2.controller.response.ItemStockDto;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/item-stock")
public class ItemStockController {

    @Autowired
    ItemStockRepo itemStockRepo;

    @Autowired
    AuthenticationFacade authenticationFacade;

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    Environment env;

    @GetMapping(value = "/item-stock/list-paged-not-inv-loc/{invLocId}")
    public Page<ItemStock> getItemStockNotInvLoc(Pageable pageable, @PathVariable Integer invLocId, @RequestParam(value = "q", required = false) String query) {
        if (query == null || query.trim().length() == 0) {
            return itemStockRepo.findAllByInventoryLocationIdNotAndQuantityGreaterThanOrderByItemCode(invLocId, BigDecimal.ZERO, pageable);
        } else {
            return itemStockRepo.findAllByInventoryLocationIdNotAndQuantityGreaterThanAndItemCodeLikeOrItemDescriptionLikeOrderByItemCode(invLocId, BigDecimal.ZERO, "%"+query.toUpperCase()+"%", pageable);
        }
    }

    @GetMapping(value = "/item-stock/list-paged-inv-loc/{invLocId}")
    public Page<ItemStock> getItemStockInvLoc(Pageable pageable, @PathVariable Integer invLocId, @RequestParam(value = "q", required = false) String query) {

        Page<ItemStock> stocks;

        Employee employee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());
        Integer employeeDepartmentId = employee.getDepartment().getId();

        if (query == null || query.trim().length() == 0) {
            stocks = itemStockRepo.findAllByInventoryLocationIdAndTotalQuantityGreaterThanOrderByItemCode(invLocId, BigDecimal.ZERO, pageable);
        } else {
            stocks = itemStockRepo.findAllByInventoryLocationIdAndTotalQuantityGreaterThanAndItemCodeIgnoreCaseLikeOrItemDescriptionIgnoreCaseLikeOrderByItemCode(invLocId, BigDecimal.ZERO, "%"+query.toUpperCase()+"%", pageable);
        }

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

    @GetMapping(value = "/item-stock/list-paged")
    public Page<ItemStock> getItemStocks(Pageable pageable, @RequestParam(value = "q", required = false) String query) {
        if (query == null || query.trim().length() == 0) {
            return itemStockRepo.findAllByQuantityGreaterThanOrderByItemCode(BigDecimal.ZERO, pageable);
        } else {
            return itemStockRepo.findAllByItemCodeLikeOrItemDescriptionLikeAndQuantityGreaterThanOrderByItemCode("%"+query.toUpperCase()+"%", BigDecimal.ZERO, pageable);
        }
    }

    @GetMapping(value = "/item-stock/list-paged-with-zero-quantity")
    public Page<ItemStock> getItemStocksWithZeroQuantity(Pageable pageable, @RequestParam(value = "q", required = false) String query) {
        if (query == null || query.trim().length() == 0) {
            return itemStockRepo.findAllByQuantityGreaterThanOrderByItemCode(new BigDecimal(-1), pageable);
        } else {
            return itemStockRepo.findAllByItemCodeLikeOrItemDescriptionLikeAndQuantityGreaterThanOrderByItemCode("%"+query.toUpperCase()+"%", new BigDecimal(-1), pageable);
        }
    }

    @GetMapping(value = "/item-stock/list-paged-with-zero-quantity/inv-loc/inv-cat/{invLocId}/{invCatId}")
    public Page<ItemStock> getItemStocksWithZeroQuantityInvLocInvCat(Pageable pageable,
                                                                     @PathVariable Integer invLocId, @PathVariable Integer invCatId,
                                                                     @RequestParam(value = "q", required = false) String query) {
        if (query == null || query.trim().length() == 0) {
            query = "";
        }

        Employee employee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());
        Integer employeeDepartmentId = employee.getDepartment().getId();

        Page<ItemStock> stocks = itemStockRepo.findAllItemStockByParams(invLocId, invCatId, new BigDecimal(-1), "%"+query.toUpperCase()+"%", employeeDepartmentId, pageable);
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

    @GetMapping(value = "/item-stock/list-paged-inv-loc-with-zero-quantity/{invLocId}")
    public Page<ItemStock> getItemStocksInvLocWithZeroQuantity(Pageable pageable,
                                                               @PathVariable Integer invLocId,
                                                               @RequestParam(value = "q", required = false) String query) {
        Page<ItemStock> stocks;

        Employee employee = employeeRepo.findOneByAccountNumber(authenticationFacade.getLoggedIn().getAccountNo());
        Integer employeeDepartmentId = employee.getDepartment().getId();

        if (query == null || query.trim().length() == 0) {
            stocks = itemStockRepo.findAllByInventoryLocationIdAndTotalQuantityGreaterThanOrderByItemCode(invLocId, new BigDecimal(-1), pageable);
        } else {
            stocks = itemStockRepo.findAllByInventoryLocationIdAndTotalQuantityGreaterThanAndItemCodeIgnoreCaseLikeOrItemDescriptionIgnoreCaseLikeOrderByItemCode(invLocId, new BigDecimal(-1), "%"+query.toUpperCase()+"%", pageable);
        }

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

    @GetMapping(value = "/item-stock/list-paged-inv-loc-with-zero-quantity-no-stock/{invLocId}")
    public Page<ItemStockDto> getItemStocksInvLocWithZeroQuantityNoStock(Pageable pageable,
                                                                         @PathVariable Integer invLocId,
                                                                         @RequestParam(value = "q", required = false) String query) {
        Page<ItemStock> page;
        if (query == null || query.trim().length() == 0) {
            page = itemStockRepo.findAllByInventoryLocationIdAndQuantityGreaterThanAndNoStockOrderByItemCode(invLocId, pageable);
        } else {
            page = itemStockRepo.findAllByItemCodeLikeOrItemDescriptionLikeAndInventoryLocationIdAndQuantityGreaterThanAndNoStockOrderByItemCode("%" + query + "%", invLocId, pageable);
        }
        return page.map(stock -> {
            ItemStockDto dto = new ItemStockDto();
            dto.setId(stock.getId());
            dto.setItem(stock.getItem());
            dto.setInventoryLocation(stock.getInventoryLocation());
            dto.setTotalQuantity(stock.getTotalQuantity());
            dto.setTotalItemCost(stock.getTotalItemCost());
            return dto;
        });
    }
}
