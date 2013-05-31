package ARain;
import vgpackage.*;
import mytools.*;
public class Wave {
	private static int attackDelay;		// delay until another attack wave launched
	private final static int TOTAL = 3;
	public final static int MAX_IN_ATTACK = 6;
	private static Wave list[];
	private static int yTop, yBottom;
	private static int activeWaves;

	private static int playerX;

	private static ARain parent;
	private int status;
	private Pt pos;
	private Pt vel;
	private Pt startPos;
	private int time;
	private int shootDelay;
	private Angle desAngle;
	private int seekDelay;		// delay before choosing a new velocity
	private Pt desVel;
	private Pt accel;

	private int count;
	private int aliens[];
	private int size;

	private final static int MIN_X = 50 * BEngine.ONE;
	private final static int MAX_X = ARain.MAIN_WORLD_XM - MIN_X;

	// A_STATUS values:
	private final static int ATT_UNUSED = 0;
	private final static int ATT_STARTING = 1;
	private final static int ATT_ATTACKING = 2;
	private final static int ATT_ENDING = 3;

	public static boolean attackingPhase(int index) {
		Wave w = list[index];
		return (w.status == ATT_STARTING || w.status == ATT_ATTACKING);
	}

	public static boolean active() {
		return (activeWaves != 0);
	}

	public Wave() {
		pos = new Pt();
		vel = new Pt();
		desVel = new Pt();
		startPos = new Pt();
		accel = new Pt();

		aliens = new int[MAX_IN_ATTACK];
		setStatus(ATT_UNUSED);
		desAngle = new Angle();
	}

	public static void init(ARain parent) {
		Wave.parent = parent;
		list = new Wave[TOTAL];
		for (int i = 0; i < TOTAL; i++)
			list[i] = new Wave();

		playerX = parent.MAIN_WORLD_XM / 2; //getPlayer().pos().x;
//		int plrX = (parent.MAIN_WORLD_XM / 2);
//      db.pr("resetting playerX to "+(playerX / VidGame.ONE) );

		activeWaves = 0;
		yTop = -10 << BEngine.FRACBITS;
		yBottom = parent.MAIN_WORLD_YM + (25 << BEngine.FRACBITS);
	}

	private static void setAttackDelay() {
		int level = VidGame.getLevel();
/*
                if (db.COPYPROT) {
                  if (Alien.cb != parent.CSUM && level >= 2)
                          level += 4;
                }
*/
		level = Math.min(level, 10);
		attackDelay = MyMath.rnd(1500) + (3500 - level * 300);
	}

	public static void newLevel() {
		setAttackDelay();
	}

	public static void removeAll() {
		for (int i = 0; i < TOTAL; i++)
			list[i].setStatus(ATT_UNUSED);
	}

	private void setStatus(int s) {
		if (status == ATT_UNUSED && s != ATT_UNUSED)
			activeWaves++;
		if (status != ATT_UNUSED && s == ATT_UNUSED)
			activeWaves--;
		status = s;
		time = 0;
	}

	public static int getSize(int index) {
		return list[index].size;
	}

	public static void move() {

		if (Alien.aliveTotal() != 0) {
			attackDelay -= 1024/VidGame.FPS;
			if (attackDelay <= 0) {
				if (VidGame.getStage() == parent.GS_REGROUP)
					setAttackDelay();
				else
					doAttack();
			}
		}
		int plrX = (parent.MAIN_WORLD_XM / 2);
		if (VidGame.getMode() >= VidGame.MODE_PLAYING) {
			plrX = parent.getPlayer().pos().x;
      }

		playerX = accelTo(playerX, plrX, BEngine.TICK * 3000);
//      db.pr("accel blah blah");
		for (int wave = 0; wave < TOTAL; wave++) {
			list[wave].moveOne();
		}
	}

	private static void doAttack() {

		boolean found = false;

		// Find an unused attack wave.

		int wave = 0;
		while (wave < TOTAL) {
			if (list[wave].status == ATT_UNUSED) break;
			wave++;
		}
		if (wave == TOTAL) return;

		// Determine which aliens to include in the attack.

		Wave wv = list[wave];
		wv.count = Alien.constructAttackWave(wv.aliens, wave);
		if (wv.count == 0) return;
		wv.size = wv.count;

		setAttackDelay();
		Alien a = Alien.get(wv.aliens[0]);

		wv.setStatus(ATT_STARTING);
		a.getPos(wv.pos);
		wv.seekDelay = 0;

		wv.setShootDelay();

		wv.startPos.x = wv.pos.x - Alien.waveX();
		wv.startPos.y = wv.pos.y;

		wv.vel.x = Alien.waveXVel() / 4;
		wv.vel.y = 0;

	}

	private static int accelTo(int current, int desired, int maxStep) {
		int dist = desired - current;
		if (dist < -maxStep)
			dist = -maxStep;
		if (dist > maxStep)
			dist = maxStep;
		return current + dist;
	}

	private final static int START_YVEL = BEngine.TICK * 450;
	private final static int START_YACC = BEngine.TICK * 80;
	private final static int START_XACC = BEngine.TICK * 80;

