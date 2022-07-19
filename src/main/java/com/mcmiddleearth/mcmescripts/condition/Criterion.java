package com.mcmiddleearth.mcmescripts.condition;

import java.util.function.Function;

public class Criterion implements Function<Integer,Boolean> {

    String comparator;
    Integer limit;

    public Criterion(String comparator, Integer limit) {
        this.comparator = comparator;
        this.limit = limit;
    }

    @Override
    public Boolean apply(Integer integer) {
        switch (comparator) {
            case "<": return integer < limit;
            case ">": return integer > limit;
            case "<=": return integer <= limit;
            case ">=": return integer >= limit;
            case "==":
            case "=": return integer.equals(limit);
            case "<>":
            case "!=": return !integer.equals(limit);
            default: return true;
        }
    }

    public String getComparator() {
        return comparator;
    }

    public Integer getLimit() {
        return limit;
    }
}
