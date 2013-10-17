import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;



public class GraphFrame extends JFrame {
	public final static int padding = 50;
	public final static int vertexDiameter = 20;
	public final static int edgeLength = 100;
	public final static int agentImageWidth = vertexDiameter * 3;
	public final static Font regularFont = new Font("Verdana", Font.PLAIN, 12);
	public final static Font agentFont = new Font("Verdana", Font.BOLD, 16);
	
	private Simulation simulation;
	private BufferedImage agentImage;
	
    public GraphFrame(Simulation simulation){
    	this.simulation = simulation;
    	
    	// Load agent image
    	try {
			agentImage = ImageIO.read(new File("resources/agentTurtle.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	int width = (simulation.graph.width - 1) * edgeLength + 2*padding;
    	int height = (simulation.graph.height - 1) * edgeLength + 2*padding;
        setSize(width,height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
   }

   public void paint(Graphics g){
	   g.translate(padding, padding);
	   Vertex[][] vertexArray = simulation.graph.vertexArray;

	   // Draw edges
	   for (int i = 0; i < simulation.graph.width; i++) {
		   for (int j = 0; j < simulation.graph.height; j++) {
			   Point edgePointA = new Point(i*edgeLength,j*edgeLength);
			   Point edgePointB = new Point((i+1)*edgeLength,(j)*edgeLength);
			   Point edgePointC = new Point((i)*edgeLength,(j+1)*edgeLength);
			   
			   if (i != (simulation.graph.width - 1)) {
				   drawEdge(g, edgePointA, edgePointB);
			   }
			   if (j != (simulation.graph.height - 1)) {
				   drawEdge(g, edgePointA, edgePointC);
			   }
		   }
	   }
	   
	   // Draw vertices
	   for (int i = 0; i < simulation.graph.width; i++) {
		   for (int j = 0; j < simulation.graph.height; j++) {
			   Vertex vertex = vertexArray[i][j];
			   // TODO: i & j are reversed here
			   Point vertexPoint = new Point(j*edgeLength,i*edgeLength);
			   
			   // Is an agent at this vertex?
			   if (vertex == simulation.agentA.currentLocation) {
				   drawAgent(g, simulation.agentA, vertexPoint);
			   } else {
				   drawVertex(g, vertex, vertexPoint);
			   }
		   }
	   }
   }
   
   public void drawEdge(Graphics g, Point pointA, Point pointB) {
	   g.setColor(Color.black);
	   g.drawLine(pointA.x, pointA.y, pointB.x, pointB.y);
//	   g.drawString(edge.name, (pointB.x - pointA.x)/2, pointA.y);
//	   g.setFont(regularFont);
   }
   
   public void drawVertex(Graphics g, Vertex vertex, Point point) {
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
   
   public void drawAgent(Graphics g,Agent agent, Point point) {
	   point.x = point.x - (agentImageWidth/2);
	   point.y = point.y - (agentImageWidth/2);
	   g.drawImage(agentImage, point.x, point.y, agentImageWidth, agentImageWidth, null);
	   g.setColor(Color.black);
	   g.setFont(agentFont);
	   g.drawString(agent.name, point.x + agentImageWidth/2, point.y + agentImageWidth/2);
   }
}
