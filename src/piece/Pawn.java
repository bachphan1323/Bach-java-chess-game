package piece;

import main.GamePanel;
import main.Type;

public class Pawn extends Piece {
	
	public Pawn(int color, int col, int row) {
		super(color, col, row);
		
		type = Type.PAWN; 
		
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/w-pawn");
		}
		else {
			image = getImage("/piece/b-pawn");
		}
	}
	public boolean canMove(int targetCol, int targetRow) {
		
		if(isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
			
			// define the move value based on its color
			int moveValue;
			
			if(color == GamePanel.WHITE) {
				moveValue = -1; // move up for white
			}
			else {
				moveValue = 1; // move down for black
			}
			
			// Pawn can't move forward when there is a piece blocking its path
			hittingP = getHittingP(targetCol, targetRow);
			// 1 square movement
			if(targetCol == preCol && targetRow == preRow + moveValue && hittingP == null) {
				return true;
			}
			// 2 square movement
			if(targetCol == preCol && targetRow == preRow + moveValue * 2 && hittingP == null && moved == false
					&& !pieceIsOnStraightLine(targetCol, targetRow)) {
				return true;
			}
			// Capture move
			if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null 
					&& hittingP.color != color) {
				return true;
			}
			
			// EnPassant
			if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
				for(Piece piece : GamePanel.simPieces) {
					if(piece.col == targetCol && piece.row == preRow && piece.twoStepped == true) {
						hittingP = piece;
						return true;
					}
				}
			}
			
		}
		return false;
	}
}
