package tablut;

import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;

import static tablut.Piece.*;
import static tablut.Square.*;

/** The state of a Tablut Game.
 *  @author Matthew J. Lee
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        this.listOfPieces = model.listOfPieces;
        this._moveCount = model._moveCount;
        this._winner = model._winner;
        this._turn = model._turn;
        this._repeated = model._repeated;
    }

    /** A List of Pieces on my board. */
    private Piece[][] listOfPieces;

    /** Clears the board to the initial position. */
    void init() {
        listOfPieces = new Piece[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                listOfPieces[i][j] = EMPTY;
            }
        }

        for (Square sq : INITIAL_DEFENDERS) {
            listOfPieces[sq.col()][sq.row()] = WHITE;
        }

        for (Square sq : INITIAL_ATTACKERS) {
            listOfPieces[sq.col()][sq.row()] = BLACK;
        }

        listOfPieces[THRONE.col()][THRONE.row()] = KING;

        _winner = null;
        _turn = BLACK;
        _moveCount = 0;
        _repeated = false;
    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * @param n integer value representing limit number of moves.
     * */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw new Error("2*n <= number of moves");
        }
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        if (stackOfPlays.contains(listOfPieces)) {
            _repeated = true;
            _winner = turn().opponent();
        }
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        for (int col = 0; col < SIZE; col++) {
            for (int row = 0; row < SIZE; row++) {
                if (listOfPieces[col][row] == KING) {
                    return sq(col, row);
                }
            }
        }
        return null;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        if (col > 8 || col < 0) {
            throw new IllegalArgumentException("Column is off the board");
        }
        if (row < 0 || row > 8) {
            throw new IllegalArgumentException("Row is off the board");
        }
        return listOfPieces[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        listOfPieces[s.col()][s.row()] = p;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        stackOfPlays.add(deepcopy(listOfPieces));
        listOfPieces[s.col()][s.row()] = p;
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        if (!from.isRookMove(to)) {
            return false;
        } else {
            int direction = from.direction(to);
            if (direction == 0) {
                for (int n = from.row() + 1; n <= to.row(); n++) {
                    if (listOfPieces[from.col()][n] != EMPTY) {
                        return false;
                    }
                }
            }
            if (direction == 1) {
                for (int e = from.col() + 1; e <= to.col(); e++) {
                    if (listOfPieces[e][from.row()] != EMPTY) {
                        return false;
                    }
                }
            }
            if (direction == 2) {
                for (int s = from.row() - 1; s >= to.row(); s--) {
                    if (listOfPieces[from.col()][s] != EMPTY) {
                        return false;
                    }
                }
            } else {
                for (int w = from.col() - 1; w >= to.col(); w--) {
                    if (listOfPieces[w][from.row()] != EMPTY) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        if (from != kingPosition() && to == THRONE) {
            return false;
        }
        return (isUnblockedMove(from, to));
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        if (!isLegal(from, to)) {
            throw new IllegalArgumentException("It's blocked.");
        }
        if (!isLegal(from)) {
            throw new IllegalArgumentException(("Not your move"));
        }
        if (listOfPieces[from.col()][from.row()] != KING && to.equals(THRONE)) {
            throw new IllegalArgumentException("King"
                    + "ONLY on the throne.");
        }
        assert isLegal(from, to);
        stackOfPlays.add(deepcopy(listOfPieces));
        listOfPieces[to.col()][to.row()] = listOfPieces[from.col()][from.row()];
        listOfPieces[from.col()][from.row()] = EMPTY;
        Move.mv(from, to);
        if (kingPosition() != null && kingPosition().isEdge()) {
            _winner = WHITE;
        }
        _moveCount += 1;
        _lastMove = Move.mv(from, to);
        if (to.row() - 2 >= 0) {
            capture(to, sq(to.col(), to.row() - 2));
        }
        if (to.col() - 2 >= 0) {
            capture(to, sq(to.col() - 2, to.row()));
        }
        if (to.row() + 2 <= 8) {
            capture(to, sq(to.col(), to.row() + 2));
        }
        if (to.col() + 2 <= 8) {
            capture(to, sq(to.col() + 2, to.row()));
        }


        checkRepeated();
        _turn = _turn.opponent();
        hasMove(_turn);
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        assert isLegal(move.from(), move.to());
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Square btw = sq0.between(sq2);
        if (kingPosition() == null) {
            return;
        }
        int c = kingPosition().col();
        int r = kingPosition().row();
        Piece older  = listOfPieces[sq0.col()][sq0.row()];
        Piece newer = listOfPieces[sq2.col()][sq2.row()];
        if (_lastMove.to() == sq0) {
            if (newer == KING
                    && listOfPieces[btw.col()][btw.row()] == WHITE) {
                captureHelper(btw);
            } else if (newer == BLACK
                    && listOfPieces[btw.col()][btw.row()] == KING) {
                if (kingPosition() == THRONE) {
                    if (listOfPieces[c + 1][r] == BLACK
                            && listOfPieces[c - 1][r]
                            == BLACK && listOfPieces[c][r + 1] == BLACK
                            && listOfPieces[c][r - 1] == BLACK) {
                        listOfPieces[c][r] = EMPTY;
                        _winner = BLACK;
                    }
                }
                if (kingPosition() != null) {
                    if (kingPosition() == NTHRONE || kingPosition() == STHRONE
                            || kingPosition() == WTHRONE
                            || kingPosition() == ETHRONE) {
                        int numBlack = 0;
                        if (listOfPieces[c + 1][r] == BLACK) {
                            numBlack += 1;
                        } else if (listOfPieces[c - 1][r] == BLACK) {
                            numBlack += 1;
                        } else if (listOfPieces[c][r + 1] == BLACK) {
                            numBlack += 1;
                        } else if (listOfPieces[c][r - 1] == BLACK) {
                            numBlack += 1;
                        } else if (numBlack == 3) {
                            listOfPieces[btw.col()][btw.row()] = EMPTY;
                            _winner = BLACK;
                        }
                    }
                }
            }
            if (older == newer || newer == EMPTY && sq2 == THRONE
                    || older == WHITE && newer == KING
                    || newer == WHITE && older == KING) {
                if (_turn != listOfPieces[btw.col()][btw.row()]
                        && listOfPieces[btw.col()][btw.row()] != KING) {
                    listOfPieces[btw.col()][btw.row()] = EMPTY;
                } else if (listOfPieces[btw.col()][btw.row()] == KING
                        && kingPosition() != THRONE && kingPosition() != NTHRONE
                        && kingPosition() != ETHRONE && kingPosition()
                        != WTHRONE && kingPosition() != STHRONE) {
                    listOfPieces[btw.col()][btw.row()] = EMPTY;
                    _winner = BLACK;
                }
            }
        }
    }

    /** A helper method for capture.
     * @param btwn The Square between.*/
    private void captureHelper(Square btwn) {
        if (kingPosition() == THRONE) {
            int counter = 0;
            for (int z = 0; z <= 3; z++) {
                if (listOfPieces[INITIAL_DEFENDERS[z].col()]
                        [INITIAL_DEFENDERS[z].row()] == BLACK) {
                    counter += 1;
                }
            }
            if (counter == 3) {
                listOfPieces[btwn.col()][btwn.row()] = EMPTY;
            }
        }
    }

    /** Undo one move.  Has no effect on the initial board. */

    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            _moveCount -= 1;
            _turn = _turn.opponent();
        } else {
            throw new IllegalArgumentException("You are "
                     + "at the beginning of the game!");
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        if (!_repeated && _moveCount > 0) {
            listOfPieces = stackOfPlays.pop();
        } else {
            throw new IllegalArgumentException(("This can't be done."));
        }
        _repeated = false;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        stackOfPlays.clear();
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        List<Move> sideOfLegalMoves = new ArrayList<Move>();
        for (Square squareFrom : pieceLocations(side)) {
            int direction = 0;
            while (direction < 4) {
                for (Square squareTo
                        :Square.ROOK_SQUARES[squareFrom.index()][direction]) {
                    if (isLegal(squareFrom, squareTo)) {
                        sideOfLegalMoves.add(Move.mv(squareFrom, squareTo));
                    }
                }
                direction++;
            }
        }
        return sideOfLegalMoves;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        if (legalMoves(side).isEmpty()) {
            _winner = side.opponent();
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Creates copies of the list of pieces.
     * @param pieceList an input of a double array.
     * @return piece */
    private Piece [][] deepcopy(Piece[][] pieceList) {
        Piece[][] piececopy = new Piece[SIZE][SIZE];
        for (int col = 0; col < SIZE; col++) {
            for (int row = 0; row < SIZE; row++) {
                if (pieceList[col][row] == BLACK) {
                    piececopy[col][row] = BLACK;
                } else if (pieceList[col][row] == WHITE) {
                    piececopy[col][row] = WHITE;
                } else if (pieceList[col][row] == EMPTY) {
                    piececopy[col][row] = EMPTY;
                } else if (pieceList[col][row] == KING) {
                    piececopy[col][row] = KING;
                }
            }
        }
        return piececopy;
    }

    /** Return the locations of all pieces on SIDE. */
    public HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> pieceLocats = new HashSet<>();
        for (int col = 0; col < 9; col++) {
            for (int row = 0; row < 9; row++) {
                if (listOfPieces[col][row] == side) {
                    pieceLocats.add(sq(col, row));
                }
                if (side == WHITE && listOfPieces[col][row] == KING) {
                    pieceLocats.add(sq(col, row));
                }
            }
        }
        return pieceLocats;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;

    /** Stack of iterations of the board game play. */
    private Stack<Piece[][]> stackOfPlays = new Stack<>();
    /** Store the previous move. */
    private Move _lastMove;

}
