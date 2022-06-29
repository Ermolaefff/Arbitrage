package MONEYpackage.Algorithms;

public class Finder {
    private final SubstringFinder finder;
    private final char separator;

    public Finder(String substring, char separator) {
       finder = new SubstringFinder(substring);
       this.separator = separator;
    }

    public boolean isSubstringFound(int symbol) {
        return finder.isSubstringFound(symbol);
    }

    public char getSeparator() {
        return separator;
    }
}
