package ARain;

import vgpackage.*;
import java.awt.*;
import mytools.*;

public class Player {
	private ARain parent;

	private boolean alive;
	private Pt pos;
	private int fireDelay;
	private int flameAnim;
	private int shineAnim;
	private final static int FLAME_FRAMES = 12;
	private final static int FLAME_FPS = 8;
	private final static int SHINE_FRAMES = 20;
	private final static int SHINE_FPS = 16;

	private Sprite ims[];

	private final static int SPEED = BEngine.TICK * 16 * 150;
	private final static int INSET = 20 << BEngine.FRACBITS;

	private final static int S_SHIP = 0;
	private final static int S_FLAMES = 1;
	private final static int S_SHINE = 5;

	private final int S_BULLET = 10;
	private final int S_TOTAL = 11;

	public Sprite icon() {
		return ims[S_SHIP];
	}

	private int bullets[];
	private final static int BULLETS = 3;
	private final static int BULLET_SPEED = BEngine.TICK * 16 * 450;

	public Pt pos() {
		return pos;
	}

	private final static int shipCol[] = {
		0,14,4,23,
		19,14,23,23,
		10,0,13,12,
		6,13,17,21
	};

	public Player(ARain parent) {
		this.parent = parent;
		pos = new Pt();

		bullets = new int[BULLETS * 4];

		Sprite base = new Sprite("ship",0,0);

		ims = new Sprite[S_TOTAL];

		ims[S_SHIP] = new Sprite(base, 0, 0, 24, 24, 12, 17);
		ims[S_SHIP].addColRect(shipCol);

		for (int i = 0; i < 4; i++) {
			ims[S_FLAMES+i] = new Sprite(base, 2 + i*8, 27, 9,6, 4, -6);
		}
		for (int i = 0; i < 5; i++) {
			ims[S_SHINE+i] = new Sprite(base, 28 + i*6,  16, 4, 8, 2, 17 - 16);
		}

		ims[S_BULLET] = new Sprite("bullet",1,1);
		ims[S_BULLET].addColRect(0,0, 1,10);
	}

	public void destroy() {
		pos.x -= BEngine.ONE;
		for (int i = 0; i < 2; i++, pos.x += BEngine.ONE*2) {
			OurExp.add(ims[S_SHIP], pos, i == 0 ? 300 : 200, 0, 40);
		}
		alive = false;
		VidGame.setStage(parent.GS_REGROUP);
		Sfx.play(parent.E_SHIPEXP);
	}
	public boolean collidePossible() {
		return alive;
	}

	public Sprite sprite() {
		return ims[S_SHIP];
	}

	public void move() {
      Joystick joystick = VidGame.getJoystick();
		if (VidGame.getMode() != VidGame.MODE_PLAYING) return;
		if (!alive) {
			if (VidGame.stageStart(parent.GS_NORMAL)) {
				alive = true;
				pos.x = parent.MAIN_WORLD_XM / 2;
				pos.y = parent.MAIN_WORLD_YM - (20 << VidGame.FRACBITS);

if (false)
                pos.y = parent.MAIN_WORLD_YM / 2;


				VidGame.adjustLives(-1);
				Sfx.play(parent.E_NEWSHIP);
			}
		}

		if (alive) {
			flameAnim += (1024 * FLAME_FPS) / (VidGame.FPS * FLAME_FRAMES);
			flameAnim &= 0x3ff;
			shineAnim += (1024 * SHINE_FPS) / (VidGame.FPS * SHINE_FRAMES);
			shineAnim &= 0x3ff;

			pos.x += joystick.xPos() * SPEED;
			pos.x = MyMath.clamp(pos.x, INSET, parent.MAIN_WORLD_XM - INSET);

			fireDelay -= 1024/VidGame.FPS;
			if (fireDelay <= 0) {
				if (joystick.fireButtonClicked(0)) {
					fireDelay = 1024 / 7;

					int i = unusedBullet();
					if (i >= 0) {

						bullets[i] = pos.x;
						bullets[i+1] = pos.y - 10*BEngine.ONE;
						bullets[i+2] = 0;
						bullets[i+3] = -BULLET_SPEED;

						Sfx.play(parent.E_SHOOT);
					}
				}
			}
		} else
			joystick.clear();

		moveBullets();
	}

	private int unusedBullet() {
		// Find an unused bullet.  Its y-velocity must be zero.
		int i = 0;

		for (; i < BULLETS*4; i+=4) {
			if (bullets[i+3] == 0) return i;
		}
		return -1;
	}

	// Determine if a player's bullet has hit a sprite.
	// If so, returns true and removes bullet.
	public boolean bulletHit(Pt pos, Sprite s) {
		for (int i = 0; i < BULLETS*4; i+=4) {
			if (bullets[i+3] == 0) continue;

			if (!s.collided(pos.x, pos.y, bullets[i+0], bullets[i+1], ims[S_BULLET]))
				continue;
			bullets[i+3] = 0;
			return true;
		}
		return false;
	}

	private void moveBullets() {
		for (int i = 0; i < BULLETS*4; i+=4) {
			if (bullets[i+3] == 0) continue;

			int x = bullets[i+0];
			int y = bullets[i+1] + bullets[i+3];
			if (y < BEngine.ONE * -16) {
				bullets[i+3] = 0;
				continue;
			}
			bullets[i+1] = y;
		}
	}

	private void plotBullets() {
//		Pt sLoc = new Pt();
		for (int i = 0; i < BULLETS*4; i+=4) {
			if (bullets[i+3] == 0) continue;

//			BEngine.ptWorldToView(bullets[i+0], bullets[i+1], sLoc);
			BEngine.drawSpriteWorld(ims[S_BULLET], bullets[i+0], bullets[i+1]);
		}
	}

	private final int shineX[] = {-10,9};
	private final byte flameFrames[] = {
		0,1,3,2,0,3,1,2,0,1,3,2,
	};

    private static Pt workPt = new Pt();
	public void plot() {

		plotBullets();
		if (!alive) return;

//		Pt sLoc = new Pt();
//		BEngine.ptWorldToView(pos.x, pos.y, sLoc);

		BEngine.drawSpriteWorld(
			ims[S_SHIP], pos);//sLoc.x, sLoc.y);

		BEngine.drawSpriteWorld( ims[S_FLAMES +
			flameFrames[(flameAnim * FLAME_FRAMES) >> 10]
		], pos);//sLoc.x, sLoc.y);

		int shine = (shineAnim * SHINE_FRAMES) >> 10;
		if (shine < 5) {
            BEngine.ptWorldToView(pos.x, pos.y, workPt);
			for (int j = 0; j < 2; j++) {
				BEngine.drawSprite(ims[S_SHINE+shine],
                    workPt.x + shineX[j],
                    workPt.y);
			}
		}
	}
}