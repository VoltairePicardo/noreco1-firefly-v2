package com.noreco1.fireflyv2.service.strategy.inventory_document;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of all {@link InventoryDocumentReleasingStrategy} beans, keyed by
 * document type code.
 * <p>
 * Spring collects every strategy bean into the injected {@code List} for us -
 * adding a new document type only means dropping in a new
 * {@code @Component} implementing the strategy interface, no switch/factory
 * code to touch.
 */
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
