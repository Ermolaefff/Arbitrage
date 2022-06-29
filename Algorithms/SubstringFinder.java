package MONEYpackage.Algorithms;

public class SubstringFinder {
    private final int finalCondition;
    private int currentCondition = 0;
    private int[][] dfa; //Deterministic Finite Automaton

    public SubstringFinder(String pat) {
        finalCondition = pat.length();
        if (finalCondition == 0) return;
        int R = 256;
        dfa = new int[R][finalCondition];
        dfa[pat.charAt(0)][0] = 1;
        for (int X = 0, j = 1; j < finalCondition; j++) {
            for (int c = 0; c < R; c++)
                dfa[c][j] = dfa[c][X];
            dfa[pat.charAt(j)][j] = j + 1;
            X = dfa[pat.charAt(j)][X];
        }
    }

    public boolean isSubstringFound(int symbol) {
        if (finalCondition == 0) return false;

        currentCondition = dfa[symbol][currentCondition];
        if (currentCondition == finalCondition) {
            currentCondition = 0;
            return true;
        }
        return false;
    }

}
