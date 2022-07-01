package com.mcmiddleearth.mcmescripts.condition;

import com.mcmiddleearth.mcmescripts.debug.Descriptor;
import com.mcmiddleearth.mcmescripts.selector.Selector;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;

public class CriterionCondition<T> extends Condition {

    private final Criterion test;
    private final Selector<T> selector;

    public CriterionCondition(Selector<T> selector, Criterion test) {
        this.test = test;
        this.selector = selector;
    }

    @Override
    public boolean test(TriggerContext context) {
        int size = selector.select(context).size();
        context.getDescriptor() //.add(super.getDescriptor()).indent()
                .addLine("Condition description: ").indent().add(getDescriptor()).outdent()
                .addLine("Selected quantity: "+size);
        boolean result = test.apply(size);
        context.getDescriptor().addLine("Test result: "+result).outdent();
        return result;
    }

    @Override
    public Descriptor getDescriptor() {
        return super.getDescriptor().indent()
                .addLine("Selector: "+selector.getSelector())
                .addLine("Criterion: "+test.getComparator()+test.getLimit())
                .outdent();
    }

}
