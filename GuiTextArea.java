import javax.swing.*;        

public class GuiTextArea {
    
    JTextArea area;

    //--------------------
    GuiTextArea(String title) {
	
	//Create and set up the window
	JFrame frame = new JFrame(title);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	area = new JTextArea(20, 40);
	area.setEditable(false);
	JScrollPane scrollPane = 
	    new JScrollPane(area,
			    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	
	frame.getContentPane().add(scrollPane);
	
	//Display the window.
	frame.pack();
	frame.setVisible(true);
    }

    //--------------------
    public void print(String s)   { 
	area.append(s);
        area.setCaretPosition(area.getDocument().getLength());
    }
    public void println(String s) { print(s+"\n"); }
    public void println()         { print("\n"); }

}
