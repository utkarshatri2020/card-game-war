
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client extends JFrame  {
  // IO streams
  DataOutputStream toServer = null;
  DataInputStream fromServer = null;
  JTextField textField = null;
  JTextArea textArea = null;
  Socket socket = null;
  JButton openButton;
  JButton closeButton;
  JButton startGameButton;
  double playerNum;
  
  public Client() {
	  super("Client");
	  
	  this.setLayout(new BorderLayout());
	  
	  textArea = new JTextArea(30,30);
	  JScrollPane scrollPane = new JScrollPane(textArea);

	  JPanel topPanel = new JPanel(new GridLayout(2,1));
	  JPanel controlPanel = new JPanel();
	  
	  openButton = new JButton("Open Connection");
	  startGameButton = new JButton("Ready");
	  closeButton = new JButton("Close Connection");
	  topPanel.add(openButton);
	  controlPanel.add(startGameButton);
	  controlPanel.add(closeButton);
	  
	  topPanel.add(controlPanel);
	  this.add(topPanel, BorderLayout.NORTH);
	  this.add(scrollPane, BorderLayout.CENTER);
	  startGameButton.addActionListener(new StartGameListener());;
	  closeButton.addActionListener((e) -> { 
		  try { 
			  toServer.writeDouble(2);
			  toServer.flush();
			  socket.close(); 
			  textArea.append("connection closed\n");
			  } 
		  catch (Exception e1) {
			  System.err.println("error"); 
			  }
		  });
	  openButton.addActionListener(new OpenConnectionListener());
      setSize(400, 200);
  }
  
  class OpenConnectionListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		try {
			socket = new Socket("localhost", 8000);
			try {
			      // Create an input stream to receive data from the server
			      fromServer = new DataInputStream(socket.getInputStream());

			      // Create an output stream to send data to the server
			      toServer = new DataOutputStream(socket.getOutputStream());
			    }
		    catch (IOException ex) {
		      textArea.append(ex.toString() + '\n');
		    }
			playerNum = fromServer.readDouble();
			if(playerNum == -1) {
				textArea.append("connection rejected by server");
				socket.close();
			} else {
				textArea.append("connected\n");
				textArea.append("You are player " + playerNum + "\n");
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			textArea.append("connection Failure");
		}
	}
	  
  }
  
  class StartGameListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	  
	    
	    try {
	        // Send the start command to teh player
	        toServer.writeDouble(1);
	        toServer.flush();
	  
//	        // Get game result from the player
	        double result = fromServer.readDouble();
//	  
	        // Display game result
	        if (result == -1) {
	        	textArea.append("You lost!\n");
	        	
	        } 
	        else if (result == 1) {
	        	textArea.append("You won!\n");
	        }
	        else {
	        	textArea.append("It's a tie!\n");
	        }
	      }
	      catch (IOException ex) {
	        System.err.println(ex);
	      }
	    
	  

		
	}
  }
  
//  class WindowActionListener implements WindowListener{
//  }
	  

  public static void main(String[] args) {
    Client c = new Client();
    c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    c.setVisible(true);
  }
}
