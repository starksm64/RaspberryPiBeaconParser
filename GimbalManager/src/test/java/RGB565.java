
public class RGB565 {

    public static short ARGB8_to_RGB565(int argb){
        int a = (argb & 0xFF000000) >> 24;
        int r = (argb & 0x00FF0000) >> 16;
        int g = (argb & 0x0000FF00) >> 8;
        int b = (argb & 0x000000FF);

        r  = r >> 3;
        g  = g >> 2;
        b  = b >> 3;

        return (short) (b | (g << 5) | (r << (5 + 6)));
    }

    public static int RGB565_to_ARGB8(short rgb565){
        int a = 0xff;
        int r = (rgb565 & 0xf800) >> 11;
        int g = (rgb565 & 0x07e0) >> 5;
        int b = (rgb565 & 0x001f);

        r  = r << 3;
        g  = g << 2;
        b  = b << 3;

        return (a << 24) | (r << 16) | (g << 8) | (b);
    }
}