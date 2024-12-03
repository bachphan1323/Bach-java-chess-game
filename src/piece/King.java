package piece;

import main.GamePanel;
import main.Type;

public class King extends Piece {

	public King(int color, int col, int row) {
		super(color, col, row);
		
		type = Type.KING;
		
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/w-king");
		}
		else {
			image = getImage("/piece/b-king");
		}
	}
	
	public boolean canMove (int targetCol, int targetRow) {
		
		// This is true if the mouse is pointing within the board
		if(isWithinBoard(targetCol, targetRow)) {
			
			// Regular Movement
			/* If the (difference between targetCol and preCol) plus (the difference between targetRow and preRow) equals 1: 
			 * it means the target square is right next to the king either vertically or horizontally. 
			 * If the ratio between these differences is equal to 1: 
			 * it means the target square is a diagonal square, which is valid. */
			if(Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow) == 1 || 
					Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 1) {
				if(isValidSquare(targetCol, targetRow)) {
					return true;
				}
			}
			// Castling Move
			if(moved == false) {
				
				// Short castling
				if(targetCol == preCol + 2 && targetRow == preRow && !pieceIsOnStraightLine(targetCol, targetRow)) {
					for(Piece piece : GamePanel.simPieces) {
						if(piece.col == preCol + 3 && piece.row == preRow && !piece.moved) {
							GamePanel.castlingP = piece; // setting castlingP to the Rook on king side
							return true;
						}
					}
				}
				// Long castling
				if(targetCol == preCol - 2 && targetRow == preRow && !pieceIsOnStraightLine(targetCol, targetRow)) {
					
					Piece p[] = new Piece[2];
					
					for(Piece piece : GamePanel.simPieces) {
						if(piece.col == preCol - 3 && piece.row == targetRow) { // set the piece right next to the rook to [0] in the array
							p[0] = piece;
						}
						if(piece.col == preCol - 4 && piece.row == targetRow) { // set the rook to [1] in the array
							p[1] = piece;
						}
						
						if(p[0] == null && p[1]!= null && !p[1].moved) { // if the piece right next to the rook is null & the rook hasn't moved
							GamePanel.castlingP = p[1]; // set castlingP to the Rook on the queen side
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
