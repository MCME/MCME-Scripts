package com.mcmiddleearth.mcmescripts.condition;

import com.mcmiddleearth.mcmescripts.debug.Descriptor;
import com.mcmiddleearth.mcmescripts.trigger.TriggerContext;
import org.bukkit.World;

import java.util.Locale;

public class MessageCondition extends Condition{

    private final String message, fullMessage;

    private final ComparatorType comparatorType;

    private final boolean negate;

    public MessageCondition(String message, boolean negate) {
        this.negate = negate;
        this.fullMessage = message;
        if(message.startsWith("*") && message.endsWith("*")) {
            comparatorType = ComparatorType.CONTAINS;
            if(message.equals("*")) {
                this.message = "";
            } else {
                this.message = message.substring(1, message.length() - 1);
            }
        } else if(message.startsWith("*")) {
            comparatorType = ComparatorType.ENDS_WITH;
            this.message = message.substring(1);
        } else if(message.endsWith("*")) {
            comparatorType = ComparatorType.STARTS_WITH;
            this.message = message.substring(0,message.length()-1);
        }
        else {
            comparatorType = ComparatorType.EQUALS;
            this.message = message;
        }
    }

    @Override
    public boolean test(TriggerContext context) {
        context.getDescriptor().add(super.getDescriptor()).indent()
               .addLine("Looking for message: "+fullMessage)
                .addLine("Negate: "+negate);
        boolean result;
        switch(comparatorType) {
           case EQUALS:
               result = context.getMessage().equals(message);
               break;
            case STARTS_WITH:
                result = context.getMessage().startsWith(message);
            case ENDS_WITH:
                result = context.getMessage().endsWith(message);
            default:
                result = context.getMessage().contains(message);
        }
        context.getDescriptor().addLine("Test result: "+result).outdent();
        return result;
    }

    public Descriptor getDescriptor() {
        return super.getDescriptor().indent()
                .addLine("Search String: "+fullMessage)
                .addLine("Negate: "+negate)
                .outdent();
    }

    private enum ComparatorType {
        STARTS_WITH,
        ENDS_WITH,
        CONTAINS,
        EQUALS;

    }
}
