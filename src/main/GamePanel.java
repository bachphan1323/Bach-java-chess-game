package main;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable {
	public static final int WIDTH = 1300;
	public static final int HEIGHT = 800;
	final int FPS = 60;
	Thread gameThread;
	Board board = new Board();
	Mouse mouse = new Mouse();
	
	// Pieces
	public static ArrayList<Piece> pieces = new ArrayList<>();
	public static ArrayList<Piece> simPieces = new ArrayList<>();
	ArrayList<Piece> promoPieces = new ArrayList<>();
	Piece activeP; // will be the piece that the player is currently holding
	Piece checkingP;
	public static Piece castlingP;
	
	// Color
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;
	
	// Booleans
	boolean canMove;
	boolean validSquare;
	boolean promotion;
	boolean gameover;
	boolean stalemate;
	
	// Constructor of GamePanel class
	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		setBackground(Color.black);
		
		/* The program can detect the player's mouse movement or action */
		addMouseMotionListener(mouse);
		addMouseListener(mouse);
		
		/* Adding pieces into the ArrayList: pieces */
//		setPieces();
//		testChecking();
		testStalemate();

		copyPieces(pieces, simPieces);
	}
	
	public void launchGame() {
		gameThread = new Thread(this);
		gameThread.start(); //start() calls the run() method
	}
	
	public void setPieces( ) {
		//White pieces
		pieces.add(new Pawn(WHITE,0,6));
		pieces.add(new Pawn(WHITE,1,6));
		pieces.add(new Pawn(WHITE,2,6));
		pieces.add(new Pawn(WHITE,3,6));
		pieces.add(new Pawn(WHITE,4,6));
		pieces.add(new Pawn(WHITE,5,6));
		pieces.add(new Pawn(WHITE,6,6));
		pieces.add(new Pawn(WHITE,7,6));
		pieces.add(new Knight(WHITE,1,7));
		pieces.add(new Knight(WHITE,6,7));
		pieces.add(new Rook(WHITE,0,7));
		pieces.add(new Rook(WHITE,7,7));
		pieces.add(new Bishop(WHITE,2,7));
		pieces.add(new Bishop(WHITE,5,7));
		pieces.add(new Queen(WHITE,3,7));
		pieces.add(new King(WHITE,4,7));
		
		//Black pieces
		pieces.add(new Pawn(BLACK,0,1));
		pieces.add(new Pawn(BLACK,1,1));
		pieces.add(new Pawn(BLACK,2,1));
		pieces.add(new Pawn(BLACK,3,1));
		pieces.add(new Pawn(BLACK,4,1));
		pieces.add(new Pawn(BLACK,5,1));
		pieces.add(new Pawn(BLACK,6,1));
		pieces.add(new Pawn(BLACK,7,1));
		pieces.add(new Knight(BLACK,1,0));
		pieces.add(new Knight(BLACK,6,0));
		pieces.add(new Rook(BLACK,0,0));
		pieces.add(new Rook(BLACK,7,0));
		pieces.add(new Bishop(BLACK,2,0));
		pieces.add(new Bishop(BLACK,5,0));
		pieces.add(new Queen(BLACK,3,0));
		pieces.add(new King(BLACK,4,0));
	}
	
	// test Checking announcement
	public void testChecking() {
		pieces.add(new King(WHITE,4,7));
		pieces.add(new Queen(WHITE,3,7));
		pieces.add(new Knight(WHITE,1,7));
		pieces.add(new Knight(WHITE,6,7));
		pieces.add(new King(BLACK,4,0));
		pieces.add(new Queen(BLACK,3,0));
		pieces.add(new Bishop(BLACK,2,0));
		pieces.add(new Bishop(BLACK,5,0));
	}
	// test stalemate annoucement
	public void testStalemate() {
		pieces.add(new King(BLACK,0,3));
		pieces.add(new Queen(WHITE,1,1));
		pieces.add(new King(WHITE,2,4));
	}
	
	//copy the pieces in ArrayList: pieces and paste them in ArrayList: simPieces
	private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
		
		target.clear();
		for(int i = 0; i < source.size(); i++) {
			target.add(source.get(i));
		}
	}
	
	@Override
	public void run() {
		
		//Game loop
		double drawInterval = 1000000000/FPS; // 1 billion nanoseconds / 60 = 16,666,666 million nanoseconds is the desired frame-drawing interval of 1 frame
		double delta = 0; 
		long lastTime = System.nanoTime();
		long currentTime;
		long timer = 0;
		int drawCount = 0;
		
		while(gameThread != null) {
			
			currentTime = System.nanoTime();
			
			/* currentTime - lastTime = how much time has passed */
			delta += (currentTime - lastTime) / drawInterval;  // delta holds the fraction between how much time has passed and the desired drawing interval
			
			timer += currentTime - lastTime; // timer keeps track of the time passed since the last frame
			lastTime = currentTime; // update lastTime to this iteration's currentTime
			
			/* delta >= 1 means enough time has passed for at least 1 frame */
			if(delta >= 1) {
				update(); // update the position of the pieces
				repaint(); // repaint the pieces
				delta--;
				drawCount++;
			}
			
			/* 1000000000 nanoseconds = 1 second. if 1 second or more has passed, then print the current drawCount
			 * check if the repaint() method is being called 60 times in a second */
			if(timer >= 1000000000) {
				System.out.println("FPS:" + drawCount);
				drawCount = 0;
				timer = 0;
			}
		}
	}
	
	// the game loop will call these two methods 60 times/second
	private void update() {
		
		if(promotion) {
			promoting();
		}
		else if(!gameover && !stalemate) {
			/* Mouse pressed */
			if(mouse.pressed) {
				if(activeP == null) { /* activeP = null means that the player isn't already holding a piece */
					for(Piece piece: simPieces) {
						
						/* if the piece in consideration has the same color as the turn's color (Black or White's move) 
						 * and its column and row number is = mouse's grid position (column, row) then the player can pick it up */
						if(piece.color == currentColor && 
								piece.col == mouse.x/Board.SQUARE_SIZE && 
								piece.row == mouse.y/Board.SQUARE_SIZE) {
							activeP = piece;
						}
					}
				}
				else {
					simulate();
				}
			}
			
			/* Mouse button released */
			if(mouse.pressed == false) {
				if(activeP != null) {
					if(validSquare) {
						
						// Move Confirmed
						
						// Update the piece list in case a piece has been captured during the simulation phase
						copyPieces(simPieces, pieces); 
						// player confirmed the move, update the piece's position with new col and row
						activeP.updatePosition(); 
						
						// check if castlingP is storing a rook
						if(castlingP != null) {
							castlingP.updatePosition(); // update its cordinates to confirm the castling move
						}
						
						if(isKingInCheck() && isCheckmate()) {
							gameover = true;
						}
						else if(isStalemate() && !isKingInCheck()) {
							stalemate = true;
						}
						else {
							// Pawn promotion
							if(canPromote()) {
								promotion = true;
							} 
							else {
								changePlayer();
							}
						}
					} 
					else {
						// The move is not valid so reset everything
						copyPieces(pieces, simPieces);
						activeP.resetPosition();
						activeP = null;
					}
				}
			}
		}
	}
	
	// once a piece is being held, player can think about where to put it down. This method updates the piece's x, y coordinates to the mouse's x, y.
	private void simulate() {
		
		canMove = false;
		validSquare = false;
		
		/* Reset the piece list in every loop. 
		 * This is basically for restoring the removed piece during simulation phase, when activeP 
		 * might touch the opponent's piece without confirming the capture move. */
		copyPieces(pieces, simPieces);
		
		// Reset the castling piece's position after the simulation is over
		if(castlingP != null) {
			castlingP.col = castlingP.preCol;
			castlingP.x = castlingP.getX(castlingP.col);
			castlingP = null;
		}
		
		/* By default, the mouse pointer is set by Java to be at the top-left corner of the
		 * image. By - half square-size both horizontally and vertically, we bring the 
		 * mouse pointer to the center of the image */
		activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
		activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
		
		/* Adjust the registered hit-box of the piece to the center of the square instead of the top-left corner per default by Java */
		activeP.col = activeP.getCol(activeP.x);
		activeP.row = activeP.getRow(activeP.y);
		
		/* Check if the mouse is hovering over a valid square*/
		if(activeP.canMove(activeP.col, activeP.row)) { // set canMove and validSquare to true
			
			canMove = true;
			
			// If capturing a piece, remove the captured piece from the list
			if(activeP.hittingP != null) {
				simPieces.remove(activeP.hittingP.getIndex());
			}
			
			// Handling castling
			checkCastling();
			
			// Handling King's illegal movement
			if(!isIllegal(activeP) && !opponentCanCaptureKing()) {
				validSquare = true;
			}
		}
	}
	
	/* Handling King's illegal movement */
	private boolean isIllegal(Piece king) {
		
		if(king.type == Type.KING) {
			for(Piece piece : simPieces) {
				// check if there is a piece that can move to the square that the king is trying to move. 
				// this would be an illegal move
				if(piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
					return true;
				}
			}
		}
		return false;
	}
	private	boolean opponentCanCaptureKing() {
		// getting my king
		Piece king = getKing(false);
		
		for(Piece piece : simPieces) {
			if(piece.color != king.color && piece.canMove(king.col, king.row)) {
				return true;
			}
		}
		return false;
	}
	private boolean isKingInCheck() {
		
		// getting the opponent's king 
		Piece king = getKing(true);
		
		// if our activeP can move to where their King is, then their king is in check. Set checkingP to activeP
		if(activeP.canMove(king.col, king.row)) {
			checkingP = activeP;
			return true;
		}
		else {
			checkingP = null;
		}
		return false;
	}
	private Piece getKing(boolean opponent) {
		
		Piece king = null;
		for(Piece piece : simPieces) {
			if(opponent) {
				if(piece.type == Type.KING && piece.color != currentColor) {
					king = piece;
				}
			}
			else {
				if(piece.type == Type.KING && piece.color == currentColor) {
					king = piece;
				}
			}
		}
		return king;
	}
	
	/* Handling Checkmate */
	private boolean isCheckmate() {
		
		Piece king = getKing(true);
		
		if(kingCanMove(king)) {
			return false;
		}
		// check if block with our piece is possible
		else { 
			// check the position of the checking piece and the king in check
			int colDiff = Math.abs(checkingP.col - king.col);
			int rowDiff = Math.abs(checkingP.row - king.row);
			
			if(colDiff == 0) {
				
				// the checking piece is checking on the same vertical file
				if(checkingP.row < king.row) { // the checking piece is above
					// for every square between the checking piece and the king
					for(int row = checkingP.row; row < king.row; row++) {
						// scan the simPieces array to find a piece that can move to that square to block the check
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
				// the checking piece is below
				if(checkingP.row > king.row) {
					// for every square between the checking piece and the king
					for(int row = checkingP.row; row > king.row; row--) {
						// scan the simPieces array to find a piece that can move to that square to block the check
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
			}
			// the checking piece is checking on the same horizontal file
			else if(rowDiff == 0) {
				
				if(checkingP.col < king.col) {
					// for every square between the checking piece and the king
					for(int col = checkingP.col; col < king.col; col++) {
						// scan the simPieces array to find a piece that can move to that square to block the check
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				if(checkingP.col > king.col) {
					// for every square between the checking piece and the king
					for(int col = checkingP.col; col > king.col; col--) {
						// scan the simPieces array to find a piece that can move to that square to block the check
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
			}
			// the checking piece is checking diagonally
			else if(colDiff == rowDiff) {
				
				// the checking piece is above the king
				if(checkingP.row < king.row) {
					// upper left
					if(checkingP.col < king.col) {
						for(int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
					// upper right
					if(checkingP.col > king.col) {
						for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
				}
				// the checking piece is below the king
				if(checkingP.row > king.row) {
					// lower left
					if(checkingP.col < king.col) {
						for(int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
					// lower right
					if(checkingP.col > king.col) {
						for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.	canMove(col, row)) {
									return false;
								}
							}
						}
					}
				}
				
			}
		}
		
		return true;
	}
	private boolean kingCanMove(Piece king) {
		
		// simulate if there is any square the king can move to 
		if(isValidMove(king, -1, -1)) {return true;}
		if(isValidMove(king, 0, -1)) {return true;}
		if(isValidMove(king, 1, -1)) {return true;}
		if(isValidMove(king, -1, 0)) {return true;}
		if(isValidMove(king, 1, 0)) {return true;}
		if(isValidMove(king, -1, 1)) {return true;}
		if(isValidMove(king, 0, 1)) {return true;}
		if(isValidMove(king, 1, 1)) {return true;}
		
		return false;
	}
	private boolean isValidMove(Piece king, int colPlus, int rowPlus) {
		
		boolean isValidMove = false;
		
		// temporarily update the king's position to simulate move
		king.col += colPlus;
		king.row += rowPlus;
		
		if(king.canMove(king.col, king.row)) {
			
			if(king.hittingP != null) {
				simPieces.remove(king.hittingP.getIndex());
			}
			if(!isIllegal(king)) {
				isValidMove = true;
			}
		}
		
		// reset the king's position and the piece arrayList after simulation
		king.resetPosition();
		copyPieces(pieces, simPieces);
		
		return isValidMove;
	}
	
	/* Handling stalemate */
	private boolean isStalemate() {
		int count = 0;
		// count the number of pieces
		for(Piece piece : simPieces) {
			if(piece.color != currentColor) {
				count++;
			}
		}
		// if there is only 1 piece left, i.e the king
		if(count == 1) {
			if(!kingCanMove(getKing(true))) {
				return true;
			}
		}
		
		return false;
	}
	
	/* Castling */
	private void checkCastling() {
		
		if(castlingP != null) {
			if(castlingP.col == 0) { //  if its the left rook
				castlingP.col += 3; //  then plus 3 to its col number
			}
			else if(castlingP.col == 7) { //  if its the right rook
				castlingP.col -= 2; //  then minus 2 to its col number
			}
			castlingP.x = castlingP.getX(castlingP.col); // update the rook's x cordinate with the new col number
		}
	}
	
	// Changing Turns
	private void changePlayer() {
		if(currentColor == WHITE) {
			currentColor = BLACK;
			// Reset black's two stepped status
			for(Piece piece : pieces) {
				if(piece.color == BLACK) {
					piece.twoStepped = false;
				}
			}
		}
		else {
			currentColor = WHITE;
			// Reset white's two stepped status
			for(Piece piece : pieces) {
				if(piece.color == WHITE) {
					piece.twoStepped = false;
				}
			}
		}
		activeP = null;
	}
	
	// Promotion 
	private boolean canPromote() {
	
		if(activeP.type == Type.PAWN) {
			// if pawn has reached the promotion rank
			if(currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7) {
				// this is to display the options for the pawn to promote to. Add them to the promoPieces list
				promoPieces.clear();
				promoPieces.add(new Rook(currentColor,9,2));
				promoPieces.add(new Knight(currentColor,9,3));
				promoPieces.add(new Bishop(currentColor,9,4));
				promoPieces.add(new Queen(currentColor,9,5));
				return true;
			}
		}
		
		return false;
	}
	
	// Pawn Promotion Method
	private void promoting() {
		if(mouse.pressed) {
			for(Piece piece : promoPieces) {
				if(piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE) {
					switch(piece.type) {
					case ROOK: simPieces.add(new Rook(currentColor, activeP.col, activeP.row)); break;
					case KNIGHT: simPieces.add(new Knight(currentColor, activeP.col, activeP.row)); break;
					case BISHOP: simPieces.add(new Bishop(currentColor, activeP.col, activeP.row)); break;
					case QUEEN: simPieces.add(new Queen(currentColor, activeP.col, activeP.row)); break;
					default: break;
					}
					simPieces.remove(activeP.getIndex());
					copyPieces(simPieces, pieces);
					activeP = null;
					promotion = false;
					changePlayer();
				}
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		
		//Board
		board.draw(g2);
		
		//Pieces
		for(Piece p : simPieces) {
			p.draw(g2);
		}
		
		// if player is holding a piece
		if(activeP != null) {
			
			if(canMove) {
				
				if(isIllegal(activeP) || opponentCanCaptureKing()) {
					g2.setColor(Color.red);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
					g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				}
				else {
					g2.setColor(Color.white);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
					g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				}
			}
			
			activeP.draw(g2);
		}
		
		/* In-Game Messages */
		// Status Messages:
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
		g2.setColor(Color.white);
		
		if(promotion) {
			g2.drawString("Promote to:", 840, 150);
			for(Piece piece : promoPieces) {
				g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
			}
		} 
		else {
			if(currentColor == WHITE) {
				g2.drawString("White's turn!", 830, 550);
				if(checkingP != null && checkingP.color == BLACK) {
					g2.setColor(Color.red);
					g2.drawString("The King", 830, 600);
					g2.drawString("is in check!", 830, 650);
					g2.drawString("Block the check or move", 830, 700);
					g2.drawString("your king to safety", 830, 750);
				}
			} 
			else {
				g2.drawString("Black's turn!", 830, 260);
				if(checkingP != null && checkingP.color == WHITE) {
					g2.setColor(Color.red);
					g2.drawString("The King", 830, 50);
					g2.drawString("is in check!", 830, 100);
					g2.drawString("Block the check or move", 830, 150);
					g2.drawString("your king to safety", 830, 200);
				}
			}
		}
		if(gameover) {
			String s = "";
			if(currentColor == WHITE) {
				s = "Game Over, White Wins";
			}
			else {
				s = "Game Over, Black Wins";
			}
			g2.setFont(new Font("Arial",Font.PLAIN, 90));
			g2.setColor(Color.green);
			g2.drawString(s, 200, 420);
		}
		if(stalemate) {
			g2.setFont(new Font("Arial",Font.PLAIN, 90));
			g2.setColor(Color.lightGray);
			g2.drawString("Stalemate!", 200, 420);
		}
	}
}
