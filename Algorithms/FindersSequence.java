package MONEYpackage.Algorithms;

import MONEYpackage.Enums.ParsingFields;
import MONEYpackage.Parser.NumberTransformers.NumberTransformer;

import java.util.Arrays;

public class FindersSequence {
    private final SubstringFinder[] finders;
    private final ParsingFields[] fieldsMarkers;
    private final NumberTransformer[] numberTransformers;
    private final char[] separators;
    private final Object[] fields;
    private int currentFieldIdx = 0;
    private final int size;
    private boolean finish = false;

    public FindersSequence(int size) {
        this.size = size;

        finders = new SubstringFinder[size];
        separators = new char[size];
        fieldsMarkers = new ParsingFields[size];
        numberTransformers = new NumberTransformer[size];

        fields = new Object[ParsingFields.values().length];
    }

    public void refreshFields() {
        currentFieldIdx = 0;
        finish = false;
        Arrays.fill(fields, null);
    }

    public void addFinder(ParsingFields field, String substring, char separator, NumberTransformer transformer) {
        finders[currentFieldIdx] = new SubstringFinder(substring);
        separators[currentFieldIdx] = separator;
        fieldsMarkers[currentFieldIdx] = field;
        numberTransformers[currentFieldIdx] = transformer;
        if (++currentFieldIdx == size) currentFieldIdx = 0;
    }

    public void printFieldsInfo() {
        for(ParsingFields fieldIdx : ParsingFields.values()) {
            if (fields[fieldIdx.ordinal()] != null)
                System.out.println(fieldIdx + " " + fields[fieldIdx.ordinal()].toString());
        }
    }

    public void addField(String field) {
        fields[fieldsMarkers[currentFieldIdx].ordinal()] = numberTransformers[currentFieldIdx] == null ?
                field : numberTransformers[currentFieldIdx].transform(field);
    }

    public Object getField(ParsingFields parsingField) {
        return fields[parsingField.ordinal()];
    }

    public Object[] getAllFields() {
        return fields.clone();
    }

    public boolean isSubstringFound(int symbol) {
        if (currentFieldIdx != 0 && finders[0].isSubstringFound(symbol)) //////////////////Если пройдет полный цикл полей////////////////////////<-исправить
            refreshFields();
        return finders[currentFieldIdx].isSubstringFound(symbol);
    }

    public char getCurrentSeparator() {
        return separators[currentFieldIdx];
    }

    public void switchToTheNextFinder() {
        finish = (++currentFieldIdx == size);
        if (finish) currentFieldIdx = 0;
    }

    public boolean isFinish() {
        return finish;
    }
}
