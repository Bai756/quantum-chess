package piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import javax.imageio.ImageIO;
import main.GamePanel;
import main.Type;

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
    public double probability = 1.0;
    public ArrayList<Piece> connectedPieces = new ArrayList<>();

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
}
