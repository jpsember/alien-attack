package ARain;

import vgpackage.*;
import java.awt.*;

public class ScorePnl {

	private static final int SCORES_TOTAL = 2;
	private ARain parent;
//	private BEngine ge;
	private Player player;

	private Sprite spr[];

	private final int sData[] = {
		0,0,	16,24,	8,12,
		17,0,	24,25,	12,12,
		41,0,	24,25,	12,12,
		65,0,	24,25,	12,12,
	};

	public ScorePnl(ARain parent, Player player) {
//		this.ge = ge;
		this.parent = parent;
		this.player = player;

		Sprite m = new Sprite("markers");

		spr = new Sprite[4];
		int j = 0;
		for (int i = 0; i < 4; i++, j+=6) {
			spr[i] = new Sprite(m, sData[j+0],sData[j+1],sData[j+2],sData[j+3],sData[j+4],sData[j+5]);
		}
	}

	public void plotChanges() {//boolean valid) {
        if (!BEngine.layerValid()) {
			BEngine.clearView();
			Sprite title = new Sprite("title");
			BEngine.drawSprite(title, 0, 0);
		}

		plotLevel();//g, valid);
		plotScores();//g, valid);
		plotMen();//g, valid);
		plotMsg();//g, valid);
	}

	private final static int SCORE_DIGITS_TOTAL = 7;

	private final static int SCORE_LABEL_X = 8;
	private final static int SCORE_VALUE_X = 116;
	private final static int scoreY[] = {400,420};

	private int drawnScores[];
	private String strs[];

	private void plotScores() {
        boolean valid = BEngine.layerValid();
		if (!valid) {
			strs = new String[SCORES_TOTAL];
			drawnScores = new int[SCORES_TOTAL];
			for (int i = 0; i<SCORES_TOTAL; i++)
				strs[i] = new String();
			parent.getCharSet(1).plotString(getString(STR_SCORE),SCORE_LABEL_X,scoreY[0]);
			parent.getCharSet(1).plotString(getString(STR_HIGH),SCORE_LABEL_X,scoreY[1]);
		}
		for (int i = 0; i < SCORES_TOTAL; i++) {
			if (!valid || drawnScores[i] != VidGame.getScore(i)) {
				drawnScores[i] = VidGame.getScore(i);

				BEngine.setColor(Color.black);
				parent.getCharSet(1).clearBounds(SCORE_VALUE_X,scoreY[i],SCORE_DIGITS_TOTAL);
				parent.getCharSet(1).plotString(cvtScoreToString(drawnScores[i]),SCORE_VALUE_X,scoreY[i]);
			}
		}
	}

	private final static int MEN_X = 18;
	private final static int MEN_Y = 330;
	private final static int MEN_SPACING = 29;
	private final static int MEN_TOTAL = 6;
	private final static int MEN_WIDTH = 28;
	private final static int MEN_HEIGHT = 36;

	private int menPlotFlags;

	private void plotMen() {
        boolean valid = BEngine.layerValid();

		int newPlotFlags = 0;

		for (int i = 0; i < MEN_TOTAL; i++) {
			int flag = 1 << i;
			int plotFlag = 0;

			if (VidGame.getMode() == VidGame.MODE_PLAYING)
				plotFlag = (VidGame.getLives() > i) ? flag : 0;

			if (!valid || ((menPlotFlags & flag) != plotFlag)) {

				int x = MEN_X + MEN_SPACING * i;

				BEngine.setColor(Color.black);
				BEngine.fillRect(x-MEN_WIDTH/2, MEN_Y - MEN_HEIGHT/2, MEN_WIDTH, MEN_HEIGHT);
				if (plotFlag != 0)
					BEngine.drawSprite(player.icon(), x, MEN_Y);
			}
			newPlotFlags |= plotFlag;
		}
		menPlotFlags = newPlotFlags;
	}



	private final static int LEVEL_X = 4;
	private final static int LEVEL_Y = 350;
	private final static int LEVEL_WIDTH = 	200;
	private final static int LEVEL_HEIGHT = 26;

	private int oldLevel;
	private final int markerValues[] = {1,5,10,50};

	private void plotLevel() {
        boolean valid = BEngine.layerValid();
		int newLevel = VidGame.getLevel() + 1;

		if (VidGame.getMode() == VidGame.MODE_PREGAME)
			newLevel = 0;

		if (newLevel != oldLevel || !valid) {
			BEngine.setColor(Color.black);
			BEngine.fillRect( LEVEL_X, LEVEL_Y, LEVEL_WIDTH, LEVEL_HEIGHT);
			oldLevel = newLevel;

			int rem = newLevel;
			int x = LEVEL_X;

			while (rem > 0) {

				int s = 3;
				while (rem < markerValues[s]) s--;

				rem -= markerValues[s];

				int w = 26;
				if (s == 0)
					w = 18;
				BEngine.drawSprite(spr[s], x + w/2, LEVEL_Y + 12);
				x += w;
			}
		}
	}

