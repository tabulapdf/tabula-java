package technology.tabula;

public class CloneFactoryTabula {
    public TabulaInterface makeDuplicate(TabulaInterface tabulaInterface){
        return tabulaInterface.newDuplicate();
    }
}
