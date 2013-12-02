import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class GraphPanel extends JPanel implements Runnable {
	// Frame rate
	private final static int fps = 60;
	private final static int skipTicks = 1000 / fps;
	private long lastFrameTime;

	// Sizes and fonts
	public final static int padding = 80;
	public final static int vertexDiameter = 20;
	public final static int edgeLength = 120;
	public final static int eventSize = vertexDiameter/3;
	public final static int visibleEdgeLength = 120 - vertexDiameter - eventSize;
	public final static int edgeStringPad = edgeLength/40;
	public final static int agentImageWidth = vertexDiameter * 3;
	public final static Font regularFont = new Font("Verdana", Font.PLAIN, 12);
	public final static Font agentFont = new Font("Verdana", Font.BOLD, 16);
	public final static Font edgeFont = new Font("Verdana", Font.BOLD, 12);
	
	// Thread properties
	private Thread thread;
	private boolean running = false;
	
	private Simulation simulation;
	private BufferedImage agentImage;
	public int width;
	public int height;
	
    public GraphPanel(Simulation simulation){
    	this.simulation = simulation;
    	
    	// Load agent image
    	try {
			agentImage = ImageIO.read(new File("resources/agentTurtle.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	width = (simulation.graph.width - 1) * edgeLength + 2*padding;
    	height = (simulation.graph.height - 1) * edgeLength + 2*padding;
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setVisible(true);
        start();
   }
    
   public void paintComponent(Graphics g){
	   // Clear screen
	   g.setColor(Color.WHITE);
	   g.fillRect(0, 0, width, height);

	   // Center the graph
	   g.translate(padding, padding);

	   // Draw edges
	   for (EventEdge edge : simulation.graph.edges) {
		   drawEdge((Graphics2D)g, edge);
	   }
	   
	   // Draw vertices
	   for (Vertex vertex : simulation.graph.vertices) {
		   drawVertex((Graphics2D)g, vertex);
	   }
	   
	   // Draw the agents
	   for (Agent agent : simulation.agents) {
		   drawAgent((Graphics2D)g, agent);
	   }
	   
	   // Draw statistics/info
	   drawStats(g);
   }
   
   public void drawStats(Graphics g) {
	   DecimalFormat df = new DecimalFormat("#.##");
	   String averageDelayNumber = df.format(simulation.getAverageDelay()/1000); // delay in seconds
	   String averageDelay = "Average Delay: " + averageDelayNumber + " seconds";
	   g.drawString(averageDelay,edgeLength*2,-edgeLength/2);
	   String eventsGenerated = "Events Generated: " + new Integer(simulation.eventsGenerated).toString();
	   g.drawString(eventsGenerated,edgeLength/12,-edgeLength/2);
	   String deadEvents = "Dead Events: " + new Integer(simulation.getDeadEventCount()).toString();
	   g.drawString(deadEvents,edgeLength/12,-edgeLength/4);
   }
   
   public void drawEdge(Graphics2D g, EventEdge edge) {
	   Vertex vertexA = edge.vertex1;
	   Vertex vertexB = edge.vertex2;
	   Point pointA = new Point(vertexA.y*edgeLength,vertexA.x*edgeLength);
	   Point pointB = new Point(vertexB.y*edgeLength,vertexB.x*edgeLength);

	   g.setColor(Color.black);
//	   g.setStroke(new BasicStroke(edge.getEvents().size()));
	   g.setStroke(new BasicStroke(edge.getPriority()/5));
	   g.drawLine(pointA.x, pointA.y, pointB.x, pointB.y);
	   g.setFont(edgeFont);
	   g.setColor(Color.red);
	   g.drawString(new Integer(edge.getPriority()).toString(), (pointB.x + pointA.x)/2 + edgeStringPad, (pointB.y + pointA.y)/2 - edgeStringPad);

	   // Event beads
//	   int events = edge.getEvents().size();
//	   float spaceSize = (float)visibleEdgeLength / (float)events;
//	   for (int i = 0; i < visibleEdgeLength; i+=spaceSize) {
//	       g.fillOval(((pointA.x - pointB.x) + i), ((pointA.y - pointB.y) + i), eventSize, eventSize);
//	   }
   }
   
   public void drawVertex(Graphics2D g, Vertex vertex) {
	   Point point = new Point(vertex.y*edgeLength, vertex.x*edgeLength);

	   point.x = point.x - (vertexDiameter/2);
	   point.y = point.y - (vertexDiameter/2);
	   g.setColor(Color.black);
       g.fillOval(point.x, point.y, vertexDiameter, vertexDiameter);
	   g.setColor(Color.white);
	   
	   // Center txt label
//	   int stringWidth = (int)g.getFontMetrics().getStringBounds(vertex.name, g).getWidth();  
//	   int stringHeight = (int)g.getFontMetrics().getStringBounds(vertex.name, g).getHeight(); 
//	   point.x = point.x + (vertexDiameter/2 - stringWidth/2);
//	   point.y = point.y + (vertexDiameter/2 + stringHeight/2);
//	   g.setFont(regularFont);
//	   g.drawString(vertex.name, point.x, point.y);
   }
   
   public void drawAgent(Graphics2D g, Agent agent) {
	   Vertex vertex = agent.lastLocation;
	   Point point = new Point(vertex.y*edgeLength, vertex.x*edgeLength);
	   point.x -= (agentImageWidth/2);
	   point.y -= (agentImageWidth/2);

	   Vertex nextVertex = agent.movingToLocation;
	   Point currentPoint = new Point(nextVertex.y*edgeLength, nextVertex.x*edgeLength);
	   currentPoint.x -= (agentImageWidth/2);
	   currentPoint.y -= (agentImageWidth/2);
		   
	   long totalTime = agent.endTime - agent.startTime;
	   long currentTime = System.currentTimeMillis();
	   double percentComplete = (1 - ((double)(agent.endTime - currentTime) / (double)totalTime));

	   // Cannot be more than 100% complete
	   percentComplete = Math.min(percentComplete, 1.0);
		   
	   point.x += (currentPoint.x - point.x) * percentComplete;
	   point.y += (currentPoint.y - point.y) * percentComplete;
	   
	   // Draw image and labels
	   g.drawImage(agentImage, point.x, point.y, agentImageWidth, agentImageWidth, null);
	   g.setColor(Color.black);
	   g.setFont(agentFont);
	   g.drawString(agent.name, point.x + agentImageWidth/2, point.y + agentImageWidth/2);
	   g.setFont(regularFont);
	   g.drawString(new Integer(agent.totalPriorityCollected).toString(), point.x + agentImageWidth/2, point.y +agentImageWidth);
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
