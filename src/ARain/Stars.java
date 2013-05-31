// Decompiled by Jad v1.5.7a. Copyright 1997-99 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Stars.java
package ARain;

import vgpackage.*;
import mytools.*;
import java.awt.*;

public class Stars
{

    public static void init(ARain arain)
    {
        parent = arain;
//        ge = bengine;
        c = new int[240];
        d = new int[240];
        colors = new Color[8];
        int i = 0;
        final int ai[] = {
            250, 80, 40, 80, 250, 120, 40, 60, 250, 200,
            200, 70, 70, 200, 200, 200, 70, 200, 160, 180,
            230, 230, 160, 180
        };
        for(int j = 0; j < 8;)
        {
            colors[j] = new Color(ai[i], ai[i + 1], ai[i + 2]);
            j++;
            i += 3;
        }
		defined = false;
    }

    public static void erase()
    {
        if(!drawn)
        {
            return;
        } else
        {
            drawThem(drawnPhase);
            drawn = false;
            return;
        }
    }

    private static void drawThem(int i)
    {
        Graphics g = BEngine.getGraphics();
        g.setColor(Color.white);
        g.setXORMode(Color.black);
        int ai[] = {
            -4, -3, 0, 0, -4, -4, -1, 0, -3, -3,
            -2, -2, -4, -4, -1, -1, -4, -3, 0, 0,
            -4, -4, -1, 0, -3, -3, -2, -2, -4, -4,
            -1, -1, -5, -4, 0, 0, -4, -4, -1, 0,
            -5, -4, 0, 0, -4, -4, -1, 0, -5, -4,
            0, 0, -4, -4, -1, 0, -5, -4, 0, 0,
            -4, -4, -1, 0, -5, -3, -1, 0, -5, -5,
            -3, -1, -5, -4, -2, 0, -5, -3, 0, 0,
            -5, -3, -1, 0, -5, -5, -3, -1, -5, -4,
            -2, 0, -5, -3, 0, 0
        };
        for(int j = 0; j < 8; j++)
        {
            int k = (j + i) & 7;
            if((k & 0x3) != 3)
            {
                g.setColor(colors[k]);
                int l = k * 4;
                for(int i1 = j * 2; i1 < 120; i1 += 16)
                {
                    int j1 = d[i1];
                    int k1 = d[i1 + 1];
                    g.drawLine(j1, k1 + ai[l], j1, k1 + ai[l + 1]);
                    g.drawLine(j1, k1 + ai[l + 2], j1, k1 + ai[l + 3]);
                }

            }
        }

        g.setPaintMode();
    }

    public static void plot()
    {
        if(!defined)
            return;
        for(int i = 0; i < 120; i += 2)
        {
            d[i] = c[i] + BEngine.viewR.x;
            d[i + 1] = c[i + 1] + BEngine.viewR.y;
        }

        drawThem(phase);
        drawnPhase = phase;
        drawn = true;
    }

    public static void move()
    {
//        Random r = VidGame.getRandom();
        if(!defined)
        {
            defined = true;
            endY = 12 + parent.MAIN_SCRN_YM;
            for(int i = 0; i < 120; i += 2)
            {
                c[i] = MyMath.rnd(parent.MAIN_SCRN_XM);
                c[i + 1] = MyMath.rnd(endY);
            }

        }
        for(int j = 0; j < 120; j += 2)
        {
            int k = c[j + 1];
            if((k += 2) >= endY)
            {
                k -= endY;
                c[j] = MyMath.rnd(parent.MAIN_SCRN_XM);
            }
            c[j + 1] = k;
        }

        moveDelay += 256;
        if(moveDelay >= 1024)
        {
            moveDelay = 0;
            phase = (phase + 1) & 7;
        }
    }

    public Stars()
    {
    }

    private static ARain parent;
//    private static BEngine ge;
    private static int c[];
    private static final int TOTAL = 120;
    private static boolean defined;
    private static int endY;
    private static int d[];
    private static boolean drawn;
    private static final int SPEED = 2;
    private static final int COLOR_SPEED = 256;
    private static final int COLORS = 8;
    private static Color colors[];
    private static int phase;
    private static int drawnPhase;
    private static int moveDelay;
}
