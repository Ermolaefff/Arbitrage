package MONEYpackage.Parser.NumberTransformers;

public class DoubleTransformer implements NumberTransformer {
    public Object transform(String doubleStr) {
        if (doubleStr.length() == 0) return new Double(0);
        for (int i = 0; i < doubleStr.length(); i++) {
            char c = doubleStr.charAt(i);
            if (c != 'e' && (c < ',' || c > '9')) return new Double(0);
        }
       return Double.parseDouble(doubleStr);
    }

    public static void main(String[] args) {
        DoubleTransformer transformer = new DoubleTransformer();
        System.out.println(transformer.transform("3.71e-6"));
    }
}
