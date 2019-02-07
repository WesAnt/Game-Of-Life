import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This is a coding assignment as part of the application for the BBC Software Engineering Graduate Scheme.
 * 
 *  This is based on Conway's Game of Life. 
 * 
 * Assumptions: Each cell should be selectable.
 * 				The grid will grow as the live cells move or populate outside of the initial area.
 * 				Each cell size will decrease in scale as viewing area gets bigger (smallest is 4 x 4 pixels).
 * 				The whole of the world should be viewable by means of navigational buttons.
 * 				There is an infinite amount of iterations.
 * 				Automation speed should be set at a moderate-fast pace (since a step through option is included).
 * 				The starting state should be randomly generated.
 * 
 * 
 * 
 * Instructions: 
 * 
 * 				Left mouse button - toggles live cell/dead cell.
 * 				Right mouse button - hold and move mouse up, down, left, right to navigate the grid.
 * 				 
 * 				
 * 
 * @author (Wesley White)
 * @version (5/2/2019) 
 */


/* Set up the main user interface and global variables */
public class GameOfLife extends JFrame {
	
	int offWorldWidth = 200;  // The starting width of the grid.
	int offWorldHeight = 200; // The starting height of the grid.
	
	int gridWidth = 20; // This is the grid width in cells.
	int gridHeight = 20; // This is the grid height in cells.
	
	
	int displayXStart; // The starting X coordinate of the part of the cells array to be displayed.
	int displayXEnd; // The ending X coordinate of the part of the cells array to be displayed.
	int displayYStart; // The starting Y coordinate of the part of the cells array to be displayed.
	int displayYEnd; // The ending Y coordinate of the part of the cells array to be displayed.
	int xPos = 0; // The X position relative to x=0 in the displayed area.
	int yPos = 0; // The y position relative to y=0 in the displayed area.
	
	int cellWidth = 800/gridWidth; // This is the cell width in pixels.
	int cellHeight = 800/gridHeight; // This is the cell height in pixels.
	int magnifierIndex = 5; // The index pointer of the magnifierIncrements array.
	int[] magnifierIncrements = new int[] {1, 2, 4, 5, 8, 10, 20, 40}; // Lookup table containing the proportional increments.
	boolean reset = false; 
	
	int cellSpacing = 1; // This is the spacing between each cell in pixels.
	int mouseX = 0; // Mouse X position.
	int mouseY = 0; // Mouse Y position.
	int oldX = 0; // The previous X position of the mouse before moving it.
	int oldY = 0; // The previous Y position of the mouse before moving it.
	boolean rightMousePressed = false; // Is right mouse button being held down.
	
	
	boolean automatedGame = false; // Has the Start/Stop button been pressed. 
	Timer timer = new Timer(); // The Timer object
	
	

	/* Buttons used for interaction */
	private JButton nextScenario;
	private JButton zoomOut;
	private JButton zoomIn;
	private JButton startGame;
	
	/* Navigational buttons */
	private JButton upButton;
	private JButton downButton;
	private JButton leftButton;
	private JButton rightButton;
	
	/* Text at bottom of screen */
	private JLabel authorText;
	
	
	Random rand = new Random(); // Generate a random number.
	
	boolean[][] cells = new boolean[offWorldHeight][offWorldWidth]; // The main cells array. If an element is TRUE then it contains a live cell, otherwise it doesn't.
	boolean[][] cellsCopy; // A copy of the cells array.
	
	
	
	public GameOfLife() {
		this.setTitle("Game of Life");
		this.setSize(806, 1000);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		
		startGame = new JButton("Start/Stop Game");
		nextScenario = new JButton("Next Scenario");
		zoomOut = new JButton("Zoom out");
		zoomIn  = new JButton("Zoom in");
		upButton = new JButton("Up");
		downButton = new JButton("Down");
		leftButton = new JButton("Left");
		rightButton = new JButton("Right");
		
		
		authorText = new JLabel("Written by Wesley White 2019");
		
		
		
		CalculateDisplay(); // Calculate which part of the cells array to display.
		
		
		/* Randomise initial grid state */
		for (int y = displayYStart+2; y < displayYEnd-2; y++) {
			for (int x = displayXStart+2; x < displayXEnd-2; x++) {
				if (rand.nextInt(100) < 15) {
					cells[y][x] = true;
				}
			}
		}
		
		Grid grid = new Grid();
		this.setContentPane(grid);
		
		grid.add(startGame);
		grid.add(nextScenario);
		grid.add(zoomOut);
		grid.add(zoomIn);
		grid.add(upButton);
		grid.add(downButton);
		grid.add(leftButton);
		grid.add(rightButton);
		grid.add(authorText);
	
		this.setVisible(true);
		
		startGame.addActionListener(new StartGame());
		nextScenario.addActionListener(new NextScenario());
		zoomOut.addActionListener(new ZoomOut());
		zoomIn.addActionListener(new ZoomIn());
		upButton.addActionListener( new UpButton());
		downButton.addActionListener( new DownButton());
		leftButton.addActionListener( new LeftButton());
		rightButton.addActionListener( new RightButton());
		
		startGame.setBounds(370,850,130,80);
		nextScenario.setBounds(520,850,100,80);
		zoomOut.setBounds(180,850,150,80);
		zoomIn.setBounds(10,850,150,80);
		upButton.setBounds(690,820, 60, 40);
		downButton.setBounds(690,900, 60, 40);
		leftButton.setBounds(645,860, 60, 40);
		rightButton.setBounds(735,860, 60, 40);
		
		authorText.setBounds(315,920,200,80);
		
		MoveMouse moveMouse = new MoveMouse();
		this.addMouseMotionListener(moveMouse);
		
		ClickMouse clickMouse = new ClickMouse();
		this.addMouseListener(clickMouse);
			
	}

