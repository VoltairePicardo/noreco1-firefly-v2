package com.noreco1.fireflyv2.resource;

import com.noreco1.fireflyv2.model.BankAccount;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;

public class BankAccountResource extends EntityModel<BankAccount> {
    public BankAccountResource(BankAccount content, Link... links) {
        super(content, java.util.Arrays.asList(links));
    }

    public BankAccountResource(BankAccount content, Iterable<Link> links) {
        super(content, links);
    }
}
