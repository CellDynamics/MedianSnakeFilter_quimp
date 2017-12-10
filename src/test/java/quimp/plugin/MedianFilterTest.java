package quimp.plugin;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;

/**
 * Test runner for Interpolate class.
 * 
 * <p>Contain only simple non-parameterized tests
 * 
 * @author p.baniukiewicz
 *
 */
public class MedianFilterTest {

  private List<Point2d> testcase;
  static final Logger LOGGER = LoggerFactory.getLogger(MedianFilterTest.class.getName());

  /**
   * Called after construction but before tests.
   * 
   * @throws Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    testcase = new ArrayList<Point2d>();
    for (int i = 1; i <= 10; i++) {
      testcase.add(new Point2d(i, i));
    }
  }

  /**
   * tearDown.
   * 
   * @throws java.lang.Exception Exception
   */
  @After
  public void tearDown() throws Exception {
    testcase = null;
  }

  /**
   * Test of getInterpolationMean method.
   * 
   * <p>Pre: Vector of 1-10 elements
   * 
   * <p>Post: Running median for window 3
   * 
   * @throws com.github.celldynamics.quimp.plugin.QuimpPluginException QuimpPluginException
   */
  @SuppressWarnings("serial")
  @Test
  public void test_getInterpolationMean() throws QuimpPluginException {
    MedianSnakeFilter_ in = new MedianSnakeFilter_();
    in.attachData(testcase);
    Integer window = 3;
    in.setPluginConfig(new ParamList() {
      {
        put("Window", String.valueOf(window));
      }
    });
    double[] expected =
            { 2.0, 2.0000, 3.0000, 4.0000, 5.0000, 6.0000, 7.0000, 8.0000, 9.0000, 9.0 };

    List<Point2d> out;
    out = in.runPlugin();
    LOGGER.debug("org     : " + testcase.toString());
    LOGGER.debug("Window 3: " + out.toString());

    for (int i = 0; i < 10; i++) {
      assertEquals(expected[i], out.get(i).getX(), 1e-4);
      assertEquals(out.get(i).getX(), out.get(i).getY(), 1e-6);
    }

  }

}
