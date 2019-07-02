package bridges.game;

/**
 * An enum representing each state a Board can have in a game:
 * <br><br>
 * NOBOARD: There's no board present.<br>
 * UNSOLVED: The board is valid, but not fully solved.<br>
 * SOLVED: The board is valid and fully solved.<br>
 * UNSOLVABLE: The board is valid, but can not be solved in the
 * current configuration (that is: some bridges are incorrect).<br>
 * INCORRECT: There are islands present, which have to many bridges.
 *
 * @author Maik Messerschmidt
 */
public enum BoardState {
    NOBOARD, UNSOLVED, SOLVED, UNSOLVABLE, INCORRECT;
}