	private final static int ATT_YACC = BEngine.TICK * 200;
	private final static int ATT_XACC = BEngine.TICK * 200;

	private final static int END_YVEL = BEngine.TICK * 1200;
	private final static int END_YACC = BEngine.TICK * 40;
	private final static int END_XVEL = BEngine.TICK * 1080;

	private void setShootDelay() {
		int level = Math.min(VidGame.getLevel(), 10);
		shootDelay = MyMath.rnd(800) + (600 - level * 40);
	}

	private void moveOne() {
		if (status == ATT_UNUSED) return;

		int prevX = pos.x;
		int prevY = pos.y;

		int desX = Alien.waveX() + startPos.x;

		time += 1024 / VidGame.FPS;

		switch (status) {
		case ATT_STARTING:
			if (time > 800) {
				setStatus(ATT_ATTACKING);
				seekDelay = 0;
				Sfx.play(parent.E_WAVE);
			}
			break;

		case ATT_ATTACKING:
			break;

		case ATT_ENDING:
			{
				desAngle.setInt(pos.y > startPos.y - (30 << BEngine.FRACBITS) ? 0 : 128);
				// Seek the start position
				vel.y = accelTo(vel.y, END_YVEL, END_YACC);
				if (pos.y <= startPos.y)
					vel.y = Math.min(vel.y, startPos.y - pos.y);
				int distX = desX - pos.x;
				vel.x = MyMath.clamp(desX - pos.x, -END_XVEL, END_XVEL);
			}
			break;
		}


		if (status <= ATT_ATTACKING) {
			seekDelay -= 1024 / VidGame.FPS;
			if (seekDelay <= 0 || pos.x <= MIN_X || pos.x >= MAX_X) {
				adjustVel();
			}

			vel.x = accelTo(vel.x, desVel.x, accel.x);
			vel.y = accelTo(vel.y, desVel.y, accel.y);

			// Use the horizontal velocity as an indicator of the rotation angle.

			int des = 128 + (vel.x / (BEngine.TICK * 60));
			desAngle.approach(des << Angle.TRIGBITS, (2*Angle.MAX) / VidGame.FPS);

//                        System.out.println("desAngle="+desAngle);
		}

		pos.y += vel.y;
		pos.x += vel.x;

		if (status == ATT_ENDING) {
			if (pos.y == startPos.y && pos.x == desX) {
				setStatus(ATT_UNUSED);
			}
		}

		if (pos.y >= yBottom) {
			pos.y = yTop;
			if (
				Alien.aliveTotal() > 4
			 || VidGame.getStage() != parent.GS_NORMAL
			) {
				setStatus(ATT_ENDING);
				vel.y = 0;
				vel.x = 0;
				pos.x = desX + MyMath.rndCtr(BEngine.ONE * 60);
			}
		}

		int velX = pos.x - prevX;
		int velY = pos.y - prevY;

		// Move every alien in the wave...

		for (int i = 0; i < count; i++) {
			Alien a = Alien.get(aliens[i]);
			a.adjustPos(velX, velY);
			a.setDesAngle(desAngle);
		}
		if (status == ATT_UNUSED)
			Alien.endWave(count, aliens);

		if (status == ATT_ATTACKING) {
			shootDelay -= 1024/VidGame.FPS;
			if (shootDelay <= 0) {
				setShootDelay();
				// Choose an alien to shoot.
				int ai = MyMath.rnd(count);
				Alien.shootBullet(aliens[ai], vel.x);
			}
		}
	}


	public static boolean destroyAlien(int index, int wave) {
		Wave w = list[wave];

		for (int j = 0; ; j++) {
			if (w.aliens[j] != index) continue;

			// Remove this alien from the list by replacing with last.

			w.aliens[j] = w.aliens[w.count-1];
			w.count--;
			if (w.count == 0) {
				w.setStatus(ATT_UNUSED);
				return false;
			}
			return true;
		}
	}

	private void adjustVel() {

		accel.x = START_XACC;
		accel.y = START_YACC;
		desVel.x = 0;
		desVel.y = START_YVEL;

		if (status == ATT_ATTACKING) {
			accel.x = ATT_XACC;
			accel.y = ATT_YACC;

			int hazeFactor = Math.max(100, (350 - VidGame.getLevel() * 20));
         int iAdjust = MyMath.rndCtr(hazeFactor << BEngine.FRACBITS);
			int desX = MyMath.clamp(
				playerX + iAdjust,
				0,
				parent.MAIN_WORLD_XM);

 			int n = 0;

			n = BEngine.TICK * Math.min(1400 + 150 * VidGame.getLevel(), 3500);
			int maxHVel = (n + MyMath.rnd(n >> 2));

			int dist = Math.abs(desX - pos.x) * (VidGame.FPS >> 1);
			if (maxHVel > dist)
				maxHVel = dist;

			if (desX < pos.x)
				maxHVel = -maxHVel;

			n = VidGame.TICK * Math.min(800 + 120 * VidGame.getLevel(), 2000);
			int maxYVel = (n + MyMath.rnd(n >> 2));

			desVel.x = maxHVel;
			desVel.y = maxYVel;
		}

		seekDelay = MyMath.rnd(1000) + 300;
	}
}
