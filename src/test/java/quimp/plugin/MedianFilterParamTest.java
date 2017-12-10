package quimp.plugin;

import static com.github.baniuk.ImageJTestSuite.dataaccess.ResourceLoader.loadResource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.baniuk.ImageJTestSuite.dataaccess.DataLoader;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.utils.test.RoiSaver;

/**
 * Test runner for Interpolate class using parameters.
 * 
 * <p>Test only getInterpolationMean method using its own parameters
 * 
 * <p>Use src/test/resources/Interpolate_Test_Analyzer.m for plotting results
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(Parameterized.class)
public class MedianFilterParamTest {
  private List<Point2d> testcase;
  private Integer window;
  private Path testfileName;
  static final Logger LOGGER = LoggerFactory.getLogger(MedianFilterParamTest.class.getName());

  /**
   * Parameterized constructor.
   * 
   * <p>Each parameter should be placed as an argument here Every time runner triggers, it will pass
   * the arguments from parameters we defined to this method
   * 
   * @param testFileName test file name
   * @param window averaging window size
   * @see DataLoader
   */
  public MedianFilterParamTest(String testFileName, Integer window) {
    this.testfileName = Paths.get(testFileName);
    this.window = window;
  }

  /**
   * Called after construction but before tests.
   * 
   * @throws Exception Exception
   */
  @Before
  public void setUp() throws Exception {
    String tf = testfileName.getFileName().toString();
    testcase = new DataLoader(loadResource(getClass().getClassLoader(), tf).toString())
            .getListofPoints();
  }

  /**
   * Set of parameters for tests.
   * 
   * <p>see: QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating *.dat files
   * 
   * @return List of strings with paths to testfiles and smooth parameter
   * 
   */
  @Parameterized.Parameters
  public static Collection<Object[]> testFiles() {
    //!>
    return Arrays.asList(new Object[][] { 
      { "testData_75.dat", 1 },
      { "testData_75.dat", 3 },
      { "testData_75.dat", 5 },
      { "testData_75.dat", 9 },
      { "testData_75.dat", 15 },

      { "testData_125.dat", 1 },
      { "testData_125.dat", 3 },
      { "testData_125.dat", 5 },
      { "testData_125.dat", 9 },
      { "testData_125.dat", 15 },

      { "testData_137.dat", 1 },
      { "testData_137.dat", 3 },
      { "testData_137.dat", 5 },
      { "testData_137.dat", 9 },
      { "testData_137.dat", 15 },

      { "testData_1.dat", 1 },
      { "testData_1.dat", 3 },
      { "testData_1.dat", 5 },
      { "testData_1.dat", 9 },
      { "testData_1.dat", 15 }, });
    //!<
  }

  /**
   * Test of getInterpolationMean method.
   * 
   * <p>Pre: original images saved as test_roiSaver_
   * 
   * <p>Post:Save image test_getInterpolationMean_* in /tmp/
   * 
   * <p>See: QuimP-toolbox/algorithms/src/test/resources/ Interpolate_Test_Analyzer.m for plotting
   * results
   * 
   * <p>See QuimP-toolbox/Prototyping/59-Shape_filtering/main.m for creating *.dat files
   * 
   * @throws QuimpPluginException
   * 
   */
  @SuppressWarnings("serial")
  @Test
  public void test_getInterpolationMean() throws QuimpPluginException {
    List<Point2d> out;
    MedianSnakeFilter_ i = new MedianSnakeFilter_();
    i.attachData(testcase);
    i.setPluginConfig(new ParamList() {
      {
        put("window", String.valueOf(window));
      }
    });
    out = i.runPlugin();
    RoiSaver.saveRoi("/tmp/test_getInterpolationMean_" + testfileName.getFileName() + "_"
            + window.toString() + ".tif", out);
    LOGGER.debug("setUp: " + testcase.toString());
    if (out.size() < 100) {
      LOGGER.debug("testInterpolate: " + out.toString());
    }
  }

  /**
   * Simple test of RoiSaver class, create reference images without processing.
   * 
   * <p>Post: Save image /tmp/testroiSaver_*.tif
   */
  @Test
  @Ignore
  public void test_roiSaver() {
    RoiSaver.saveRoi(
            "/tmp/test_roiSaver_" + testfileName.getFileName() + "_" + window.toString() + ".tif",
            testcase);
  }

}
