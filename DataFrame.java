import java.util.*;
import java.io.*;

class DataFrame {
    private List<String> columns;
    private List<String> rows;
    private List<List<Object>> data;
    private List<Class <?>> colType;

    private boolean checkRowSize(List<Object> row){
        if(this.data.get(0).size()!=row.size() && !this.data.isEmpty())
            return false;
        for(int i=0;i<row.size();i++){
            if(detectType(row.get(i))!=this.colType.get(i)){
                return false;
            }
        }
        return true;
    }
    private Class<?> detectType(Object obj){
        return (obj != null) ? obj.getClass() : null;
    }
    private static void to_csv(List<List<Object>> fulldata, List<String> cols, String filepath)throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))){
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < cols.size(); i++){
                builder.append(cols.get(i).toString());
                if(i < cols.size() - 1)
                    builder.append(",");
            }
            writer.write(builder.toString());
            writer.newLine();
            for(List<Object> row : fulldata){
                StringBuilder nbuilder = new StringBuilder();
                for(int i = 0; i < row.size(); i++){
                    nbuilder.append(row.get(i).toString());
                    if(i < row.size() - 1)
                        nbuilder.append(",");
                }
                writer.write(nbuilder.toString());
                writer.newLine();
            }
        }
    }
    private static List<List<Object>> read_CSV(String filePath) throws IOException {
        List<List<Object>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                List<Object> row = new ArrayList<>();
                for (String value : values) {
                    row.add(value);
                }
                rows.add(row);
            }
        }
        return rows;
    }
    DataFrame() {
        this.columns = new ArrayList<>();
        this.data = new ArrayList<>();
        this.colType = new ArrayList<>();
    }
    public DataFrame(List<List<Object>> data) {
        this(data, generateDefaultColumnNames(data));
    }
    public DataFrame(List<List<Object>> data, List<String> columns) {
        this(data, null, columns);
    }
    public DataFrame(List<List<Object>> data, List<String> rows, List<String> columns) {
        if (data.isEmpty() || data.get(0).size() != columns.size()) {
            throw new IllegalArgumentException("Column size mismatch with data.");
        }

        this.data = data;
        this.columns = columns;
        this.rows = (rows != null && rows.size() == data.size()) ? rows : null;
        this.colType = new ArrayList<>();

        for (int i = 0; i < data.get(0).size(); i++) {
            Class<?> type = detectType(data.get(0).get(i));
            this.colType.add(type);
            for (int j = 0; j < data.size(); j++) {
                if (!type.isInstance(data.get(j).get(i))) {
                    throw new IllegalArgumentException("Inconsistent data type at row " + j + ", column " + i);
                }
            }
        }
    }
    private static List<String> generateDefaultColumnNames(List<List<Object>> data) {
        List<String> cols = new ArrayList<>();
        for (int i = 0; i < data.get(0).size(); i++) {
            cols.add(String.valueOf(i));
        }
        return cols;
    }
    void newRow(List<Object> rowData) {
        if(!checkRowSize(rowData)){
            return;
        }
        this.data.add(rowData);
        if(!this.rows.isEmpty()){
            this.rows.add((data.size()-1)+"");
        }
    }
    void newRow(String rowName, List<Object> rowData) {
        if(!checkRowSize(rowData)){
            return;
        }
        this.rows.add(rowName);
        this.data.add(rowData);
    }

    void nameRows(List<String> rows) {
        if(rows.size()!=this.data.size()){
            return;
        }
        for(int i=0;i<rows.size();i++)
            this.rows.add(rows.get(i));
    }
    List<String> columns(){
        return this.columns;
    }
    public void newColumn(String columnName, List<Object>  columns) {
        if(columns.size()!=this.data.size())
            return;
        this.columns.add(columnName);
        this.colType.add(detectType(columns.get(0)));
        for(int i=0;i<this.data.size();i++){
            data.get(i).add(columns.get(i));
        }
    }
    void newColumn(List<Object>  columns) {
        if(columns.size()!=this.data.size())
            return;
        this.columns.add("Column"+this.columns.size());
        this.colType.add(detectType(columns.get(0)));
        for(int i=0;i<this.data.size();i++){
            data.get(i).add(columns.get(i));
        }
    }
    void renameColumns(List<String>  columns) {
        if(columns.size()!=this.columns.size())
            return;
        this.columns = columns;
    }
    void newEmptyColumn(String columnName) {
        columns.add(columnName);
        for(int i=0;i<this.data.size();i++){
            data.get(i).add(null);
        }
        this.colType.add(detectType(columns.get(0)));
    }
    void read_csv(String filepath)throws IOException{
        this.data = read_CSV(filepath);
        for(int i = 0; i < this.data.get(0).size(); i++)
            this.columns.add((String)this.data.get(0).get(i));
        this.data.remove(0);
        read_colTpye();
    }
    private void read_colTpye() {
        this.colType = new ArrayList<>();

        for (int i = 0; i < this.data.get(0).size(); i++) {
            Class<?> type = detectType(this.data.get(0).get(i));
            this.colType.add(type);
            for (int j = 0; j < this.data.size(); j++) {
                if (!type.isInstance(this.data.get(j).get(i))) {
                    throw new IllegalArgumentException("Inconsistent data type at row " + j + ", column " + i);
                }
            }
        }
    }
    void to_csv(String filepath) throws IOException{
        to_csv(this.data, this.columns, filepath);
    }
    int count(){
        return this.data.size();
    }
    List<List<Object>> getData(){
        return this.data;
    }
    List<List<Object>> head(){
        List<List<Object>> head = new ArrayList<>();
        for(int i=0;i<5;i++)
            head.add(this.data.get(i));
        return head;
    }
    List<List<Object>> head(int n){
        if(n<=0 || n>this.data.size())
            return null;
        if(n==this.data.size())
            return this.data;
        List<List<Object>> head = new ArrayList<>();
        for(int i=0;i<n;i++)
        head.add(this.data.get(i));
        return head;
    }
    List<List<Object>> tail(){
        List<List<Object>> tail = new ArrayList<>();
        for(int i=0;i<5;i++)
            tail.add(this.data.get(this.data.size()-5+i));
        return tail;
    }
    List<List<Object>> tail(int n){
        if(n<=0 || n>this.data.size())
            return null;
        if(n==this.data.size())
            return this.data;
        List<List<Object>> head = new ArrayList<>();
        for(int i=0;i<n;i++)
        head.add(this.data.get(this.data.size()-n+i));
        return head;
    }
    Object get(int row, int column) {
        if(this.data.size()<=row || this.columns.size()<=column){
            return null;
        }
        return this.data.get(row).get(column);
    }
    Object get(String row, String column){
        if(!this.rows.contains(row) || !this.columns.contains(column))
            return null;
        return this.data.get(this.rows.indexOf(row)).get(this.columns.indexOf(column));
    }
    Object get(int row, String column){
        if(this.data.size()<=row || !this.columns.contains(column))
            return null;
        return this.data.get(row).get(this.columns.indexOf(column));
    }
    Object get(String row, int column){
        if(!this.rows.contains(row) || this.columns.size()<=column)
            return null;
        return this.data.get(this.rows.indexOf(row)).get(column);
    }
    public void removeColumn(int colIndex){
        for(int i = 0; i < this.data.size(); i++){
            this.data.get(i).remove(colIndex);
        }
        this.colType.remove(colIndex);
        this.columns.remove(colIndex);
    }
    public void removeRow(int rowIndex){
        this.data.remove(rowIndex);
    }
    public List<Object> getColumn(int colIndex){
        List<Object> column = new ArrayList<>();
        for(int i = 0; i < this.data.size(); i++){
            column.add(this.data.get(i).get(colIndex));
        }
        return column;
    }
    public List<Object> getRow(int rowIndex){
        return this.data.get(rowIndex);
    }
    private void iterate(List<String> over, int size){
        for(int i=0;i<size;i++){
            System.out.print("\t"+over.get(i));
        }
        System.out.println();
    }
    private void iterate(int index, List<Object> over, int size){
        if(index>9)
        System.out.print(index+"\t");
        else
        System.out.print(index+" \t");
        for(int i=0;i<size;i++){
            System.out.print(over.get(i)+"\t");
        }
        System.out.println();
    }
    void display() {
        iterate(this.columns, this.columns.size());
        for(int i=0;i<this.data.size();i++)
        iterate(i, this.data.get(i), columns.size());
    }
}
