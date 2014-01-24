import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RefineryUtilities;
import org.jfree.chart.util.ShapeUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.omg.CORBA.portable.OutputStream;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * A simple demonstration application showing how to create a line chart using data from an
 * {@link XYDataset}.
 *
 */
public class LineChart extends ApplicationFrame {

	private static final long serialVersionUID = -8547876522897390460L;
	
	public JFreeChart chart;
	private String title;
	private String xLabel;
	private String yLabel;
	
	private int width;
	private int height;

    public LineChart(final String x, final String y, XYDataset dataset) {

        super(y + " vs " + x);
        xLabel = x;
        yLabel = y;
    	title = new String(xLabel + " vs " + yLabel);
    	
    	width = 1200;
    	height = 800;

//        final dataset  = createDataset();
        chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(width, height));
        setContentPane(chartPanel);

    }
    
    private XYDataset createDataset() {
        
        final XYSeries series1 = new XYSeries("First");
        series1.add(1.0, 1.0);
        series1.add(2.0, 4.0);
        series1.add(3.0, 3.0);
        series1.add(4.0, 5.0);
        series1.add(5.0, 5.0);
        series1.add(6.0, 7.0);
        series1.add(7.0, 7.0);
        series1.add(8.0, 8.0);

        final XYSeries series2 = new XYSeries("Second");
        series2.add(1.0, 5.0);
        series2.add(2.0, 7.0);
        series2.add(3.0, 6.0);
        series2.add(4.0, 8.0);
        series2.add(5.0, 4.0);
        series2.add(6.0, 4.0);
        series2.add(7.0, 2.0);
        series2.add(8.0, 1.0);

        final XYSeries series3 = new XYSeries("Third");
        series3.add(3.0, 4.0);
        series3.add(4.0, 3.0);
        series3.add(5.0, 2.0);
        series3.add(6.0, 3.0);
        series3.add(7.0, 6.0);
        series3.add(8.0, 3.0);
        series3.add(9.0, 4.0);
        series3.add(10.0, 3.0);

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
                
        return dataset;
        
    }
    
    private JFreeChart createChart(final XYDataset dataset) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
        	title,      // chart title
        	xLabel,                      // x axis label
        	yLabel,                      // y axis label
            dataset
        );

        chart.setBackgroundPaint(Color.white);

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
//        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        
        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickUnit(new NumberTickUnit(0.5));
        plot.setDomainAxis(xAxis);
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShape(1, ShapeUtilities.createDiamond(5));

        // All lines are black
        int seriesCount = plot.getSeriesCount();
        for (int i = 0; i < seriesCount; i++) {
            renderer.setSeriesPaint(i, Color.black);
        }
//        renderer.setSeriesLinesVisible(0, false);
//        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
                
        return chart;
    }
    
    public void saveChartAsPNG(File file) {
    	try {
			ChartUtilities.saveChartAsPNG(file, chart, width, height);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
//    public void saveChartAsSVG(File file) {
//        // Get a DOMImplementation and create an XML document
//        DOMImplementation domImpl =
//            GenericDOMImplementation.getDOMImplementation();
//        Document document = domImpl.createDocument(null, "svg", null);
//
//        // Create an instance of the SVG Generator
//        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
//
//        // draw the chart in the SVG generator
//        chart.draw(svgGenerator, bounds);
//
//        // Write svg file
//        OutputStream outputStream = new FileOutputStream(svgFile);
//        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
//        svgGenerator.stream(out, true /* use css */);						
//        outputStream.flush();
//        outputStream.close();
//    }
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {

//		String userHomeFolder = System.getProperty("user.home");
//		File simulationsFolder = new File(userHomeFolder, "Desktop/Simulations");
//		simulationsFolder.mkdirs();
//		File graphFolder = new File(simulationsFolder, "graphs");
//		graphFolder.mkdirs();
//		File graphFile = new File(graphFolder, "graph.png");

//        final LineChart chart = new LineChart("Handled Rate", "Speed of Events");
//        chart.saveChart(graphFile);
//        chart.pack();
//        RefineryUtilities.centerFrameOnScreen(chart);
//        chart.setVisible(true);
    }
}