	final static int powers10[] = {1,10,100,1000,10000,100000,1000000};

	protected static String cvtScoreToString(int n) {
		int size = 0;

		String s = new String();

		boolean leadZero = false;
		for (int i = SCORE_DIGITS_TOTAL-1; i >= 0; i--) {
			int d = n / powers10[i];
			if (d > 0 || i == 0)
				leadZero = true;
			if (leadZero) {
				s += (char)('0'+d);
				size++;
			}
			n -= d * powers10[i];
		}
		while (size < SCORE_DIGITS_TOTAL) {
			s += ' ';
			size++;
		}
		return s;
	}

	private final int MSG_LOADING = 1<<0;
	private final int MSG_SPACEBAR = 1<<1;
	private final int MSG_SPACEBAR2 = 1<<2;
	private final int MSG_SPACEBAR3 = 1<<3;
	private final int MSG_CONTROLS = 1<<4;
	private final int MSG_CONTROLS2 = 1<<5;
	private final int MSG_CONTROLS3 = 1<<6;
	private final int MSG_CONTROLS4 = 1<<7;
	private final int MSG_GAMEOVER = 1<<8;
	private final int MSG_PAUSED = 1<<9;
	private final int MSG_TOTAL = 10;

	private int prevMsgBits;

    private String getString(int index) {
        return stringList[index + STR_TOTAL *
            VidGame.getLanguage(stringList.length / STR_TOTAL)];
    }

    private static final int STR_LOADING = 0;
    private static final int STR_SCORE = 10;
    private static final int STR_HIGH = 11;
    private static final int STR_TOTAL = 12;

	private static final String stringList[] = {
        // LANG_ENGLISH
        "LOADING...",
        "PRESS",
        "SPACE BAR",
        "TO START",
        "CONTROLS",
        "           ",
        "  F    FIRE",
        "<   >  MOVE",
        "GAME OVER",
        "GAME PAUSED",
        "SCORE:",
        " HIGH:",

        // LANG_FRENCH
        "CHARGEMENT...",
        "CLIQUEZ",
        "BARRE D'ESPACE",
        "POUR DEMARRER",
        "CONTROLES",
        "            ",
        "  F   TIRER ",
        "<   > BOUGER",
        "GAME OVER",
        "GAME PAUSED",
        "SCORE:",
        " HIGH:",
    };

	private void plotMsg() {
        boolean valid = BEngine.layerValid();
		// Determine what messages to plot

		int msgBits = 0;

		if (VidGame.getMode() == VidGame.MODE_GAMEOVER)
			msgBits |= MSG_GAMEOVER;

		if (VidGame.paused())
			msgBits |= MSG_PAUSED;

		if (VidGame.getMode() == VidGame.MODE_PREGAME) {
			int time = (VidGame.getTime() * (1024 / VidGame.FPS)) % 14000;
			if (VidGame.loading()) //getMode() == VidGame.MODE_LOADING)
				msgBits |= MSG_LOADING;
			else if (time < 6000)
				msgBits |= MSG_SPACEBAR | MSG_SPACEBAR2 | MSG_SPACEBAR3;
			else {
				for (int i = 0; i<2; i++) {
					if (time >= (6000 + i*1024)) {
						final int ctrlMsg[] = {MSG_CONTROLS,MSG_CONTROLS2|MSG_CONTROLS3|MSG_CONTROLS4};
						msgBits |= ctrlMsg[i];
					}
				}
			}
		}

		// Now that we know what to plot, plot any changes that have occurred.

		for (int pass = 0; pass < 2; pass++) {
			for (int i = 0; i < MSG_TOTAL; i++) {
				if (valid && (((msgBits ^ prevMsgBits) & (1 << i)) == 0)) continue;

				final int msgY[] = {
					235,

					235,
					255,
					275,

					210,
					245,
					265,
					285,

					255,

					255,
				};

				final int MSG_X = 10;

				if (pass == 0) {
					BEngine.setColor(Color.black);
					parent.getCharSet(1).clearBounds(MSG_X,msgY[i],17);
					continue;
				}

				if ((msgBits & (1 << i)) != 0) {
					parent.getCharSet(1).centerString( getString(STR_LOADING + i),BEngine.viewR.width/2, msgY[i]);
				}
			}
		}
		prevMsgBits = msgBits;
	}
}