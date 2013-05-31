package ARain;

import vgpackage.*;
import java.awt.*;

public class ScoreObj {
//	private static BEngine ge;
	private static ARain parent;
	private static ScoreObj list[];

	private final static int TOTAL = 5;
	private final static int YVEL = BEngine.TICK * 300;

	private Pt pos;
	private boolean alive;
	private int lifeSpan;
	private int score;

	public ScoreObj() {
		pos = new Pt();
	}

	public static void init(ARain parent) {
//		ScoreObj.ge = ge;
		ScoreObj.parent = parent;

		list = new ScoreObj[TOTAL];
		for (int i = 0; i < TOTAL; i++)
			list[i] = new ScoreObj();

	}

	private static void removeAll() {
		for (int i = 0; i < TOTAL; i++) {
			list[i].alive = false;
		}
	}

	public static void move() {
		if (VidGame.stageStart(parent.GS_INTRO)) {
			removeAll();
		}

		for (int j = 0; j < TOTAL; j++) {
			ScoreObj a = list[j];

			if (!a.alive) continue;

			a.lifeSpan -= 1024/VidGame.FPS;
			if (a.lifeSpan <= 0) {
				a.alive = false;
				continue;
			}

			a.pos.y -= YVEL;
		}
	}

    private static Pt workPt = new Pt();
	public static void plot() {
//		Pt sLoc = new Pt();

		for (int i = 0; i < TOTAL; i++) {

			ScoreObj a = list[i];
			if (!a.alive) continue;
			BEngine.ptWorldToView(a.pos.x, a.pos.y, workPt);

			String s = Integer.toString(a.score);

			parent.getCharSet(0).centerString(
                 s, workPt.x, workPt.y);
		}

	}

	public static void add(int score, Pt loc, int lifeSpan) {
		ScoreObj n = list[0];
		for (int i = 0; i < TOTAL; i++) {
			ScoreObj a = list[i];
			if (!a.alive || a.lifeSpan < n.lifeSpan)
				n = a;
		}

		loc.copyTo(n.pos);
		n.alive = true;
		n.lifeSpan = lifeSpan;
		n.score = score;
	}

}
