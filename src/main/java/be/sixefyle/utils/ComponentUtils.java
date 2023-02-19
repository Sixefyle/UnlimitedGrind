package be.sixefyle.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.List;

public class ComponentUtils {

    public static Component createText(String line, List<String> hoverText){
        Component component = Component.text(line);
        Component hoverComponent = Component.empty();
        for (String s : hoverText) {
            hoverComponent = hoverComponent.append(Component.text(s));
            hoverComponent = hoverComponent.appendNewline();
        }
        component = component.hoverEvent(HoverEvent.showText(hoverComponent));
        return component;
    }

    public static Component createText(String line, String hoverText){
        Component component = Component.text(line);
        Component hoverComponent = Component.text(hoverText);

        component = component.hoverEvent(HoverEvent.showText(hoverComponent));
        return component;
    }

    public static Component createSeparator(String separator){
        return Component.text(separator)
                .hoverEvent(HoverEvent.showText(Component.empty()))
                .clickEvent(ClickEvent.runCommand(""));
    }
}
