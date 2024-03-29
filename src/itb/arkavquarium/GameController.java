package itb.arkavquarium;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * <h1>GameController! The GameController
 * class controls the state of the game as
 * well as controlling what's going to
 * be shown by the GameView.</h1>
 *
 * @author Abram Situmorang
 * @author Faza Fahleraz
 * @author Senapati Sang Diwangkara
 * @author Yusuf Rahmat Pratama
 * @version 0.0
 * @since 2018-04-15
 */
class GameController extends JPanel implements ActionListener {

  private LinkedList<Guppy> contentGuppy;
  private LinkedList<Piranha> contentPiranha;
  private LinkedList<Snail> contentSnail;
  private LinkedList<Coin> contentCoin;
  private LinkedList<Pellet> contentPellet;
  // Events
  private int mouseClickX;
  private int mouseClickY;
  private boolean mouseClickValid;
  // Game state
  private Aquarium aquarium;
  private GameState gameState;
  private int coinCount;
  private int eggCount;
  // Audio
  private boolean musicPlayed;
  private Clip musicClip;
  private GameState gameMusicState;

  /**
   * A constructor. Constructs a new GameController object.
   */
  public GameController() {

    // Add keyboard event listener
    addKeyListener(new KAdapter());

    // Add mouse event listener
    addMouseListener(new MAdapter());
    mouseClickValid = false;

    // Setup window
    setBackground(Color.black);
    setFocusable(true);
    setPreferredSize(new Dimension(Constants.GRAPHICS_WIN_WIDTH, Constants.GRAPHICS_WIN_HEIGHT));

    // Init game state
    gameState = GameState.uninitialized;
    musicPlayed = false;

    // Init game timer, choose framerate
    Timer timer = new Timer(Constants.GRAPHICS_FRAME_DELAY, this);
    timer.start();
  }

  /**
   * Entry point of the game.
   * @param args Arguments provided.
   */
  public static void main(String[] args) {

    EventQueue.invokeLater(() -> {
      JFrame ex = new GameView();
      ex.setVisible(true);
    });
  }

  private void initGameState() {

    // Init game state
    aquarium = new Aquarium(Constants.GAME_SCREEN_LEFT_PADDING, Constants.GAME_SCREEN_TOP_PADDING,
      Constants.GRAPHICS_WIN_WIDTH - Constants.GAME_SCREEN_RIGHT_PADDING,
      Constants.GRAPHICS_WIN_HEIGHT - Constants.GAME_SCREEN_BOTTOM_PADDING);
    coinCount = Constants.GAME_INITIAL_COIN;
    eggCount = 0;

    // Get aquarium objects
    contentGuppy = aquarium.getContentGuppy();
    contentPiranha = aquarium.getContentPiranha();
    contentSnail = aquarium.getContentSnail();
    contentCoin = aquarium.getContentCoin();
    contentPellet = aquarium.getContentPellet();
  }

  private void updateGameState() {

    // Update game state
    aquarium.updateState((double) Constants.GRAPHICS_FRAME_DELAY / 1000);

    // Get updated aquarium objects
    contentGuppy = aquarium.getContentGuppy();
    contentPiranha = aquarium.getContentPiranha();
    contentSnail = aquarium.getContentSnail();
    contentCoin = aquarium.getContentCoin();
    contentPellet = aquarium.getContentPellet();

    // Check win/lose
    if (eggCount >= 3) {
      gameState = GameState.won;
    } else if (coinCount < Constants.GUPPY_PRICE
        && contentGuppy.getLength() == 0
        && contentPiranha.getLength() == 0
        && contentCoin.getLength() == 0) {
      gameState = GameState.lost;
    }

    // Get coins form snails
    Iterator<Snail> snailIterator = contentSnail.iterator();
    while (snailIterator.hasNext()) {
      Snail snail = snailIterator.next();
      coinCount += snail.getCoin();
      snail.resetCoin();
    }
  }

