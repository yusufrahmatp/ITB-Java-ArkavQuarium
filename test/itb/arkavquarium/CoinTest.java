package itb.arkavquarium;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoinTest {
  // Stub Aquarium
  private Aquarium a;

  @Before
  public void setUp() {
    a = new Aquarium(0, 0, 100, 100);
    Snail s1 = a.getContentSnail().iterator().next();
    a.getContentSnail().remove(s1);
  }

  /*
   This is a test just for the coin mechanics.
   For a test where the pellet gets consumed by a Snail, see Snail test.
  */

  @Test
  public void fallTest() {
    Coin c = new Coin(50, 50, 50, a);
    a.getContentCoin().add(c);
    a.updateState(5.0);
    assertTrue("Coin is not falling", c.getY() > 50);
  }

  @Test
  public void xTest() {
    Coin c = new Coin(50, 50, 50, a);
    a.getContentCoin().add(c);
    a.updateState(5.0);
    assertEquals("Coin's abscissa changed", 50, c.getX(), 0.01);
  }

  @Test
  public void bottomTest() {
    Coin c = new Coin(50, 99, 50, a);
    a.getContentCoin().add(c);
    a.updateState(8 + 2); // less than coin deletion interval
    assertEquals("Coin falls through the aquarium",  c.getY(), a.getYMax(), 1);
  }

  @Test
  public void deletionTest() {
    Coin c = new Coin(50, a.getYMax(), 50, a);

    /* Delete the first guppy */
    Guppy g = a.getContentGuppy().iterator().next();
    a.getContentGuppy().remove(g);

    a.getContentCoin().add(c);
    a.updateState(Constants.COIN_DELETION_INTERVAL  + 1); // more than coin deletion interval
    assertTrue("Coin goes divine", a.getContentCoin().isEmpty());
  }
}