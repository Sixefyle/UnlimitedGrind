package be.sixefyle.utils;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.iridium.iridiumcore.DefaultFontInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.List;

public class StringUtils {
    private final static int CENTER_PX = 154;

    public static int getLineSize(String line){
        if(line == null) return 0;

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : line.toCharArray()){
            if(c == '§'){
                previousCode = true;
                continue;
            }else if(previousCode == true){
                previousCode = false;
                if(c == 'l' || c == 'L'){
                    isBold = true;
                    continue;
                }else isBold = false;
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }
        return messagePxSize;
    }

    public static int getLongestLine(List<String> list){
        int size = 0;
        int lineSize;
        for (String s : list) {
            lineSize = getLineSize(s);
            if(lineSize > size){
                size = lineSize;
            }
        }
        return size;
    }

    public static String getCenteredText(String message, List<String> list){
        message = IridiumColorAPI.process(message);
        int size = getLongestLine(list) / 2;

        int messagePxSize = getLineSize(message);
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = size - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb + message;
    }
}
