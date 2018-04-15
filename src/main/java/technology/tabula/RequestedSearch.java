package technology.tabula;

class RequestedSearch{

    String _keyBeforeTable;
    Boolean _includeKeyBeforeTable;
    String _keyAfterTable;
    Boolean _includeKeyAfterTable;

    public RequestedSearch(String keyBeforeTable, Boolean includeKeyBeforeTable,
                           String keyAfterTable, Boolean includeKeyAfterTable) {
        _keyBeforeTable = keyBeforeTable;
        _includeKeyBeforeTable = includeKeyBeforeTable;

        _keyAfterTable = keyAfterTable;
        _includeKeyAfterTable = includeKeyAfterTable;
    }
}
