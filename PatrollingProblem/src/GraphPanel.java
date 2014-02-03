import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class GraphPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = -5788856317436681430L;

	// Frame rate
	private final static int fps = 60;
	private final static int skipTicks = 1000 / fps;
	private long lastFrameTime;

	// Sizes and fonts
	public final int padding = 80;
	public final int vertexDiameter = 20;
	public final int edgeLength = 120;
	public final int eventSize = vertexDiameter/3;
	public final int visibleEdgeLength = edgeLength - vertexDiameter - eventSize;
	public final int beadthPlacementLength = visibleEdgeLength * 5/6;
	public final int edgeStringPad = edgeLength/40;
	public final int agentImageWidth = vertexDiameter * 2;
	public final int agentImageHeight = agentImageWidth * 3/2;
	public final Font regularFont = new Font("Verdana", Font.PLAIN, 12);
	public final Font agentFont = new Font("Verdana", Font.BOLD, 16);
	public final Font edgeFont = new Font("Verdana", Font.PLAIN, 12);
	public final Font edgeFontBold = new Font("Verdana", Font.BOLD, 12);
	
	// Thread properties
	private Thread thread;
	private boolean running = false;
	
	private Simulation simulation;
	private BufferedImage agentFront;
	private BufferedImage agentBack;
	private BufferedImage agentLeft;
	private BufferedImage agentRight;
	public int width;
	public int height;
	
	private Date now;
	
    public GraphPanel(Simulation simulation){
    	this.simulation = simulation;
    	
    	// Load agent image
    	try {
    		agentFront = ImageIO.read(new File("resources/robotFront.png"));
    		agentBack = ImageIO.read(new File("resources/robotBack.png"));
    		agentLeft = ImageIO.read(new File("resources/robotLeft.png"));
    		agentRight = ImageIO.read(new File("resources/robotRight.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	width = (simulation.graph.width - 1) * edgeLength + 2 * padding;
    	height = (simulation.graph.height - 1) * edgeLength + 2 * padding;
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setVisible(true);
        start();
   }
    
   public void paintComponent(Graphics g){
	   
	   Graphics2D graphics2D = (Graphics2D) g;
	   
	   // Now
	   now = new Date();
	   
	   // Turn on anti-aliasing
	   graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	   // Clear screen
	   g.setColor(Color.WHITE);
	   g.fillRect(0, 0, width, height);

	   // Center the graph
	   g.translate(padding, padding);

	   // Draw edges
	   for (EventEdge edge : simulation.graph.edges) {
		   drawEdge(graphics2D, edge);
	   }
	   
	   // Draw vertices
	   for (Vertex vertex : simulation.graph.vertices) {
		   drawVertex(graphics2D, vertex);
	   }
	   
	   // Draw the agents
	   for (Agent agent : simulation.agents) {
		   drawAgent(graphics2D, agent);
	   }
	   
	   // Draw statistics/info
	   drawStats(graphics2D);
	   
	   // Draw timing clock
	   drawClock(graphics2D);
   }
   
   private void drawStats(Graphics2D g) {
	   DecimalFormat df = new DecimalFormat("#,###.##");
	   String averageDelayNumber = df.format(simulation.getAverageDelay()); // delay in seconds
	   String averageDelay = "Average Delay: " + averageDelayNumber;
	   g.setColor(Color.black);
	   g.setFont(regularFont);

	   g.drawString(averageDelay,edgeLength*2,-edgeLength/2);
	   String eventsGenerated = "Events Generated: " + NumberFormat.getNumberInstance(Locale.US).format(simulation.eventsGenerated) + " / " + NumberFormat.getNumberInstance(Locale.US).format(simulation.totalEvents);
	   g.drawString(eventsGenerated,edgeLength/12,-edgeLength/2);
	   String deadEvents = "Dead Events: " + NumberFormat.getNumberInstance(Locale.US).format(simulation.getDeadEventCount());
	   g.drawString(deadEvents,edgeLength/12,-edgeLength/4);
   }
   
   private void drawEdge(Graphics2D g, EventEdge edge) {
	   Vertex vertexA = edge.vertex1;
	   Vertex vertexB = edge.vertex2;
	   Point pointA = new Point(vertexA.y*edgeLength,vertexA.x*edgeLength);
	   Point pointB = new Point(vertexB.y*edgeLength,vertexB.x*edgeLength);
	   
	   // For getting line direction
	   double pointDistance = Math.sqrt(Math.pow(pointB.y - pointA.y,2) + Math.pow(pointB.x - pointA.x,2));
	   double heightPercentage = (pointB.y - pointA.y) / pointDistance;
	   double widthPercentage = (pointB.x - pointA.x) / pointDistance;

	   g.setColor(Color.black);
//	   g.setStroke(new BasicStroke(edge.getEvents().size()));
	   g.setStroke(new BasicStroke(edge.getPriority(now.getTime())/5 + 1));
	   g.drawLine(pointA.x, pointA.y, pointB.x, pointB.y);
	   
	   int priority = edge.getPriority(now.getTime());
	   Color redColor;
	   Font font;
	   if (priority == 0) {
		   font = edgeFont;
		   redColor = new Color(255,0,0,127);
	   } else {
		   font = edgeFontBold;
		   redColor = new Color(255,0,0,255);
	   }
	   g.setFont(font);
	   g.setColor(redColor);
	   
	   String priorityString = new Integer(edge.getPriority(now.getTime())).toString();
	   FontMetrics metrics = g.getFontMetrics(font);
	   int stringWidth = metrics.stringWidth(priorityString);
	   int stringHeight = metrics.getHeight();
	   g.drawString(priorityString, (pointB.x + pointA.x)/2 - stringWidth/2 + (int) (heightPercentage * stringWidth), (pointB.y + pointA.y)/2 - (int) (stringHeight/2 * widthPercentage));

	   // Event beads
	   int events = edge.getEvents().size();
	   int spacing = beadthPlacementLength / (events + 1);
	   
	   int beadDistance = visibleEdgeLength - beadthPlacementLength + spacing;
	   for (int i = 0; i < events; i++) {
		   int beadX = (pointB.x - (int) (beadDistance * widthPercentage));
		   int beadY = (pointB.y - (int) (beadDistance * heightPercentage));
		   beadX -= (eventSize / 2);
		   beadY -= (eventSize / 2);
	       g.fillOval(beadX, beadY, eventSize, eventSize);
	       beadDistance += spacing;
	   }
   }
   
   private void drawVertex(Graphics2D g, Vertex vertex) {
	   Point point = new Point(vertex.y*edgeLength, vertex.x*edgeLength);

	   point.x = point.x - (vertexDiameter/2);
	   point.y = point.y - (vertexDiameter/2);
	   g.setColor(Color.black);
       g.fillOval(point.x, point.y, vertexDiameter, vertexDiameter);
	   g.setColor(Color.white);
	   
	   // Center txt label
	   int stringWidth = (int)g.getFontMetrics().getStringBounds(vertex.name, g).getWidth();  
	   int stringHeight = (int)g.getFontMetrics().getStringBounds(vertex.name, g).getHeight(); 
	   point.x = point.x + (vertexDiameter/2 - stringWidth/2);
	   point.y = point.y + (vertexDiameter/2 + stringHeight/2);
	   g.setFont(regularFont);
	   g.drawString(vertex.name, point.x, point.y);
   }
   
   private void drawAgent(Graphics2D g, Agent agent) {
	   Vertex vertex = agent.lastLocation;
	   Point point = new Point(vertex.y*edgeLength, vertex.x*edgeLength);
	   point.x -= (agentImageWidth/2);
	   point.y -= (agentImageHeight/2);

	   Vertex nextVertex = agent.movingToLocation;
	   Point currentPoint = new Point(nextVertex.y*edgeLength, nextVertex.x*edgeLength);
	   currentPoint.x -= (agentImageWidth/2);
	   currentPoint.y -= (agentImageHeight/2);
		   
	   double totalTime = agent.endTime - agent.startTime;
	   long currentTime = System.currentTimeMillis();
	   double percentComplete;
	   if (totalTime == 0) {
		   percentComplete = 1.0;
	   } else {
		   percentComplete = (1 - ((double)(agent.endTime + simulation.startTime - currentTime) / totalTime));
	   }

	   // Cannot be more than 100% complete
	   percentComplete = Math.min(percentComplete, 1.0);
	   percentComplete = Math.max(percentComplete, 0);
		   
	   point.x += (currentPoint.x - point.x) * percentComplete;
	   point.y += (currentPoint.y - point.y) * percentComplete;
	   
	   // Different sides images of the agent
	   BufferedImage agentImage;
	   if (currentPoint.x > point.x) {
		   agentImage = agentRight;
	   } else if (currentPoint.x < point.x) {
		   agentImage = agentLeft;
	   } else if (currentPoint.y < point.y) {
		   agentImage = agentBack;
	   } else {
		   agentImage = agentFront;
	   }
	   
	   // Draw image and labels
	   g.drawImage(agentImage, point.x, point.y, agentImageWidth, agentImageHeight, null);
	   g.setColor(Color.black);
//	   g.setFont(agentFont);
//	   g.drawString(agent.name, point.x + agentImageWidth/2, point.y + agentImageHeight/2);
	   g.setFont(regularFont);
	   Color darkGreen = new Color(0,100,0);
	   g.setColor(darkGreen);
	   g.setFont(edgeFontBold);
	   g.drawString(new Integer(agent.totalPriorityCollected).toString(), point.x + agentImageWidth, point.y +agentImageHeight);
   }

   private void drawClock(Graphics2D g) {
	   
	   int clockRadius = 25;
	   int offset = 10;
	   g.setColor(Color.black);
	   
	   int x = width - padding*2 + offset;
	   int y = -clockRadius*2 - offset;
	   
	   int centerX = x + clockRadius;
	   int centerY = y + clockRadius;
	   
	   long timeEllapsed = System.currentTimeMillis() - (long) simulation.startTime;
	   double clockPercentage = (double) (timeEllapsed % Simulation.timeConstant) / Simulation.timeConstant;
	   int transparency = (int) ((clockPercentage - .5) * 256);
	   transparency = Math.abs(transparency) - 50;
	   transparency = Math.max(transparency, 0) * 2;
	   clockPercentage *= 2 * Math.PI;
	   clockPercentage -= Math.PI / 2;
//	   DecimalFormat df = new DecimalFormat("#.#####");
//	   String percentage = df.format(clockPercentage);
//	   System.out.println(percentage);
	   
	   int armX = (int) (centerX + Math.cos(clockPercentage) * clockRadius);
	   int armY = (int) (centerY + Math.sin(clockPercentage) * clockRadius);
	   
	   // Draw clock
	   Color transparentRed = new Color(255,0,0,transparency);
	   g.setColor(transparentRed);
	   g.fillOval(x, y, clockRadius * 2, clockRadius * 2);
	   g.setColor(Color.black);
	   g.setStroke(new BasicStroke(1));
	   g.drawOval(x, y, clockRadius * 2, clockRadius * 2);
	   g.drawLine(centerX, centerY - clockRadius, centerX, centerY - (clockRadius * 3 / 5));

	   // Draw arm
	   g.drawLine(centerX, centerY, armX, armY);
   }

   public synchronized void start() {
	   running = true;
	   thread = new Thread(this);
	   thread.start();
   }
   
   public synchronized void stop() {
   		running = false;
   		try {
   			thread.join();
   			System.out.println("Simulation has stopped.");
   		} catch (InterruptedException e) {
   			e.printStackTrace();
   		}
   }
   
	public void run() {
		lastFrameTime = System.currentTimeMillis();
		while (running) {
			
			// Only update do match the desired fps
			long currentTime = System.currentTimeMillis();
			if (currentTime > (lastFrameTime + skipTicks)) {
				repaint();
				lastFrameTime = currentTime;
			}
		}
	}

}
