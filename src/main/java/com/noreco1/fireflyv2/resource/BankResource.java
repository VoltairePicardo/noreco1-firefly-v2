package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.Bank;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

/**
 * Created by Asus on 16/08/2022.
 */
public class BankResource extends EntityModel<Bank> {
    public BankResource(Bank content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public BankResource(Bank content, Iterable<Link> links) {
        super(content, links);
    }
}