  @Override
  public void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);

    drawAssets(graphics);
  }

  private void drawAssets(Graphics graphics) {

    switch (gameState) {
      case uninitialized:
        drawMainMenu(graphics);

        break;
      case won:
        drawWinMenu(graphics);

        break;
      case lost:
        drawLoseMenu(graphics);

        break;
      default:
        drawAquarium(graphics);
        drawTopBar(graphics);

        // Draw guppy
        Iterator<Guppy> guppyIterator = contentGuppy.iterator();
        while (guppyIterator.hasNext()) {
          Guppy guppy = guppyIterator.next();
          drawGuppy(graphics, guppy);
        }

        // Draw piranha
        Iterator<Piranha> piranhaIterator = contentPiranha.iterator();
        while (piranhaIterator.hasNext()) {
          Piranha piranha = piranhaIterator.next();
          drawPiranha(graphics, piranha);
        }

        // Draw snail
        Iterator<Snail> snailIterator = contentSnail.iterator();
        while (snailIterator.hasNext()) {
          Snail snail = snailIterator.next();
          drawSnail(graphics, snail);
        }

        // Draw coin
        Iterator<Coin> coinIterator = contentCoin.iterator();
        while (coinIterator.hasNext()) {
          Coin coin = coinIterator.next();
          drawCoin(graphics, coin);
        }

        // Draw pellet
        Iterator<Pellet> pelletIterator = contentPellet.iterator();
        while (pelletIterator.hasNext()) {
          Pellet pellet = pelletIterator.next();
          drawPellet(graphics, pellet);
        }

        break;
    }
  }

  private void drawAquarium(Graphics graphics) {
    String assetPath = "assets/graphics/statics/aquarium_background.jpg";
    Image aquariumImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(aquariumImage, -5, 0, this);
  }

  private void drawTopBar(Graphics graphics) {
    // Draw top bar UI
    String assetPath = "assets/graphics/statics/aquarium_ui.png";
    Image topBarImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(topBarImage, 0, 0, this);

    // Load font
    Font font;
    try {
      font = Font.createFont(Font.TRUETYPE_FONT, new File("assets/fonts/Oswald-Heavy.ttf"));
    } catch (IOException | FontFormatException e) {
      font = new Font("Helvetica", Font.BOLD, Constants.COIN_TEXT_SIZE);
    }
    graphics.setFont(font);

    // Draw coin count text
    font = font.deriveFont((float) Constants.COIN_TEXT_SIZE);
    graphics.setFont(font);
    graphics.setColor(new Color(Constants.COIN_TEXT_COLOR_R,
        Constants.COIN_TEXT_COLOR_G,
        Constants.COIN_TEXT_COLOR_B));

    String coinPriceText = "" + coinCount;
    graphics.drawString(coinPriceText, Constants.COIN_TEXT_X, Constants.COIN_TEXT_Y);

    // Draw prices text
    font = font.deriveFont((float) Constants.PRICE_TEXT_SIZE);
    graphics.setFont(font);
    graphics.setColor(new Color(Constants.PRICE_TEXT_COLOR_R, Constants.PRICE_TEXT_COLOR_G,
        Constants.PRICE_TEXT_COLOR_B));

    String guppyPriceText = "" + Constants.GUPPY_PRICE;
    graphics.drawString(guppyPriceText, Constants.GUPPY_PRICE_TEXT_X, Constants.GUPPY_PRICE_TEXT_Y);

    String pelletPriceText = "" + Constants.PELLET_PRICE;
    graphics.drawString(pelletPriceText,
        Constants.PELLET_PRICE_TEXT_X,
        Constants.PELLET_PRICE_TEXT_Y);

    String piranhaPriceText = "" + Constants.PIRANHA_PRICE;
    graphics.drawString(piranhaPriceText,
        Constants.PIRANHA_PRICE_TEXT_X,
        Constants.PIRANHA_PRICE_TEXT_Y);

    String snailPriceText = "" + Constants.SNAIL_PRICE;
    graphics.drawString(snailPriceText, Constants.SNAIL_PRICE_TEXT_X, Constants.SNAIL_PRICE_TEXT_Y);

    String eggPriceText = "" + Constants.EGG_PRICE;
    graphics.drawString(eggPriceText, Constants.EGG_PRICE_TEXT_X, Constants.EGG_PRICE_TEXT_Y);

    // Draw egg count text
    font = font.deriveFont((float) Constants.EGG_COUNT_TEXT_SIZE);
    graphics.setFont(font);
    graphics.setColor(new Color(Constants.EGG_COUNT_TEXT_COLOR_R, Constants.EGG_COUNT_TEXT_COLOR_G,
        Constants.EGG_COUNT_TEXT_COLOR_B));

    String eggCountText = "" + eggCount;
    graphics.drawString(eggCountText, Constants.EGG_COUNT_TEXT_X, Constants.EGG_COUNT_TEXT_Y);
  }

  private void drawMainMenu(Graphics graphics) {
    String assetPath = "assets/graphics/statics/main_menu.jpg";
    Image mainMenuImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(mainMenuImage, 0, 0, this);
  }

  private void drawWinMenu(Graphics graphics) {
    String assetPath = "assets/graphics/statics/win_menu.jpg";
    Image winMenuImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(winMenuImage, -5, 0, this);
  }

  private void drawLoseMenu(Graphics graphics) {
    String assetPath = "assets/graphics/statics/lose_menu.jpg";
    Image loseMenuImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(loseMenuImage, -5, 0, this);
  }

  @SuppressWarnings("CheckStyle")
  private void drawGuppy(Graphics graphics, Guppy guppy) {
    int level = guppy.getLevel();
    boolean hungry = guppy.getHungry();
    int progress = guppy.getProgress();
    State state = guppy.getState();
    String assetPath = "assets/graphics/sprites/guppy";

    // Check level
    switch (level) {
      case 1:
        assetPath += "/small";
        break;
      case 2:
        assetPath += "/medium";
        break;
      default:
        assetPath += "/large";
        break;
    }

    // Check state
    switch (state) {
      case movingLeft:
        assetPath += hungry ? "/move/hungry/left" : "/move/normal/left";

        break;
      case movingRight:
        assetPath += hungry ? "/move/hungry/right" : "/move/normal/right";

        break;
      case turningLeft:
        assetPath += hungry ? "/turn/hungry/left" : "/turn/normal/left";

        break;
      case turningRight:
        assetPath += hungry ? "/turn/hungry/right" : "/turn/normal/right";

        break;
      case eatingLeft:
        assetPath += hungry ? "/eat/hungry/left" : "/eat/normal/left";

        break;
      case eatingRight:
        assetPath += hungry ? "/eat/hungry/right" : "/eat/normal/right";

        break;
      case deadLeft:
        assetPath += "/dead/left";

        break;
      case deadRight:
        assetPath += "/dead/right";
        break;
    }

    // Draw asset
    assetPath += "/" + (progress + 1) + ".png";
    Image guppyImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(guppyImage, (int) guppy.getX(), (int) guppy.getY(), this);
  }

  @SuppressWarnings("CheckStyle")
  private void drawPiranha(Graphics graphics, Piranha piranha) {
    boolean hungry = piranha.getHungry();
    int progress = piranha.getProgress();
    State state = piranha.getState();
    String assetPath = "assets/graphics/sprites/piranha";

    // Check state
    switch (state) {
      case movingLeft:
        assetPath += hungry ? "/move/hungry/left" : "/move/normal/left";

        break;
      case movingRight:
        assetPath += hungry ? "/move/hungry/right" : "/move/normal/right";

        break;
      case turningLeft:
        assetPath += hungry ? "/turn/hungry/left" : "/turn/normal/left";

        break;
      case turningRight:
        assetPath += hungry ? "/turn/hungry/right" : "/turn/normal/right";

        break;
      case eatingLeft:
        assetPath += hungry ? "/eat/hungry/left" : "/eat/normal/left";

        break;
      case eatingRight:
        assetPath += hungry ? "/eat/hungry/right" : "/eat/normal/right";

        break;
      case deadLeft:
        assetPath += "/dead/left";

        break;
      case deadRight:
        assetPath += "/dead/right";
        break;
    }

    // Draw asset
    assetPath += "/" + (progress + 1) + ".png";
    Image piranhaImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(piranhaImage, (int) piranha.getX(), (int) piranha.getY(), this);
  }

  @SuppressWarnings("CheckStyle")
  private void drawSnail(Graphics graphics, Snail snail) {
    int progress = snail.getProgress();
    State state = snail.getState();
    String assetPath = "assets/graphics/sprites/snail";

    // Check state
    switch (state) {
      case movingLeft:
        assetPath += "/move/left";

        break;
      case movingRight:
        assetPath += "/move/right";

        break;
      case turningLeft:
        assetPath += "/turn/left";

        break;
      case turningRight:
        assetPath += "/turn/right";

        break;
      case stillRight:
        assetPath += "/move/right";

        break;
      case stillLeft:
        assetPath += "/move/left";
        break;
    }

    // Draw asset
    assetPath += "/" + (progress + 1) + ".png";
    Image snailImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(snailImage, (int) snail.getX(), (int) snail.getY(), this);
  }

  private void drawCoin(Graphics graphics, Coin coin) {
    boolean isGold = coin.getValue() > Constants.COIN_GOLD_THRESHOLD;
    int progress = coin.getProgress();
    String assetPath = "assets/graphics/sprites/coin";

    // Draw asset
    assetPath += isGold ? "/gold" : "/silver";
    assetPath += "/" + (progress + 1) + ".png";
    Image coinImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(coinImage, (int) coin.getX(), (int) coin.getY(), this);
  }

  private void drawPellet(Graphics graphics, Pellet pellet) {
    int progress = pellet.getProgress();
    String assetPath = "assets/graphics/sprites/pellet";

    // Draw asset
    assetPath += "/" + (progress + 1) + ".png";
    Image pelletImage = new ImageIcon(assetPath).getImage();
    graphics.drawImage(pelletImage, (int) pellet.getX(), (int) pellet.getY(), this);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    playMusic();

    switch (gameState) {
      case uninitialized:
        // Check if start button clicked
        if (areaClicked(Constants.MAIN_START_BUTTON_X_START, Constants.MAIN_START_BUTTON_X_END,
            Constants.MAIN_START_BUTTON_Y_START, Constants.MAIN_START_BUTTON_Y_END)) {
          gameState = GameState.running;
          initGameState();
        }

        break;
      case won:
        // Check if start button clicked
        if (areaClicked(Constants.WIN_START_BUTTON_X_START, Constants.WIN_START_BUTTON_X_END,
            Constants.WIN_START_BUTTON_Y_START, Constants.WIN_START_BUTTON_Y_END)) {
          gameState = GameState.running;
          initGameState();
        }

        break;
      case lost:
        // Check if start button clicked
        if (areaClicked(Constants.LOSE_START_BUTTON_X_START, Constants.LOSE_START_BUTTON_X_END,
            Constants.LOSE_START_BUTTON_Y_START, Constants.LOSE_START_BUTTON_Y_END)) {
          gameState = GameState.running;
          initGameState();
        }

        break;
      default:
        // Update game state
        updateGameState();

        // Handle buy guppy button click
        if (areaClicked(Constants.BUY_GUPPY_BUTTON_X_START, Constants.BUY_GUPPY_BUTTON_X_END,
            Constants.BUY_GUPPY_BUTTON_Y_START, Constants.BUY_GUPPY_BUTTON_Y_END)) {
          if (coinCount >= Constants.GUPPY_PRICE) {
            aquarium.createGuppy();
            coinCount -= Constants.GUPPY_PRICE;
          }
        }

        // Handle buy piranha button click
        if (areaClicked(Constants.BUY_PIRANHA_BUTTON_X_START, Constants.BUY_PIRANHA_BUTTON_X_END,
            Constants.BUY_PIRANHA_BUTTON_Y_START, Constants.BUY_PIRANHA_BUTTON_Y_END)) {
          if (coinCount >= Constants.PIRANHA_PRICE) {
            aquarium.createPiranha();
            coinCount -= Constants.PIRANHA_PRICE;
          }
        }

        // Handle buy snail button click
        if (areaClicked(Constants.BUY_SNAIL_BUTTON_X_START, Constants.BUY_SNAIL_BUTTON_X_END,
            Constants.BUY_SNAIL_BUTTON_Y_START, Constants.BUY_SNAIL_BUTTON_Y_END)) {
          if (coinCount >= Constants.SNAIL_PRICE) {
            aquarium.createSnail();
            coinCount -= Constants.SNAIL_PRICE;
          }
        }

        // Handle buy egg button click
        if (areaClicked(Constants.BUY_EGG_BUTTON_X_START, Constants.BUY_EGG_BUTTON_X_END,
            Constants.BUY_EGG_BUTTON_Y_START, Constants.BUY_EGG_BUTTON_Y_END)) {
          if (coinCount >= Constants.EGG_PRICE) {
            eggCount += 1;
            coinCount -= Constants.EGG_PRICE;
          }
        }

        // Handle coin taking click
        Iterator<Coin> coinIterator = contentCoin.iterator();
        while (coinIterator.hasNext()) {
          Coin coin = coinIterator.next();
          if (areaClicked((int) coin.getX(),
              (int) coin.getX() + Constants.COIN_CLICK_AREA_WIDTH,
              (int) coin.getY(),
              (int) coin.getY() + Constants.COIN_CLICK_AREA_HEIGHT)) {
            coinCount += coin.getValue();
            aquarium.deleteCoin(coin);
          }
        }

        // Handle pellet buying click
        if (areaClicked(Constants.GAME_SCREEN_LEFT_PADDING,
            Constants.GRAPHICS_WIN_WIDTH - Constants.GAME_SCREEN_RIGHT_PADDING,
            Constants.GAME_SCREEN_TOP_PADDING,
            Constants.GRAPHICS_WIN_HEIGHT - Constants.GAME_SCREEN_BOTTOM_PADDING)) {
          if (coinCount >= Constants.PELLET_PRICE) {
            aquarium.createPellet(mouseClickX, mouseClickY);
            coinCount -= Constants.PELLET_PRICE;
          }
        }
        break;
    }

    // Render frame
    repaint();
  }

  private void playMusic() {
    if (gameMusicState == null || gameMusicState != gameState) {
      gameMusicState = gameState;

      if (musicPlayed) {
        musicClip.stop();
        musicPlayed = false;
      }
      try {
        File soundFile;
        switch (gameMusicState) {
          case uninitialized: {
            soundFile = new File("assets/sounds/main_menu_bgm.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.start();
            break;
          }
          case won: {
            soundFile = new File("assets/sounds/win_menu_bgm.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.start();
            break;
          }
          case lost: {
            soundFile = new File("assets/sounds/lose_menu_bgm.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.start();
            break;
          }
          default: {
            soundFile = new File("assets/sounds/game_bgm.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.start();
            break;
          }
        }
        musicPlayed = true;
      } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean areaClicked(int startX, int endX, int startY, int endY) {

    if (mouseClickX >= startX && mouseClickX <= endX && mouseClickY >= startY && mouseClickY <= endY
        && mouseClickValid) {
      mouseClickValid = false;
      return true;
    } else {
      return false;
    }
  }

  private class KAdapter extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent e) {

      int key = e.getKeyCode();

      if ((key == KeyEvent.VK_G)) {
        aquarium.createGuppy();
      }

      if ((key == KeyEvent.VK_P)) {
        aquarium.createPiranha();
      }

      if ((key == KeyEvent.VK_C)) {
        coinCount += 10;
      }
    }
  }

  private class MAdapter extends MouseAdapter {

    @Override
    public void mousePressed(MouseEvent e) {

      mouseClickX = e.getX() - 20;
      mouseClickY = e.getY() - 20;
      mouseClickValid = true;
    }
  }
}
