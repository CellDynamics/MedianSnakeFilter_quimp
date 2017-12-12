package quimp.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.PropertyReader;
import com.github.celldynamics.quimp.ViewUpdater;
import com.github.celldynamics.quimp.plugin.IQuimpPluginSynchro;
import com.github.celldynamics.quimp.plugin.ParamList;
import com.github.celldynamics.quimp.plugin.QuimpPluginException;
import com.github.celldynamics.quimp.plugin.snakes.IQuimpBOAPoint2dFilter;
import com.github.celldynamics.quimp.plugin.utils.IPadArray;
import com.github.celldynamics.quimp.plugin.utils.QWindowBuilder;
import com.github.celldynamics.quimp.plugin.utils.QuimpDataConverter;

/**
 * Interpolation of points (X,Y) by means of running median method.
 * 
 * @author p.baniukiewicz
 * @date 10 Dec 2017
 *
 */
public class MedianSnakeFilter_ extends QWindowBuilder implements IQuimpBOAPoint2dFilter, IPadArray,
        IQuimpPluginSynchro, ChangeListener, ActionListener {

  static final Logger LOGGER = LoggerFactory.getLogger(MedianSnakeFilter_.class.getName());
  private QuimpDataConverter xyData; //!< input List converted to separate X and Y arrays
  private int window; //!< size of processing window
  private ParamList uiDefinition; //!< Definition of UI
  protected ViewUpdater qcontext; //!< remember QuimP context to recalculate and update its view 

  /**
   * Create running median filter.
   * 
   * <p>All default parameters should be declared here. Non-default are passed by
   * setPluginConfig(ParamList)
   */
  public MedianSnakeFilter_() {
    LOGGER.trace("Entering constructor");
    this.window = 3; // default value
    LOGGER.debug("Set default parameter: window=" + window);
    // create UI using QWindowBuilder
    uiDefinition = new ParamList(); // will hold ui definitions
    // configure window, names of UI elements are also names of variables
    // exported/imported by set/getPluginConfig
    uiDefinition.put("name", "MedianFilter"); // name of win
    uiDefinition.put("window", "spinner: 1: 21: 2:" + Integer.toString(window));
    uiDefinition.put("help", "Window must be uneven. Set 1 to switch filter off.");
    buildWindow(uiDefinition); // construct ui (not shown yet)
  }

  /**
   * Attach data to process.
   * 
   * <p>Data are as list of vectors defining points of polygon. Passed points should be sorted
   * according to a clockwise or anti-clockwise direction
   * 
   * @param data Polygon points
   * @see com.github.celldynamics.quimp.plugin.snakes.IQuimpBOAPoint2dFilter#attachData(List)
   */
  @Override
  public void attachData(List<Point2d> data) {
    LOGGER.trace("Entering attachData");
    if (data == null) {
      return;
    }
    xyData = new QuimpDataConverter(data); // helper for converting from List<Vector2d> to X[], Y[]
  }

  /**
   * Perform interpolation of data by a moving median filter with given window
   * 
   * <p>By default uses CIRCULAR padding. The window must be uneven, positive and shorter than
   * data vector. X and Y coordinates of points are smoothed separately.
   * 
   * @return Filtered points as list of Vector2d objects
   * @throws QuimpPluginException when: - window is even - window is longer or equal processed
   *         data - window is negative
   */
  @Override
  public List<Point2d> runPlugin() throws QuimpPluginException {
    // collect actual parameters from UI
    window = getIntegerFromUI("window");
    LOGGER.debug(String.format("Run plugin with params: window %d", window));

    if (window % 2 == 0) {
      throw new QuimpPluginException("Input argument must be uneven");
    }
    if (window >= xyData.size()) {
      throw new QuimpPluginException("Processing window to long");
    }
    if (window < 0) {
      throw new QuimpPluginException("Processing window is negative");
    }

    // do filtering
    int cp = window / 2; // left and right range of window
    List<Point2d> out = new ArrayList<Point2d>();
    int indexTmp; // temporary index after padding
    double[] xs = new double[window]; // window point
    double[] ys = new double[window];
    int l = 0;

    for (int c = 0; c < xyData.size(); c++) { // for every point in data
      l = 0;
      for (int cc = c - cp; cc <= c + cp; cc++) { // collect points in range c-2 c-1 c-0 c+1 c+2
        indexTmp = IPadArray.getIndex(xyData.size(), cc, IPadArray.CIRCULARPAD);
        xs[l] = xyData.getX()[indexTmp];
        ys[l] = xyData.getY()[indexTmp];
        l++;
      }
      // get median
      Arrays.sort(xs);
      Arrays.sort(ys);
      out.add(new Point2d(xs[cp], ys[cp]));
    }
    return out;
  }

  /**
   * This method should return a flag word that specifies the filters capabilities.
   * 
   * @return Configuration codes
   * @see com.github.celldynamics.quimp.plugin.IQuimpCorePlugin
   * @see com.github.celldynamics.quimp.plugin.IQuimpPlugin#setup()
   */
  @Override
  public int setup() {
    LOGGER.trace("Entering setup");
    return DOES_SNAKES;
  }

  /**
   * Configure plugin and overrides default values.
   * 
   * <p>It is called by plugin user to pass configuration to plugin.
   * 
   * <p>Supported keys: <i>window</i> - size of window
   * 
   * @param par configuration as pairs [key,val]. Keys are defined by plugin creator and plugin
   *        user do not modify them.
   * @throws QuimpPluginException on wrong parameters list or wrong parameter conversion
   * @see com.github.celldynamics.quimp.plugin.IQuimpPlugin#setPluginConfig(ParamList)
   */
  @Override
  public void setPluginConfig(final ParamList par) throws QuimpPluginException {
    try {
      LOGGER.debug("Set params: " + par);
      setValues(par); // populate loaded values to UI
      window = par.getIntValue("window");
    } catch (Exception e) {
      // we should never hit this exception as parameters are not touched
      // by caller they are only passed to configuration saver and
      // restored from it
      throw new QuimpPluginException("Wrong input argument-> " + e.getMessage(), e);
    }
  }

  /**
   * Transfer plugin configuration to QuimP.
   * 
   * <p>Only parameters mapped to UI by QWindowBuilder are supported directly by getValues() Any
   * other parameters created outside QWindowBuilder should be added here manually.
   */
  @Override
  public ParamList getPluginConfig() {
    return getValues();
  }

  @Override
  public int showUi(boolean val) {
    LOGGER.debug("Got message to show UI");
    toggleWindow(val);
    return 0;
  }

  @Override
  public String getVersion() {
    String trimmedClassName = getClass().getSimpleName();
    trimmedClassName = trimmedClassName.substring(0, trimmedClassName.length() - 1); // no _
    // _ at the end of class does not appears in final jar name, we need it to
    // distinguish between plugins
    return PropertyReader.readProperty(getClass(), trimmedClassName,
            "quimp/plugin/plugin.properties", "internalVersion");
  }

  @Override
  public void attachContext(ViewUpdater b) {
    qcontext = b;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Object b = e.getSource();
    if (b == applyB || e.getActionCommand().equals("apply")) { // pressed apply
      qcontext.updateView();
    }
  }

  @Override
  public void stateChanged(ChangeEvent ce) {
    Object source = ce.getSource();
    JSpinner s = (JSpinner) ui.get("window"); // get ui element
    if (source == s) { // check if this event concerns it
      LOGGER.debug("Spinner used");
      if (((Double) s.getValue()).intValue() % 2 == 0) {
        s.setValue((Double) s.getValue() + 1);
      }
    }
    if (isWindowVisible() == true) {
      qcontext.updateView();
    }
  }

  @Override
  public void buildWindow(final ParamList def) {
    super.buildWindow(def); // window must be built first
    // attach listener to selected ui
    ((JSpinner) ui.get("window")).addChangeListener(this);
    applyB.addActionListener(this); // attach listener to apply button
  }

  @Override
  public String about() {
    return "Running median filter over outline\nAuthor: Piotr Baniukiewicz\n"
            + "mail: p.baniukiewicz@warwick.ac.uk";
  }
}
