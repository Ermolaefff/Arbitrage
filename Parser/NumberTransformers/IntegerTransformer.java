package MONEYpackage.Parser.NumberTransformers;

public class IntegerTransformer implements NumberTransformer {
    public Object transform(String integerStr) {
        if (integerStr.length() == 0) new Integer(0);
        for (int i = 0; i < integerStr.length(); i++) {
            char c = integerStr.charAt(i);
            if (c < '0' || c > '9')
                return new Integer(0);
        }
        return Integer.parseInt(integerStr);
    }
}