	public class Grid extends JPanel {
		
		
		public void paintComponent(Graphics g) {
			
			g.setColor(Color.black);
			g.fillRect(0, 0, 800, 800);
			gridWidth = 800/cellWidth; // This is the cell width in pixels.
			gridHeight = 800/cellHeight; // This is the cell height in pixels.
			
			CalculateDisplay(); // Calculate which part of the cells array to display.
			
			
			/* Draw the grid and populate the cells */
			for (int y = 0; y < gridHeight; y++) {
				for (int x = 0; x < gridWidth; x++) {
					g.setColor(Color.DARK_GRAY);
					g.fillRect(cellSpacing+x*cellWidth, cellSpacing+y*cellHeight, cellWidth-cellSpacing, 
								cellHeight-cellSpacing);
					
					if (cells[displayYStart+y][displayXStart+x] == true) {
						g.setColor(Color.green);
						g.fillRect(cellSpacing+x*cellWidth, cellSpacing+y*cellHeight, cellWidth-cellSpacing, 
								cellHeight-cellSpacing);
					}	
				}
			}
			
			/* Display grid information */
			g.setColor(Color.white);
			g.drawString("Current World Size = " + Integer.toString(offWorldWidth) + " x " + 
						Integer.toString(offWorldHeight), 300,30);
			
			g.drawString("Viewing area = (" + Integer.toString(displayXStart) + ", " + 
					Integer.toString(displayYStart) + ") - (" + Integer.toString(displayXEnd) + ", " + Integer.toString(displayYEnd) + ")", 284,50);
		
			g.setColor(Color.black);
			g.drawString("Left mouse button toggles live cell / dead cell", 100, 820);
			g.drawString("Hold right mouse button and move mouse to navigate the area", 100, 840);
		}
	}
	
	
	
