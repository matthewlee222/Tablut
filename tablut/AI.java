package tablut;

import static java.lang.Math.*;
import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Matthew J. Lee
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        if (_myPiece == WHITE) {
            findMove(this.board(),
                    maxDepth(this.board()), true, 1, -INFTY, INFTY);
        } else {
            findMove(this.board(),
                    maxDepth(this.board()), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {


        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        int best = 0;
        if (sense == 1) {
            best = -INFTY;
            for (Move mv : board.legalMoves(WHITE)) {
                board.makeMove(mv);
                int response = findMove(board,
                        depth - 1, false, -1, alpha, beta);
                if (response >= best) {
                    best = response;
                    if (saveMove) {
                        _lastFoundMove = mv;
                    }
                }
                board.undo();
                alpha = max(alpha, best);
                if (alpha >= beta) {
                    break;
                }
            }
        } else {
            best = INFTY;
            for (Move mv : board.legalMoves(BLACK)) {
                board.makeMove(mv);
                int response = findMove(board,
                        depth - 1, false, 1, alpha, beta);
                if (response <= best) {
                    best = response;
                    if (saveMove) {
                        _lastFoundMove = mv;
                    }
                }
                board.undo();
                beta = min(beta, best);
                if (alpha >= beta) {
                    break;
                }
            }
        }
        return best;
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        int N = board.moveCount();
        return (N / MAXDEPTH) + ONE;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        int statScore = 0;
        if (board.winner() != null) {
            if (board.winner().equals(WHITE)) {
                statScore += WINNING_VALUE;
            }
            if (board.winner().equals(BLACK)) {
                statScore -= -WINNING_VALUE;
            }
        } else {
            statScore += board.pieceLocations(WHITE).size() * 8;
            statScore -= board.pieceLocations(BLACK).size() * 8;
            int from = min(min(8 - board.kingPosition().row(),
                    board.kingPosition().row()), min(8
                    - board.kingPosition().col(), board.kingPosition().col()));
            statScore += 10 * from;
        }

        return statScore;
    }

    /** Incrementing the depth by one integer value. **/
    private static final int ONE = 1;

    /** Setting a value for depth and potentially updating it. **/
    private static final int MAXDEPTH = 40;

}
