package com.mcmiddleearth.mcmescripts.action;

import com.mcmiddleearth.mcmescripts.debug.DebugManager;
import com.mcmiddleearth.mcmescripts.debug.Modules;
import com.mcmiddleearth.mcmescripts.selector.Selector;
import org.bukkit.entity.Player;

public class TitleAction extends SelectingAction<Player> {

    public TitleAction(Selector<Player> selector, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        super(selector, (player, context) -> {
            DebugManager.verbose(Modules.Action.execute(TitleAction.class),"Title for player: "+player.getName() +" "+title);
            String finalTitle = title;
            String finalSubtitle = subtitle;
            if(context.getMessage()!=null) {
                finalTitle = title.replace("*message*", context.getMessage());
                finalSubtitle = subtitle.replace("*message*", context.getMessage());
            }
            if(context.getName()!=null) {
                finalTitle = title.replace("*name*", context.getName());
                finalSubtitle = subtitle.replace("*name*", context.getName());
            }
            player.sendTitle(finalTitle,finalSubtitle,fadeIn,stay,fadeOut);
        });
        //DebugManager.info(Modules.Action.create(this.getClass()),"Selector: "+selector.getSelector()+" Title: "+title);
        getDescriptor().indent()
                .addLine("Title: "+title)
                .addLine("Subtitle: "+subtitle)
                .addLine("Times: "+fadeIn+" "+stay+" "+fadeOut);
    }
}
