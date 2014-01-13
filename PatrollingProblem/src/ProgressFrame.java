import javax.swing.*;

public class ProgressFrame extends JFrame {

	private static final long serialVersionUID = -5380129516416134631L;
	private int tasks;
	private JProgressBar progressBar;
	
	public ProgressFrame(int numberOfTasks) {
		this.tasks = numberOfTasks;
		
	    setSize(500, 100);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    setResizable(false);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setLocationRelativeTo(null);
	    
	    progressBar = new JProgressBar(0, numberOfTasks);
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    progressBar.setString(0 + "/" + numberOfTasks);
	    
	    add(progressBar);
	    setVisible(true);
	}
	
	public void updateValue(int progress) {
		progressBar.setValue(progress);
	    progressBar.setString(progress + "/" + tasks);
	}
}
