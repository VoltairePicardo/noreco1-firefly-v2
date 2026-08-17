package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.StockWithdrawal;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Tri-Nvent on 7/16/2020.
 */
public class StockWithdrawalResource extends EntityModel<StockWithdrawal> {
    public StockWithdrawalResource(StockWithdrawal content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public StockWithdrawalResource(StockWithdrawal content, Iterable<Link> links) {
        super(content, links);
    }
}
