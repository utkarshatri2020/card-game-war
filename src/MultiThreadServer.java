
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javax.swing.*;

public class MultiThreadServer extends JFrame implements Runnable {
	// Number a client
	private ArrayList<Player> players = new ArrayList<>();
	private int playerID = 1;
	private int winner = -1;

	private JLabel resultLabel;
	private String image1 = "1.png";
	private String image2 = "1.png";
	private ImagePanel card1Icon;
	private ImagePanel card2Icon;
	private JPanel cardPanel;
	private CyclicBarrier barrier;
	private ServerSocket serverSocket;

	public MultiThreadServer() {
		setSize(600, 400);
		setupCardPanels();
		createLabel();
		this.setTitle("War");
		this.barrier = new CyclicBarrier(2, new GameMaster());
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		try {
			// Create a server socket
			serverSocket = new ServerSocket(8000);
			while (true) {
				// Listen for a new connection request
				Socket socket = serverSocket.accept();
				if (players.size() < 2) {
					// Increment clientNo
					Player newPlayer = new Player(socket, playerID, 0);
					players.add(newPlayer);
					// Create and start a new thread for the connection
					new Thread(newPlayer).start();
					playerID++;
				} else {
					socket.sendUrgentData(-1);
					socket.close();
				}

			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	class GameMaster implements Runnable {
		public void run() {
			// play the game
			Timer timer = new Timer(100, new ActionListener() {
				private int counter;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (counter < 20) {
						counter++;
						players.get(0).cardValue = shuffle();
						String cardPath1 = Integer.toString(players.get(0).cardValue) + ".png";
						card1Icon.setImage(cardPath1);
						players.get(1).cardValue = shuffle();
						String cardPath2 = Integer.toString(players.get(1).cardValue) + ".png";
						card2Icon.setImage(cardPath2);
					} else {
						((Timer) e.getSource()).stop();
					}

				}
			});
			// determine winner
			timer.start();
			while(timer.isRunning());
			if (players.get(0).cardValue > players.get(1).cardValue) {
				winner = players.get(0).playerID;
			} else if (players.get(1).cardValue > players.get(0).cardValue) {
				winner = players.get(1).playerID;
			} else {
				winner = 0;
			}
			//update bottom label
			if (winner != 0) {				
				resultLabel.setText("Player " + winner + " won!");
			} else {
			resultLabel.setText("It's a tie!");
			}
		}
	}

	// Define the thread class for handling new connection
	class Player implements Runnable {
		private Socket socket; // A connected socket
		private int playerID;
		private DataInputStream inputFromClient;
		DataOutputStream outputToClient;
		private int cardValue;

		/** Construct a thread */
		public Player(Socket socket, int clientNum, int cardValue) {
			this.socket = socket;
			this.playerID = clientNum;
			this.cardValue = cardValue;
			// Create data input and output streams
			try {
				this.inputFromClient = new DataInputStream(this.socket.getInputStream());
				this.outputToClient = new DataOutputStream(this.socket.getOutputStream());
				// tell the user their player number
				outputToClient.writeDouble(clientNum);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/** Run a thread */
		public void run() {
			try {
				// wait for two players to connect
				while (true) {
					// Receive command from the client
					double command = inputFromClient.readDouble();
					if (command == 1) {
						// client is ready to start the game
						// wait for all players
						try {
							barrier.await();
							// send game result back
							if(winner == this.playerID) {
								outputToClient.writeDouble(1);						
							} else if(winner == 0){
								outputToClient.writeDouble(0);
							} else {
								outputToClient.writeDouble(-1);
							}
							outputToClient.flush();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (BrokenBarrierException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if(command == 2) {
						// client quit the game
						socket.close();
						players.remove(this);
						resultLabel.setText("Player " + playerID + " left the game\n Waiting for another player...");
						return;
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				players.remove(this);
			}
		}
	}

	public void setupCardPanels() {
		this.cardPanel = new JPanel();
		JPanel labelPanel = new JPanel();
		cardPanel.setLayout(new GridLayout(1, 2));
		labelPanel.setLayout(new GridLayout(1, 2));
		// card1
		this.card1Icon = new ImagePanel(this.image1);
		// card 2
		this.card2Icon = new ImagePanel(this.image2);

		JLabel jb1 = new JLabel();
		jb1.setText("Player 1: ");
		labelPanel.add(jb1);
		JLabel jb2 = new JLabel();
		jb2.setText("Player 2: ");
		labelPanel.add(jb2);
		cardPanel.add("Player 1: ",this.card1Icon);
		cardPanel.add("Player 2: ", this.card2Icon);
		add(labelPanel,BorderLayout.NORTH);
		add(cardPanel, BorderLayout.CENTER);
	}

	public void createLabel() {
		JPanel panel = new JPanel();
		resultLabel = new JLabel("This is a game of War! Press 'Ready' to start.\n Good luck...\n");
		panel.add(resultLabel);
		((FlowLayout) panel.getLayout()).setVgap(50);
		add(panel, BorderLayout.SOUTH);
	}

	public int shuffle() {
		Random r = new Random();
		int randomNumber = r.nextInt(13) + 1;
		return randomNumber;
	}

	/**
	 * The main method is only needed for the IDE with limited JavaFX support. Not
	 * needed for running from the command line.
	 */
	public static void main(String[] args) {
		MultiThreadServer mts = new MultiThreadServer();
		mts.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mts.setVisible(true);
	}
}