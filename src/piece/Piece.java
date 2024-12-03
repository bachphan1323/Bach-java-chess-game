package piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Board;
import main.GamePanel;
import main.Type;

public class Piece {
	
	public Type type;
	public BufferedImage image;
	public int x, y;
	public int col, row, preCol, preRow;
	public int color;
	public Piece hittingP;
	public boolean moved, twoStepped;
	
	
	public Piece(int color, int col, int row) {
		
		this.color = color;
		this.col = col;
		this.row = row;
		x = getX(col);
		y = getY(row);
		preCol = col;
		preRow = row;
	}
	
	public BufferedImage getImage(String imagePath) {
		
		BufferedImage image = null;
		
		try {
			image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	/* Getter methods */
	// we can easily get x and y position of a piece by passing in col and row number
	public int getX(int col) {
		return col * Board.SQUARE_SIZE; // if a piece is on column 3, its X coordinate is 3 * 100 = 300px
	}
	public int getY(int row) {
		return row * Board.SQUARE_SIZE;// and if the piece is on row 2, its Y coordinate is 2 * 100 = 200px
	}
	
	// getting column number of a piece so GP can detect where the piece is at on the board
	public int getCol(int x) {
		/* added 50 to x before dividing because the default Java "hit-box" of the piece is top-left corner.
		 * Adding half-square size (50px) to bring the point to the center on x-plane */
		return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE; 
	}
	// getting row number of a piece so GP can detect where the piece is at on the board
	public int getRow(int y) {
		/* added half-square size (50px) to y before dividing because the default Java "hit-box" of the piece is top-left corner.
		 * This brings the point to the center on y-plane */
		return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
	}
	
	// get the array index number of a piece in the simPieces array when called
	public int getIndex() {
		for(int index = 0; index < GamePanel.simPieces.size(); index++) {
			if(GamePanel.simPieces.get(index) == this) {
				return index;
			}
		}
		return 0;
	}
	
	
	// update the piece's position when a move has been confirmed 
	public void updatePosition() {
		
		// Check for En Passant
		if(type == Type.PAWN) {
			if(Math.abs(row - preRow) == 2) {
				twoStepped = true;
			}
		}
		
		x = getX(col); 
		y = getY(row);
		preCol = getCol(x);
		preRow = getRow(y);
		moved = true;
	}
	// resets the piece-in-consideration's position because the target square was invalid
	public void resetPosition() {
		col = preCol;
		row = preRow;
		x = getX(col);
		y = getY(row);
	}
	
	/* canMove will be override in each piece. This section checks if a move is valid or not */
	public boolean canMove(int targetCol, int targetRow) { // targetCol & targetRow are the col and row where the mouse is pointing to
		return false;
	}
	public boolean isWithinBoard(int targetCol, int targetRow) {
		if(targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7) {
			return true;
		}
		return false;
	}
	public boolean isSameSquare(int targetCol, int targetRow) {
		if(targetCol == preCol && targetRow == preRow) {
			return true;
		}
		return false;
	}
	/* */
	
	/* the active piece can't move to a square that is occupied by an ally piece. 
	 * This method receives target column and row. Then it checks in the array to see if there is a piece currently 
	 * has the same column and row number as the target column and row (besides the active piece that the player is holding),
	 * and returns that occupying piece if there is one such piece. */
	public Piece getHittingP(int targetCol, int targetRow) {
		for(Piece piece : GamePanel.simPieces) {
			if(piece.col == targetCol && piece.row == targetRow && piece != this) {
				return piece;
			}
		}
		return null;
	}
	public boolean isValidSquare(int targetCol, int targetRow) {
		hittingP = getHittingP(targetCol, targetRow);
		
		// target square is vacant if there isn't a piece occupying it, or hittingP equals null:
		if(hittingP == null) {
			return true;
		}
		else { // if hittingP is !null, then the target square is occupied
			if(hittingP.color != this.color) { // if the occupying piece is an opponent's piece, it can be captured
				return true;
			}
			else { // if the occupying piece is an ally, reset hittingP to null and return false because this square is invalid 
				hittingP = null;
			}
		}
		return false;
	}
	public boolean pieceIsOnStraightLine(int targetCol, int targetRow) {
		
		// Check if the target square is directly adjacent horizontally
		if(Math.abs(preCol - targetCol) == 1 && preRow == targetRow) {
		    return false; // Moving one square left or right, no piece can be between
		}
		// Check if the target square is directly adjacent vertically
		if(Math.abs(preRow - targetRow) == 1 && preCol == targetCol) {
		    return false; // Moving one square up or down, no piece can be between
		}
		
		// When this piece is moving to the left
		for(int c = preCol - 1; c > targetCol; c--) { // int c = preCol-1 because checking the current column that the piece is on is not necessary
			for(Piece piece : GamePanel.simPieces) {
				/* Scan the array and check to see if there is a piece to the left (on the same row) of the active piece. */
				if(piece.col == c && piece.row == targetRow) { 
					hittingP = piece;
					return true;
				}
			}
		}
		// When this piece is moving to the right
		for(int c = preCol + 1; c < targetCol; c++) {
			for(Piece piece : GamePanel.simPieces) {
				if(piece.col == c && piece.row == targetRow) {
					hittingP = piece;
					return true;
				}
			}
		}
		// When this piece is moving up
		for(int r = preRow - 1; r > targetRow; r--) {
			for(Piece piece : GamePanel.simPieces) {
				if(piece.col == targetCol && piece.row == r) {
					hittingP = piece;
					return true;
				}
			}
		}
		// when this piece is moving down
		for(int r = preRow + 1; r < targetRow; r++) {
			for(Piece piece : GamePanel.simPieces) {
				if(piece.col == targetCol && piece.row == r) {
					hittingP = piece;
					return true;
				}
			}
		}
		return false;
	}
	public boolean pieceIsOnDiagonalLine(int targetCol, int targetRow) {
		
		// piece is moving up
		if(targetRow < preRow) {
			// Up Left
			for(int c = preCol - 1; c > targetCol; c--) {
				int diff = Math.abs(c - preCol);
				for(Piece piece: GamePanel.simPieces) {
					if(piece.col == c && piece.row == preRow - diff) {
						hittingP = piece;
						return true;
					}
				}
			}
			// Up Right
			for(int c = preCol + 1; c < targetCol; c++) {
				int diff = Math.abs(c - preCol);
				for(Piece piece: GamePanel.simPieces) {
					if(piece.col == c && piece.row == preRow - diff) {
						hittingP = piece;
						return true;
					}
				}
			}
		}
		// piece is moving down
		if(targetRow > preRow) {
			// Down Left
			for(int c = preCol - 1; c > targetCol; c--) {
				int diff = Math.abs(c - preCol);
				for(Piece piece: GamePanel.simPieces) {
					if(piece.col == c && piece.row == preRow + diff) {
						hittingP = piece;
						return true;
					}
				}
			}
			// Down Right
			for(int c = preCol + 1; c < targetCol; c++) {
				int diff = Math.abs(c - preCol);
				for(Piece piece: GamePanel.simPieces) {
					if(piece.col == c && piece.row == preRow + diff) {
						hittingP = piece;
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	public void draw(Graphics2D g2) {
		g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
	}
}
