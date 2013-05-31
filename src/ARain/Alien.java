package ARain;

import vgpackage.*;
import java.awt.*;
import java.applet.Applet;
import java.net.*;
import mytools.*;

public class Alien implements VidGameGlobals {
//	private static BEngine ge;
  private static ARain parent;
  private static Alien list[];
  private static Sprite ims[];
  private static Player player;
  private static int aliveTotal;
  private static int noneAliveTime;

  private static Pt nextToMove;

  private static int waveX, waveY;
  private static int maxWaveX, minWaveX;
  private static int waveVel;
  private static int waveAnimFrame;

  private final static int MOVE_PER_CYCLE = (192 / VidGame.FPS);

  private static int bullets[];
  private final static int BULLETS = 10;

  private final static int A_HORZ = 10;
  private final static int A_VERT = 7;
  private final static int TOTAL = A_HORZ * A_VERT;

  private final static int A_SIZE_X = 24;
  private final static int A_SEP_X = 3;
  private final static int A_SIZE_Y = 24;
  private final static int A_SEP_Y = 2;

  private final static int ALIEN_IMAGES = 17;
  private final static int S_TYPE0 = 0;
  private final static int S_BULLET = S_TYPE0 + ALIEN_IMAGES * 4;
  private final static int S_TOTAL = S_BULLET + 1;

  private boolean alive;
  private int attackWave; // 0, or 1+wave
  private Pt pos;
  private boolean inBgnd;
  private boolean moved;
  private boolean wasBgnd;
  private Pt lastDrawnPos;
  private int type;
  private Angle angle;
  private Angle desAngle;
  private int animFrame;
  private int spriteIndex;
  private int hitsRequired;
  public static int cb;

  public void getPos(Pt dest) {
    pos.copyTo(dest);
  }

  public Alien() {
    pos = new Pt();
    lastDrawnPos = new Pt();
    angle = new Angle();
    desAngle = new Angle();
  }

  public void adjustPos(int xv, int yv) {
    pos.x += xv;
    pos.y += yv;
    inBgnd = false;
    moved = true;
  }

  public void setDesAngle(Angle des) {
    des.copyTo(desAngle);
  }

  public static void init(ARain parent, Player player) {
//		Alien.ge = ge;
    Alien.parent = parent;
    Alien.player = player;
    Alien.nextToMove = new Pt();

    list = new Alien[TOTAL];
    for (int i = 0; i < TOTAL; i++)
      list[i] = new Alien();

    Sprite s = new Sprite("aliens");

    ims = new Sprite[S_TOTAL];
    for (int i = 0; i < S_TOTAL; i++) {
      if (i == S_BULLET) {
        ims[i] = new Sprite("abullet", 1, 11);
        ims[i].addColRect(0, 4, 1, 12);
        continue;
      }

      int x = (i % ALIEN_IMAGES) * 24;
      int y = (i / ALIEN_IMAGES) * 24;
      ims[i] = new Sprite(s, x, y, 24, 24, 12, 12);
      ims[i].addColRect(2, 5, 21, 18);
    }

    Wave.init(parent);
    bullets = new int[BULLETS * 4];
//      iPregameCount = 0;
    /*
      // Calculate the checksum of the current document base to
      // make sure we are running the applet from our own URL,
      // to prevent someone from stealing our applet.
      if (db.COPYPROT) {
       URL u = ((Applet)parent).getDocumentBase();
       String str = u.toString().toUpperCase();
       int start = Math.max(0, str.length() - 30);
       int sum = 0;
       for (int i = start; i < str.length(); i++) {
        sum ^= (((int)str.charAt(i)) << (i & 7));
       }
       // Enable this line to find out the secret
       // checksum.  It will be displayed as the high score.
       if (VidGame.DEBUG) {
        VidGame.setHighScore(sum);
        sum = parent.CSUM;
       }
       cb = sum;
      }
     */
  }

  public static int aliveTotal() {
    return aliveTotal;
  }

  private static void removeAll() {
    for (int i = 0; i < TOTAL; i++) {
      list[i].alive = false;
    }
    aliveTotal = 0;
    Wave.removeAll();
    removeBullets();
  }

