package moth.boxxed.panels.content.paintbrush;

import moth.boxxed.panels.Dashpanels;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColorPalette implements Iterable<Integer> {
    public static final int MAX = 24;

    private final List<Integer> colors = new ArrayList<>();

    public ColorPalette add(int color) {
        this.colors.add(color);
        return this;
    }

    public byte[] byteArray() {
        byte[] byteArray = new byte[this.colors.size()*3+1];

        //Should probably be able to store in a single byte
        byteArray[0] = (byte) this.colors.size();
        for (int i = 0; i < this.colors.size(); i++) {
            byteArray[i*3+1] = (byte) ((this.colors.get(i) >> 16) & 0xFF);
            byteArray[i*3+2] = (byte) ((this.colors.get(i) >> 8) & 0xFF);
            byteArray[i*3+3] = (byte) (this.colors.get(i) & 0xFF);
        }
        return byteArray;
    }

    public static ColorPalette fromBytes(byte[] bytes) {
        ColorPalette ret = new ColorPalette();
        int size = bytes[0];
        for (int i = 0; i < size; i++) {
            int r = Byte.toUnsignedInt(bytes[i*3+1]);
            int g = Byte.toUnsignedInt(bytes[i*3+2]);
            int b = Byte.toUnsignedInt(bytes[i*3+3]);
            ret.add(r << 16 | g << 8 | b);
        }
        return ret;
    }

    @Override
    public @NonNull Iterator<Integer> iterator() {
        return this.colors.stream().iterator();
    }
}