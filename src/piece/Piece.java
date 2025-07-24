package piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import main.GamePanel;
import main.Type;

import static main.GamePanel.WHITE;

public class Piece implements Cloneable {
    public Type type;
    public BufferedImage image;
    public int x;
    public int y;
    public int col;
    public int row;
    public int preCol;
    public int preRow;
    public int color;
    public Piece hittingP;
    public boolean moved;
    public boolean twoMoved;
    public ArrayList<Piece> connectedPieces = new ArrayList<>();
    public Complex amplitude = Complex.ONE;
    public boolean hasMoved;

    public Piece(int color, int col, int row) {
        this.color = color;
        this.col = col;
        this.row = row;
        this.x = this.getX(col);
        this.y = this.getY(row);
        this.preCol = col;
        this.preRow = row;
    }

    public BufferedImage getImage(String imagePath) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream(imagePath + ".png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public int getX(int col) {
        return col * 100;
    }

    public int getY(int row) {
        return row * 100;
    }

    public int getCol(int x) {
        return (x + 50) / 100;
    }

    public int getRow(int y) {
        return (y + 50) / 100;
    }

    public int getIndex() {
        for(int index = 0; index < GamePanel.simPieces.size(); ++index) {
            if (GamePanel.simPieces.get(index) == this) {
                return index;
            }
        }

        return 0;
    }

    public void updatePosition() {
        if (this.type == Type.PAWN && Math.abs(this.row - this.preRow) == 2) {
            this.twoMoved = true;
        }

        this.x = this.getX(this.col);
        this.y = this.getY(this.row);
        this.preCol = this.getCol(this.x);
        this.preRow = this.getRow(this.y);
        this.moved = true;
    }

    public void resetPosition() {
        this.col = this.preCol;
        this.row = this.preRow;
        this.x = this.getX(this.col);
        this.y = this.getY(this.row);
    }

    public boolean canMove(int targetCol, int targetRow) {
        return false;
    }

    public boolean isWithinBoard(int targetCol, int targetRow) {
        return targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7;
    }

    public boolean isSameSquare(int targetCol, int targetRow) {
        return targetCol == this.preCol && targetRow == this.preRow;
    }

    public Piece getHittingP(int targetCol, int targetRow) {
        for(Piece piece : GamePanel.simPieces) {
            if (piece.col == targetCol && piece.row == targetRow && piece != this) {
                return piece;
            }
        }

        return null;
    }

    public boolean isValidSquare(int targetCol, int targetRow) {
        this.hittingP = this.getHittingP(targetCol, targetRow);
        if (this.hittingP == null) {
            return true;
        } else if (this.hittingP.color != this.color) {
            return true;
        } else {
            this.hittingP = null;
            return false;
        }
    }

    public boolean isPieceOnStraightLine(int targetCol, int targetRow) {
        for(int c = this.preCol - 1; c > targetCol; --c) {
            for(Piece piece : GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    this.hittingP = piece;
                    return true;
                }
            }
        }

        for(int c = this.preCol + 1; c < targetCol; ++c) {
            for(Piece piece : GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    this.hittingP = piece;
                    return true;
                }
            }
        }

        for(int r = this.preRow - 1; r > targetRow; --r) {
            for(Piece piece : GamePanel.simPieces) {
                if (piece.row == r && piece.col == targetCol) {
                    this.hittingP = piece;
                    return true;
                }
            }
        }

        for(int r = this.preRow + 1; r < targetRow; ++r) {
            for(Piece piece : GamePanel.simPieces) {
                if (piece.row == r && piece.col == targetCol) {
                    this.hittingP = piece;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isPieceOnDiagonal(int targetCol, int targetRow) {
        if (targetRow < this.preRow) {
            for(int c = this.preCol - 1; c > targetCol; --c) {
                int diff = Math.abs(c - this.preCol);

                for(Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == this.preRow - diff) {
                        this.hittingP = piece;
                        return true;
                    }
                }
            }

            for(int c = this.preCol + 1; c < targetCol; ++c) {
                int diff = Math.abs(c - this.preCol);

                for(Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == this.preRow - diff) {
                        this.hittingP = piece;
                        return true;
                    }
                }
            }
        }

        if (targetRow > this.preRow) {
            for(int c = this.preCol - 1; c > targetCol; --c) {
                int diff = Math.abs(c - this.preCol);

                for(Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == this.preRow + diff) {
                        this.hittingP = piece;
                        return true;
                    }
                }
            }

            for(int c = this.preCol + 1; c < targetCol; ++c) {
                int diff = Math.abs(c - this.preCol);

                for(Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == this.preRow + diff) {
                        this.hittingP = piece;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(this.image, this.x + 5, this.y + 5, 90, 90, null);
    }

    public Piece copy() {
        return clone();
    }

    @Override
    public Piece clone() {
        try {
            Piece cloned = (Piece) super.clone();
            cloned.connectedPieces = new ArrayList<>();
            cloned.connectedPieces.addAll(this.connectedPieces);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void normalizeAmplitude() {
        double normSquared = 0.0;
        ArrayList<Piece> all = new ArrayList<>(connectedPieces);
        all.add(this);
        for (Piece p : all) {
            normSquared += p.amplitude.absSquared();
        }
        double norm = Math.sqrt(normSquared);

        // Normalize each amplitude
        if (norm > 0) {
            for (Piece p : all) {
                p.amplitude = p.amplitude.divide(norm);
            }
        } else {
            for (Piece p : all) {
                p.amplitude = Complex.ZERO;
            }
        }
    }
    public boolean canAttack(int targetCol, int targetRow, List<Piece> board) {
        // Pawns attack diagonally, not forward
        if (this.type == Type.PAWN) {
            int direction = (this.color == WHITE) ? -1 : 1;
            return (targetRow == this.row + direction) &&
                    (targetCol == this.col + 1 || targetCol == this.col - 1);
        }

        // For sliding pieces, check path clearance
        if (this.type == Type.BISHOP || this.type == Type.ROOK || this.type == Type.QUEEN) {
            if (!isPathClear(this.col, this.row, targetCol, targetRow, board)) {
                return false;
            }
        }

        // Use canMove if movement logic is valid and includes checks
        return this.canMove(targetCol, targetRow);
    }
    private boolean isPathClear(int startCol, int startRow, int endCol, int endRow, List<Piece> board) {
        int dCol = Integer.compare(endCol, startCol);
        int dRow = Integer.compare(endRow, startRow);

        int col = startCol + dCol;
        int row = startRow + dRow;

        while (col != endCol || row != endRow) {
            for (Piece p : board) {
                if (p.col == col && p.row == row) return false; // something blocks the path
            }
            col += dCol;
            row += dRow;
        }
        return true;
    }
    private boolean pathIsClearQuick(int targetCol, int targetRow, Map<String, Piece> boardMap) {
        int dCol = Integer.compare(targetCol, col);
        int dRow = Integer.compare(targetRow, row);

        for (int c = col + dCol, r = row + dRow; c != targetCol || r != targetRow; c += dCol, r += dRow) {
            if (boardMap.containsKey(c + "," + r)) return false;
        }
        return true;
    }
}