  public static void move() {
    if (VidGame.initFlag()) {
      removeAll();
    }

    noneAliveTime += 1024 / VidGame.FPS;
    if (aliveTotal != 0)
      noneAliveTime = 0;

    if (
        VidGame.getStage() != parent.GS_INTRO
        && noneAliveTime > 1000
        && VidGame.getMode() >= VidGame.MODE_PLAYING
        ) {
      VidGame.adjLevel(1);
      VidGame.setStage(parent.GS_INTRO);
      Sfx.play(parent.E_CLEARED);
    }

    // If pre-game, change waves every several seconds.
//		db.pr("VidGame mode="+VidGame.getMode()+", time="+VidGame.getTime() );

    if (
        VidGame.getMode() == VidGame.MODE_PREGAME
        && (VidGame.getTime() % (VidGame.FPS * 20) == 0)
        ) {

      // If this is the first time in the pregame mode since starting the
      // applet or playing a game, display level 0.  Otherwise, choose a
      // random low level.
      VidGame.setLevel(VidGame.getTime() == 0 ? 0 : MyMath.rnd(6) + 2);
      removeAll();
    }

    if (
        aliveTotal == 0
        && VidGame.getStage() <= parent.GS_INTRO
        && VidGame.getStageTime() > 1100
        ) {
      bringOn();
    }

    Wave.move();

    for (int j = 0; j < MOVE_PER_CYCLE; j++) {

      int i = nextToMove.y * A_HORZ + nextToMove.x;
      Alien a = list[i];
      if (a.alive && a.attackWave == 0) {
        a.pos.x += waveVel;
        a.animFrame = waveAnimFrame;
        a.moved = true;
      }

      nextToMove.x += (waveVel > 0) ? 1 : -1;
      if (nextToMove.x >= A_HORZ) {
        nextToMove.x = 0;
        nextToMove.y++;
      }
      else if (nextToMove.x < 0) {
        nextToMove.x = A_HORZ - 1;
        nextToMove.y++;
      }
      if (nextToMove.y >= A_VERT) {
        nextToMove.y = 0;
        waveAnimFrame ^= 1;

        waveX += waveVel;
        if (waveX >= maxWaveX || waveX <= minWaveX) {
          waveVel = -waveVel;
        }
        nextToMove.x = (waveVel > 0) ? 0 : A_HORZ - 1;
        break;
      }
    }

    for (int i = 0; i < TOTAL; i++) {
      Alien a = list[i];
      if (!a.alive)
        continue;
      if (a.attackWave == 0) {
        a.desAngle.set(0);
      }

      if (!a.desAngle.equals(a.angle)) {
        a.moved = true;
        a.angle.approach(a.desAngle, DASH * 1800);
       }
      else {
        if (a.attackWave == 0)
          a.inBgnd = true;
      }
      a.spriteIndex = a.spriteIndex();
    }

    moveBullets();

    testHitBullets();

    testHitPlayer();

    testBulletsHitPlayer();
  }

  // Calculate alien's position in wave
  public static void calcPosInWave(int index, Pt pos) {
    int x = index % A_HORZ;
    int y = index / A_HORZ;

    boolean moved = false;
    if (y < nextToMove.y)
      moved = true;
    else if (y == nextToMove.y) {
      if (waveVel > 0) {
        if (x < nextToMove.x)
          moved = true;
      }
      else {
        if (x > nextToMove.x)
          moved = true;
      }
    }

    pos.x = waveX + x * (A_SIZE_X + A_SEP_X) * BEngine.ONE;
    if (moved)
      pos.x += waveVel;
    pos.y = waveY + y * (A_SIZE_Y + A_SEP_Y) * BEngine.ONE;
  }

  private final int scores[] = {
      50, 80, 120,
      100, 200, 0
  };
  private final int queenScores[] = {
      300, 500, 800, 1200,
      300, 500, 300, 500,
      800, 300, 500, 300,
      300, 500, 800, 300
  };
  private final int specialQueenScores[] = {
      1500, 2000, 2500, 3000
  };

  private int spriteIndex() {
    int baseType = S_TYPE0 + (type * ALIEN_IMAGES);
    if (type == 2 && hitsRequired <= 1)
      baseType += ALIEN_IMAGES;

    int rot = ( (angle.getInt() + 8) & 0xff) >> 4;
//    if (rot != 0)
  //    System.out.println("angle="+angle+", int="+angle.getInt()+", rot="+rot);
    if (rot == 0 && attackWave == 0)
      return baseType + animFrame;

    return baseType + (rot == 0 ? 0 : 1 + rot);
  }

