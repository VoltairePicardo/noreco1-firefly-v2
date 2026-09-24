package com.noreco1.fireflyv2.service.strategy.inventory_document;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InventoryDocumentReleasingStrategyRegistry {

    private final Map<String, InventoryDocumentReleasingStrategy> strategiesByType;

    public InventoryDocumentReleasingStrategyRegistry(List<InventoryDocumentReleasingStrategy> strategies) {
        this.strategiesByType = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        InventoryDocumentReleasingStrategy::getDocumentType,
                        Function.identity()));
    }

    public Optional<InventoryDocumentReleasingStrategy> get(String documentType) {
        return Optional.ofNullable(strategiesByType.get(documentType));
    }
}