	/* Start automatic step through iterations */
	public class StartGame implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			/* Start automatic iterations */
			if (automatedGame == false) {
				
				TimerTask tt = new TimerTask() {
					

					@Override
					public void run() {
						CheckForNeighbours();
					};
					
				};
				timer = new Timer();
				timer.schedule(tt, 0, 50);
				automatedGame = true;
			}
			/* Stop automatic iterations */
			else if (automatedGame == true) {
				timer.cancel(); 
				automatedGame = false;
			}
			
			
		}
	}
	
	/* Step through Iterations one at a time */
	public class NextScenario implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
				CheckForNeighbours();
		}		
	}
	
	/* Make the display area bigger (also resets view position to central in the world) */
	public class ZoomOut implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			magnifierIndex--;
			xPos = 0;
			yPos = 0;
			
			if (magnifierIndex <= 0) {
					magnifierIndex = 0; // Viewing area is fully maximised. 
					reset = true;
					CalculateDisplay();
					
				}
				cellWidth = cellHeight = 4 * magnifierIncrements[magnifierIndex];	
		}
	}
	/* Make the display area smaller - each cell appears bigger */
	public class ZoomIn implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			magnifierIndex++;
			if (magnifierIndex > 7) {
					magnifierIndex = 7;
				} 
				cellWidth = cellHeight = 4 * magnifierIncrements[magnifierIndex];
		
		}		
	}
	
	/* Navigation buttons handlers */
	
	/* Move grid up */
	public class UpButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			yPos-=4;
			if (displayYStart <= 4) {
				yPos+=4;	
			}
		}
	}
	
	/* Move grid down */
	public class DownButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			yPos+=4;
			if (displayYEnd >= offWorldHeight-4) {
				yPos-=4;	
			}
		}
	}
	
	/* Move grid left */
	public class LeftButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			xPos-=4;
			if (displayXStart <= 4) {
				xPos+=4;
			}
		}
	}
	/* Move grid right */
	public class RightButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			xPos+=4;
			if (displayXEnd >= offWorldWidth-4) {
				xPos-=4;
			}
		}
	}

	/* Routine to move the grid with mouse */
	public class MoveMouse implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			
			/* Move grid left */
			if (rightMousePressed && (oldX > e.getX())) {
				xPos-=2;
				if (displayXStart <= 2) {
					xPos+=2;
				}
				oldX = e.getX();
			}
			/* Move grid right */
			else if (rightMousePressed && (oldX < e.getX())) {
				xPos+=2;
				if (displayXEnd >= offWorldWidth-2) {
					xPos-=2;
				}
				oldX = e.getX();	
			}
			/* Move grid up */
			if (rightMousePressed && (oldY > e.getY())) {
				yPos-=2;
				if (displayYStart <= 2) {
					yPos+=2;
				}
				oldY = e.getY();
			}
			/* Move grid down */
			else if (rightMousePressed && (oldY < e.getY())) {
				yPos+=2;
				if (displayYEnd >= offWorldHeight-2) {
					yPos-=2;
				}
				oldY = e.getY();
			}
		}

			/* Get mouse position */
		@Override
		public void mouseMoved(MouseEvent mousePosition) {
			mouseX = mousePosition.getX();
			mouseY = mousePosition.getY();	
		}
	}
	
	/* Handle the mouse buttons */
	public class ClickMouse implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) { }

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				toggleCell();
			}
	
			else if (e.getButton() == MouseEvent.BUTTON3) {
				rightMousePressed = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			rightMousePressed = false;
		}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
		
		
	}
	

	public void CalculateDisplay() {
		if (magnifierIndex == 0 && reset == true) {
			xPos = 0;
			yPos = 0;
			reset = false;
		}
	
		displayXStart = (offWorldWidth/2)-(gridWidth/2) + xPos; // The horizontal start point of the cells array displayed on screen.
		displayXEnd = (offWorldWidth/2)+(gridWidth/2) + xPos; // The horizontal end point of the cells array displayed on screen.
		displayYStart = (offWorldHeight/2)-(gridHeight/2) + yPos; // The vertical start point of the cells array displayed on screen.
		displayYEnd = (offWorldHeight/2)+(gridHeight/2) + yPos; // The vertical end point of the cells array displayed on screen.
	
	}
	
	
	public void toggleCell() {
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				if (mouseX >= cellSpacing+x*cellWidth && mouseX < x*cellWidth+cellWidth-cellSpacing && 
						mouseY >= y*cellHeight+29 && mouseY < y*cellHeight+cellHeight+29-cellSpacing) {
					if (cells[displayYStart + y][displayXStart + x] == false) { 
						cells[displayYStart + y][displayXStart + x] = true;	
					
					} else {
						cells[displayYStart + y][displayXStart + x] = false;	
					}
				}
			}
		}
	}
	
	/* Iterate through the cells array and check for neighbours */
	public void CheckForNeighbours() {
		int neighbours = 0;
		int xExpansion = 0;
		int yExpansion = 0;
		
		
		
		/* Check if the grid array needs to be expanded */
		for (int y = 0; y < offWorldHeight; y++) {
			for (int x = 5; x > 0; x--) {
				if (cells[y][x] == true || cells[y][offWorldWidth-x] == true) {xExpansion+=16;yExpansion+=16; break;}
			}
		}
		
		for (int x = 0; x < offWorldWidth; x++) {
			for (int y = 5; y > 0; y--) {
				if (cells[y][x] == true || cells[offWorldHeight-y][x] == true) {xExpansion+=16; yExpansion+=16; break;}
			}
		}
		
		cellsCopy = new boolean[offWorldHeight+yExpansion][offWorldWidth+xExpansion]; // Create the new expanded array.
	
		/* Copy the cells into the expanded Grid array. */
		for (int y = 0; y < offWorldHeight; y++) {
			for (int x = 0; x < offWorldWidth; x++) {
				cellsCopy[y+(yExpansion/2)][x+(xExpansion/2)] = cells[y][x];
			}
		}
		
		offWorldHeight += yExpansion;
		offWorldWidth += xExpansion;
		
		cells = new boolean[offWorldHeight][offWorldWidth];
		
		for (int y = 1; y < offWorldHeight-1; y++) {
			for (int x = 1; x < offWorldWidth-1; x++) {
				neighbours = 0; // Reset neighbours count.
				
				if (cellsCopy[y][x+1] == true) neighbours++;
				if (cellsCopy[y][x-1] == true) neighbours++;
				if (cellsCopy[y+1][x+1] == true) neighbours++;
				if (cellsCopy[y+1][x] == true) neighbours++;
				if (cellsCopy[y+1][x-1] == true) neighbours++;
				if (cellsCopy[y-1][x+1] == true) neighbours++;
				if (cellsCopy[y-1][x] == true) neighbours++;
				if (cellsCopy[y-1][x-1] == true) neighbours++;
				
				if ((cellsCopy[y][x] == true) && (neighbours < 2 || neighbours > 3)) {
					cells[y][x] = false;
				}
				
				else if ((cellsCopy[y][x] == true) && (neighbours == 2 || neighbours == 3)) {
					cells[y][x] = true;
				}
				
				else if ((cellsCopy[y][x] == false) && (neighbours == 3)) {
					cells[y][x] = true;
				}		
			}
		}
	}
    







    public static void main(String[] args) {
		GameOfLife gameOfLife = new GameOfLife(); // Create the main game object.
		
		while(true) {
			gameOfLife.repaint();	
		}
    
    }
}	
