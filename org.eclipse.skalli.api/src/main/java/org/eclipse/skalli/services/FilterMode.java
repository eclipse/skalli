package org.eclipse.skalli.services;

public enum FilterMode {
    /**
     * Applies all given filters from left to right to all bundles.
     */
    ALL,

    /**
     * Applies all given filters from left to right to all bundles
     * and stops when one of the filters accepted
     */
    SHORT_CIRCUIT,

    /**
     * Applies all given filters from left to right to all bundles
     * and stops, when the first
     */
    FIRST_MATCHING
}