  private void destroy(int index) {

    hitsRequired--;
    if (hitsRequired > 0) {
      Sfx.play(parent.E_DAMAGE);
      OurExp.add(ims[spriteIndex], pos, 2700, 0, 110, 30, 350);
      return;
    }
    OurExp.add(ims[spriteIndex], pos, 1800, 0, 80);

    alive = false;
    moved = true;

    int score = 0;
    {
      int scoreInd = type;
      if (attackWave != 0
          && Wave.attackingPhase(attackWave - 1)
          ) {
        scoreInd += 3;
      }

      score = scores[scoreInd];
    }
    aliveTotal--;

    boolean remaining = true;
    int waveSize = 0;
    if (attackWave != 0) {
      waveSize = Wave.getSize(attackWave - 1);
      remaining = Wave.destroyAlien(index, attackWave - 1);
    }
    boolean queenFlag = false;

    if (score == 0) {
      queenFlag = true;
      score = queenScores[MyMath.rnd(16)];
      if (!remaining && waveSize > 2)
        score = specialQueenScores[MyMath.rnd(4)];
      ScoreObj.add(score, pos, 2000);
    }

    VidGame.adjScore(score);

    Sfx.play(queenFlag ? parent.E_QUEEN : parent.E_EXP);
  }

  private static void testHitPlayer() {
    if (!player.collidePossible())
      return;
    Pt pos = player.pos();
    Sprite s = player.sprite();

    for (int i = 0; i < TOTAL; i++) {
      Alien a = list[i];
      if (!a.alive || a.attackWave == 0)
        continue;

      if (!a.ims[a.spriteIndex].collided(a.pos.x, a.pos.y, pos.x, pos.y, s))
        continue;

      player.destroy();
      a.destroy(i);
      break;
    }
  }

  private static void testHitBullets() {
    for (int i = 0; i < TOTAL; i++) {
      Alien a = list[i];

      if (!a.alive)
        continue;

      if (player.bulletHit(a.pos, ims[a.spriteIndex])) {
        a.destroy(i);
      }
    }
  }

  private static void testBulletsHitPlayer() {

    if (!player.collidePossible())
      return;

    Pt pos = player.pos();
    Sprite s = player.sprite();

    for (int i = (BULLETS - 1) * 4; i >= 0; i -= 4) {

      if (bullets[i + 3] == 0)
        continue;

      if (!ims[S_BULLET].collided(bullets[i + 0], bullets[i + 1], pos.x, pos.y,
                                  s))
        continue;

      player.destroy();
      bullets[i + 3] = 0;
      break;
    }
  }

  public static int waveX() {
    return waveX;
  }

  public static int waveXVel() {
    return waveVel;
  }

  private static final byte form0[] = {
      3, 1, 2, 1, -1,
      2, 2, 2, 2, -1,
      2, 2, 2, 2, -1,
      3, 4, -1,
      4, 2, -2
  };
  private static final byte form1[] = {
      2, 2, 2, 2, -1,
      1, 1, 2, 2, 2, 1, -1,
      2, 2, 2, 2, -1,
      2, 6, -1,
      3, 4, -2
  };
  private static final byte form2[] = {
      1, 2, 4, 2, -1,
      0, 4, 2, 4, -1,
      0, 4, 2, 4, -1,
      2, 6, -1,
      3, 4, -1,
      4, 2, -2
  };
  private static final byte form3[] = {
      0, 4, -1,
      1, 4, -1,
      2, 4, -1,
      3, 4, -1,
      4, 4, -1,
      5, 4, -1,
      6, 4, -2
  };
  private static final byte form3b[] = {
      1, 2, 4, 2, -1,
      0, 3, 1, 2, 1, 3, -1,
      1, 2, 4, 2, -1,
      2, 2, 2, 2, -1,
      3, 4, -1,
      4, 2, -1,
      1, 2, 4, 2, -2
  };

  private static final byte form4[] = {
      1, 2, 1, 2, 1, 2, -1,
      0, 3, 1, 2, 1, 3, -1,
      1, 8, -1,
      1, 8, -1,
      2, 2, 2, 2, -2
  };

  private static final byte form4b[] = {
      0, 10, -1, 2, 6, -1,
      4, 2, -1,
      4, 2, -1,
      4, 2, -1,
      4, 2, -1,
      0, 1, 3, 2, 3, 1, -2
  };

  private static final byte form5[] = {
      1, 3, 2, 3, -1,
      0, 4, 2, 4, -1,
      1, 8, -1,
      0, 3, 1, 2, 1, 3, -1,
      1, 8, -2
  };
  private static final byte form6[] = {
      1, 3, 2, 3, -1,
      0, 10, -1,
      1, 8, -1,
      2, 6, -1,
      3, 4, -1,
      1, 8, -1,
      2, 6, -2
  };
  private static final byte form7[] = {
      0, 4, 2, 4, -1,
      0, 10, -1,
      0, 10, -1,
      0, 10, -1,
      0, 10, -1,
      1, 8, -1,
      2, 6, -2
  };

