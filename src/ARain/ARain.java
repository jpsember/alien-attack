package ARain;

import vgpackage.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import mytools.*;

public class ARain extends java.applet.Applet
    implements Runnable, VidGameInt {

	public static final int MAIN_SCRN_XM = 392;
	public static final int MAIN_SCRN_YM = 434;
	public static final int MAIN_WORLD_XM = (MAIN_SCRN_XM * BEngine.ONE);
	public static final int MAIN_WORLD_YM = (MAIN_SCRN_YM * BEngine.ONE);

	public static final int E_SHOOT = 0;
	public static final int E_EXP = 1;
	public static final int E_DAMAGE = 2;
	public static final int E_CLEARED = 3;
	public static final int E_NEWSHIP = 4;
	public static final int E_SHIPEXP = 5;
	public static final int E_WAVE = 6;
	public static final int E_LIFE = 7;
	public static final int E_QUEEN = 8;

	public static final int GS_INTRO = 0;		// introducing new level
	public static final int GS_NORMAL = 1;		// shooting at aliens
	public static final int GS_REGROUP = 2;	// player died, waiting to bring on

    public static final boolean TOURN = false;   // Lombart.com tournament mode?

   public Player getPlayer() {
      return player;
   }

   public CharSet getCharSet(int i) {
      return charSets[i];
   }

   /*
	public int getStage() {
		return stage;
	}
	public int getPrevStage() {
		return prevStage;
	}

	public boolean stageStart(int testStage) {
		return (stage == testStage && stageTime == 0);
	}

	public int stageTime() {
		return stageTime;
	}

	public void setStage(int newStage) {
		pendingStage = newStage + 1;
	}
    */

    public void update(Graphics g) {
        paint(g);
    }

	public void paint(Graphics g) {

		if (!VidGame.beginPaint()) return;

		// Prepare for update.  Constructs offscreen buffers if required.
		BEngine.prepareUpdate();

		// Process bg layer
        BEngine.openLayer(BEngine.L_BGND);
		plotBgnd();
        BEngine.closeLayer();

		// Process sprite layer
		BEngine.openLayer(BEngine.L_SPRITE);
		BEngine.selectView(VIEW_MAIN);
		Stars.erase();
		BEngine.erase();
// Testing webspace problems
if (false) ; else

		plotSprites();
        BEngine.closeLayer();
		BEngine.updateScreen(g);
		VidGame.endPaint();
	}

	// ===================================
	// Applet interface
	// ===================================
	public void init() {
   	final int bonusScores[] = {20000,50000,100000,-100000};

        VidGame.doInit(this);

		VidGame.setBonusScores(bonusScores);
		VidGame.setHighScore(2000);

        if (TOURN)
            readTournamentParameters();

		BEngine.open();

		player = new Player(this);
		Alien.init(this, player);
		Stars.init(this);
		ScoreObj.init(this);
		OurExp.init();
		scorePnl = new ScorePnl(this, player);

      charSets = new CharSet[2];
		charSets[0] = new CharSet(new Sprite("charset0",0,0),8,12,1,1,
         "0123456789");
		charSets[1] = new CharSet(new Sprite("charset1",0,0),15,17,1,1,
			"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ.,!<>^~:%()?'");
		charSets[1].setSpacingX(-4);
	}

	public void start() {
		VidGame.doStart();
      Sfx.open(sfxNames);
		VidGame.setBonusSfx(E_LIFE);
	}

	public void run() {
		VidGame.doRun();
	}

	public void stop() {
		VidGame.doStop();
      Sfx.close();
	}

	public void destroy() {
      BEngine.close();
		VidGame.doDestroy();
	}

	// ===================================
	// VidGame interface
	// ===================================
	public void processLogic() {
		updateStage();
		player.move();
		Alien.move();
		Stars.move();
		ScoreObj.move();
		OurExp.move();		// should be done last.
	}
	// ===================================

	private int animTimer;
	private ScorePnl scorePnl;

	private static final int VIEW_STATUS = 1;
	private static final int VIEW_MAIN = 2;

	private static final String sfxNames[] = {
		"shoot","explode","damage","cleared",
      "newship","shipexp","wave","life","queen"
	};

	private Player player;
	private int stage, prevStage;
	private int stageTime;
	private int pendingStage;
	private CharSet[] charSets;

	private void updateStage() {
//		stageTime += 1024 / VidGame.FPS;
        int stage = VidGame.getStage();
        int stageTime = VidGame.getStageTime();

		switch (stage) {
		case GS_INTRO:
		 	if (stageTime > 2000)
				VidGame.setStage(GS_NORMAL);
            break;

         case GS_NORMAL:
			if (
				VidGame.getMode() == VidGame.MODE_PREGAME
             && !VidGame.loading()
			 && stage == GS_NORMAL
			 && stageTime > 20000
			) {
				VidGame.setStage(GS_INTRO);
			}
            break;

         case GS_REGROUP:
            if (!Wave.active() && stageTime > 3000) {
				VidGame.setStage(GS_NORMAL);
				if (VidGame.getLives() == 0) {
					VidGame.setMode(VidGame.MODE_GAMEOVER);
                    if (TOURN)
                        processTournamentScore();
                }
			}
            break;
        }
/*
		if (VidGame.initFlag())
			setStage(GS_INTRO); */

        VidGame.updateStage();
/*		prevStage = stage;
		if (pendingStage > 0) {
			stage = pendingStage - 1;
			stageTime = 0;
			pendingStage = 0;
		} */
	}

	private void plotBgnd() {
		boolean valid = BEngine.layerValid();

		if (!valid) {
			BEngine.clearView(Color.red);
if (false) {
    			BEngine.defineView(VIEW_MAIN, 236, 0, MAIN_SCRN_XM, MAIN_SCRN_YM);
    			BEngine.defineView(VIEW_STATUS, 0, 0, 236, 434);
} else {
                BEngine.defineView(VIEW_MAIN, 0, 0, MAIN_SCRN_XM, MAIN_SCRN_YM);
                BEngine.defineView(VIEW_STATUS, 392, 0, 236, 434);
            }
		}

		BEngine.selectView(VIEW_STATUS);
		scorePnl.plotChanges();//valid);

		BEngine.selectView( VIEW_MAIN);
		if (!valid)
			BEngine.clearView();	//, Color.blue);

		Alien.plot(true, true);
		Alien.plot(true, false);
	}

	private void plotSprites() {
		BEngine.selectView(VIEW_MAIN);
		Stars.plot();
		OurExp.draw();
		player.plot();
		Alien.plot(false, false);
		ScoreObj.plot();
	}

    // ------------------------------------------------------------
    // These methods & variables are only used if TOURN is true:
    // ------------------------------------------------------------

    private String playerId;;
    private String tournamentId;

    // Read the player and tournament ID's from the applet's HTML tags.
    //
    private void readTournamentParameters() {
        playerId = VidGame.getApplet().getParameter("PLAYERID");
        tournamentId = VidGame.getApplet().getParameter("TOURNID");
    }

    // Process the score for tournament play.
    //
    private void processTournamentScore() {
        if (playerId == null || tournamentId == null) return;

        // Attempt to go to a webpage
        try {
            String s;

            int checksum = createChecksum(playerId + tournamentId, VidGame.getScore());
            //Debug.print("processTournamentScore, score="+VidGame.getScore()+
            // " playerId="+playerId+", tournamentId="+tournamentId+", checksum="+checksum);

            s = "http://www.loooping.com/jeux/thundergames/"+
                "update.php?id_joueur="+playerId+"&id_tournoi="+tournamentId+
                "&score="+VidGame.getScore()+"&"+
                "checksum="+checksum;

            //Debug.print("attempting to load html page "+s);

            // s = VidGame.getApplet().getDocumentBase().toString() + "/welcome.htm";

            VidGame.getApplet().getAppletContext()
             .showDocument(new URL(s),"maj");
        } catch (java.net.MalformedURLException e) {
            Debug.print("Malformed URL Exception: "+e);
        }
    }


    private static final String boogie =
        "please enter a name and e-mail before executing ok. Quiet error was "+
        "found in jump (defect code xz123gh).";

    private int createChecksum(String name, int score) {
        score%=2001011;

        int checksum = 0;
        for(int i=0;i<name.length();i++) {
            int t = boogie.indexOf(Character.toLowerCase(name.charAt(i)));
            if (t != -1) checksum+=53-t;
        }

        checksum += (score % 7) * (score * name.length()) + (score % 33745)
            + score;
        if (score > 100107) checksum -= 3243;
        if (score < 10003) checksum *= name.length();
        if (score < 20125) checksum += score*score;

        return checksum;
    }

    // ------------------------------------------------------------
}
