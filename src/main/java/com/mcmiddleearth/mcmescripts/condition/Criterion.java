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
        return switch (comparator) {
            case "<" -> integer < limit;
            case ">" -> integer > limit;
            case "<=" -> integer <= limit;
            case ">=" -> integer >= limit;
            case "==", "=" -> integer.equals(limit);
            case "<>", "!=" -> !integer.equals(limit);
            default -> true;
        };
    }

    public String getComparator() {
        return comparator;
    }

    public Integer getLimit() {
        return limit;
    }
}
