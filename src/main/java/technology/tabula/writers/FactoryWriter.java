package technology.tabula.writers;

public class FactoryWriter {
    public Writer getInstance(String string){
        if(string=="JsonWriter"){
            return new JSONWriter();
        }
        if (string == "CSVWriter"){
            return new CSVWriter();
        }
        return null;
    }
}