  private static final byte[] formScr[] = {
      form0,
      form1,
      form2,
      form3,
      form3b,
      form4,
      form4b,
      form5,
      form6,
      form7
  };

  private static void bringOn() {
    int width = (A_HORZ - 1) * (A_SIZE_X + A_SEP_X) * BEngine.ONE;

    waveX = (parent.MAIN_WORLD_XM - width) / 2;
    waveY = BEngine.ONE * 70;

    minWaveX = BEngine.ONE * 30;
    maxWaveX = (parent.MAIN_WORLD_XM - width) - minWaveX;
    waveVel = BEngine.ONE * 2;
    nextToMove.set(0, 0);
    waveAnimFrame = 0;

    int minX = 0;
    int maxX = A_HORZ;
    int minY = 0;
    int maxY = A_VERT;

    int level = VidGame.getLevel();

    if (level < 2) {
      minX = (2 - level);
      maxX -= minX;
      maxY -= minX;
    }

    int formation = level;
    if (formation >= 10)
      formation = MyMath.rnd(8) + 2;

    byte scr[] = formScr[formation];

    int y = 0;
    int x = 0;
    int i = 0;
    int color = 0;
    while (scr[i] != -2) {
      int j = scr[i++];
      if (j == -1) {
        y++;
        x = 0;
        color = 0;
        continue;
      }
      if (color == 0) {
        x += j;
      }
      else {
        while (j-- > 0) {
          int n = y * A_HORZ + (x++);
          Alien a = list[n];
          a.alive = true;
          aliveTotal++;
          a.attackWave = 0;
          a.type = 0;
          a.angle.set(0);

          a.hitsRequired = 1;

          if (y <= 2)
            a.type = 1;

          if (y == 0) {
            a.type = 2;
            if (level >= 5)
              a.hitsRequired = 2;
            if (level >= 12)
              a.hitsRequired = 3;
          }

          calcPosInWave(n, a.pos);

          a.inBgnd = true;
          a.moved = false;
        }
      }
      color ^= 1;
    }

    Wave.newLevel();
  }

  private static Pt workPt = new Pt();
  public static void plot(boolean bgndFlag, boolean eraseFlag) {
    if (!bgndFlag)
      plotBullets();

//        Graphics g = BEngine.getGraphics();
    if (eraseFlag)
      BEngine.setColor(Color.black);
//		Pt sLoc = new Pt();
    for (int i = 0; i < TOTAL; i++) {

      Alien a = list[i];

      if (eraseFlag) {
        if (a.wasBgnd && (!a.inBgnd || a.moved || !a.alive)) {
          BEngine.ptWorldToView(a.lastDrawnPos.x, a.lastDrawnPos.y,
                                workPt);
          BEngine.fillRect(
              workPt.x - A_SIZE_X / 2, workPt.y - A_SIZE_Y / 2,
              A_SIZE_X, A_SIZE_Y);
          a.wasBgnd = false;
        }
        continue;
      }

      if (!a.alive)
        continue;

      if (bgndFlag) {
        if (! (a.inBgnd && !a.wasBgnd && a.moved))
          continue;
        a.wasBgnd = true;
        a.lastDrawnPos.x = a.pos.x;
        a.lastDrawnPos.y = a.pos.y;
        a.moved = false;
      }
      else {
        if (a.inBgnd)
          continue;
      }

//			BEngine.ptWorldToView(a.pos.x, a.pos.y, sLoc);
      BEngine.drawSpriteWorld(ims[a.spriteIndex], a.pos);
//			BEngine.drawSprite(ims[a.spriteIndex], sLoc.x, sLoc.y);
    }

  }

  public static void endWave(int count, int ind[]) {
    for (int i = 0; i < count; i++) {
      Alien a = list[ind[i]];
      a.attackWave = 0;
      calcPosInWave(ind[i], a.pos);
    }
  }

  public static Alien get(int index) {
    return list[index];
  }

  private static final int attForms[] = {
      1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      1, 0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0,
      1, 0, -1, 0, 1, -1, -1, -1, 0, -1, 0, 0,
  };
  private static final int columnInc[] = {
      1, A_HORZ - 1, 3, A_HORZ - 3
  };

