import javax.swing.BoxLayout;
import javax.swing.JFrame;

public class SimulationFrame extends JFrame {
	
    public SimulationFrame(Simulation simulation){
    	
    	GraphPanel graphPanel = new GraphPanel(simulation);
    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	
    	// Settings panel
//    	Dimension settingsPanelDimension = new Dimension(200, graphPanel.height);
//    	JPanel settingsPanel = new JPanel();
//    	settingsPanel.setBackground(Color.gray);
//    	settingsPanel.setSize(settingsPanelDimension);
    	
    	// Sizes
        setSize(graphPanel.width, graphPanel.height);
//        setSize(graphPanel.width + settingsPanelDimension.width, graphPanel.height);
    	setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
    	
    	// Frame properties
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
//        JButton pauseButton = new JButton("Play/Pause");
//        pauseButton.addActionListener(new ActionListener() { 
//        	  public void actionPerformed(ActionEvent e) { 
//        		  pauseSimulation();
//        	  } 
//        });
//        settingsPanel.add(pauseButton);
        
        // Layout
//    	add(settingsPanel);
    	add(graphPanel);
        setVisible(true);
   }
}