  // Construct an attack wave
  // Precondition:
  //	ind[] = destination for alien indices
  // Postcondition:
  //	returns number of aliens in wave;
  //	index of each alien returned in ind[]
  public static int constructAttackWave(int ind[], int waveIndex) {

    boolean found = false;

    int count = 0;

    // Find an alien to be the focus of the attack.

    int col = MyMath.rnd(A_HORZ);
    int colInc = columnInc[MyMath.rnd(4)];

    int row = 0;
    int key = 0;
    Alien a = null;
    find:for (int x = 0; x < A_HORZ; x++) {
      col += colInc;
      if (col >= A_HORZ)
        col -= A_HORZ;

      alien:for (int y = A_VERT - 1; y >= 0; y--) {
        key = y * A_HORZ + col;
        a = list[key];
        if (!a.alive || a.attackWave > 0)
          continue;

        // Make sure there are no aliens below us in the
        // neighboring columns.

        for (int nx = col - 1; nx <= col + 1; nx += 2) {
          if (nx < 0 || nx >= A_HORZ)
            continue;

          for (int na = nx + (y + 1) * A_HORZ; na < A_HORZ * A_VERT;
               na += A_HORZ) {
            Alien a2 = list[na];
            if (!a2.alive || a2.attackWave > 0)
              continue;
            continue alien;
          }
        }

        row = y;
        found = true;
        break find;
      }
    }
    if (!found)
      return 0;

    // Determine what type of formation to attack with.

    int formation = MyMath.rnd(3);

    // Add aliens to attack wave
    int aO = formation * (Wave.MAX_IN_ATTACK * 2);
    for (int ac = 0; ac < Wave.MAX_IN_ATTACK; ac++, aO += 2) {
      int xi = col + attForms[aO];
      int yi = row + attForms[aO + 1];
      if (xi < 0 || xi >= A_HORZ || yi < 0 || yi >= A_VERT)
        continue;

      int ai = xi + yi * A_HORZ;
      Alien al = list[ai];
      if (!al.alive || al.attackWave > 0)
        continue;

      al.attackWave = waveIndex + 1;
      ind[count++] = ai;
      if (ai == key)
        break;
    }
    return count;
  }

  private static final int BULLET_XVEL = BEngine.TICK * 400;

  public static void shootBullet(int a, int xvel) {
    int b = unusedBullet();
    if (b < 0)
      return;

    Alien alien = list[a];

    bullets[b + 0] = alien.pos.x;
    bullets[b + 1] = alien.pos.y + BEngine.ONE * 12;
    bullets[b + 2] = (xvel >> 1) + MyMath.rndCtr(BULLET_XVEL);

    int spd = BEngine.TICK * (1800 + 60 * VidGame.getLevel());
    spd = Math.min(spd, BEngine.TICK * 2300);

    /*
       if (db.COPYPROT) {
             if (Alien.cb != parent.CSUM)
                     spd = (spd * 28) / 16;
       }
     */
    bullets[b + 3] = spd + MyMath.rnd(spd >> 2);

  }

  private static int unusedBullet() {
    // Find an unused bullet.  Its y-velocity must be zero.
    for (int i = 0; i < BULLETS * 4; i += 4)
      if (bullets[i + 3] == 0)
        return i;
    return -1;
  }

  // Determine if bullet has hit a rectangular object.
  // If so, returns true and removes bullet.
  public static boolean bulletHit(int x, int y, int w, int h) {
    for (int i = (BULLETS - 1) * 4; i >= 0; i -= 4) {
      if (bullets[i + 3] == 0)
        continue;

      if (bullets[i + 0] < x || bullets[i + 0] >= x + w
          || bullets[i + 1] < y || bullets[i + 1] >= y + h
          )
        continue;

      bullets[i + 3] = 0;
      return true;
    }
    return false;
  }

  private static final int BULLET_END = ARain.MAIN_WORLD_YM + 14 * BEngine.ONE;

  private static void moveBullets() {
    for (int i = (BULLETS - 1) * 4; i >= 0; i -= 4) {
      if (bullets[i + 3] == 0)
        continue;

      int x = bullets[i + 0] + bullets[i + 2];
      int y = bullets[i + 1] + bullets[i + 3];

      if (y >= BULLET_END) {
        bullets[i + 3] = 0;
        continue;
      }
      bullets[i + 0] = x;
      bullets[i + 1] = y;
    }
  }

  private static void removeBullets() {
    for (int i = (BULLETS - 1) * 4; i >= 0; i -= 4) {
      bullets[i + 3] = 0;
    }
  }

  private static void plotBullets() {
//		Pt sLoc = new Pt();
    for (int i = (BULLETS - 1) * 4; i >= 0; i -= 4) {
      if (bullets[i + 3] == 0)
        continue;

//			BEngine.ptWorldToView(bullets[i+0], bullets[i+1], sLoc);
//            workPt.x = bullets[i+0];
//            workPt.y = bullets[i+1];
      BEngine.drawSpriteWorld(ims[S_BULLET], bullets[i + 0], bullets[i + 1]);
    }
  }
//   private static int iPregameCount;
}