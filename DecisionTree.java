import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

public class DecisionTree {
    private Node root;
    private int numFeatures;
    private List<String> colName;
    private EncodeData EncodeData;
    private double trainAccuracy;
    DecisionTree(){
        this.root = new Node(null);
        this.EncodeData = null;
        this.colName = null;
        this.numFeatures = 0;
        this.trainAccuracy = 0;
    }
    public void fit(DataFrame data, int targetIndex){
        this.colName = data.columns();
        List<Object> target = data.getColumn(targetIndex);
        data.removeColumn(targetIndex);
        fit(data.getData(), target);
    }
    public void fit(DataFrame data) {
        this.colName = data.columns();
        int targetIndex = -1;
        for (int i = 0; i < this.colName.size(); i++) {
            if (this.colName.get(i).trim().equalsIgnoreCase("target")) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1)
            targetIndex = data.getData().get(0).size() - 1;
    
        List<Object> target = data.getColumn(targetIndex);
        data.removeColumn(targetIndex);    
        fit(data.getData(), target);
    }
    
    public void fit(List<List<Object>> data){
        List<Object> target = new ArrayList<>();
        int targetIndex = this.colName.contains("target") ? this.colName.indexOf("target") : data.get(0).size() - 1;
        for(int i = 0; i < data.size(); i++){
            target.add(data.get(i).get(targetIndex));
            data.get(i).remove(targetIndex);
        }
        this.colName.remove(targetIndex);
        data = dataEncode(data);
        fit(data, target);
    }
    @SuppressWarnings("unchecked")
    public void fit(List<List<Object>> data, List<Object> target){
        data = dataEncode(data);
        List<double[]> features = (List<double[]>) convertType(data);
        List<String> labels = (List<String>) convertType(target);
        if(labels.size() != features.size()){
            System.out.println(labels.size());
            System.out.println(features.size());
            System.out.println("Null values present!!!");
            return;
        }
        this.numFeatures = data.get(0).size();
        this.root = build(features, labels);
        this.trainAccuracy = accuracy(data, target);
    }
    private List<List<Object>> dataEncode(List<List<Object>> data){
        for(int i = 0; i < data.get(0).size(); i++){
            boolean canParse = false;
            try {
                Integer.parseInt(data.get(0).get(i).toString());
                canParse = true;
                try{
                    Double.parseDouble(data.get(0).get(i).toString());
                }
                catch (NumberFormatException e) {
                    canParse = false;
                }
            } catch (NumberFormatException e) {
                canParse = false;
            }
            
            if (canParse) {
                continue;
            }
            int[] encoded = encodeFeature(data, i);
            for(int j = 0; j < data.size(); j++){
                data.get(j).set(i, encoded[j]);
            }
        }
        return data;
    }
    private String getMajorityLabel(List<String> labels) {
        Map<String, Long> labelCounts = labels.stream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return labelCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null); 
    }
    private Node build(List<double[]> features, List<String> labels){
        Set<String> uniqueLabels = new HashSet<>(labels);
        if (uniqueLabels.size() == 1) {
            return new Node(labels.get(0));
        }
        if (features.isEmpty() || features.get(0) == null || features.get(0).length == 0) {
            return new Node(getMajorityLabel(labels));
        }
        int bestFeatureIndex = -1;
        double bestThreshold = 0;
        double bestGini = Double.MAX_VALUE;

        for (int featureIndex = 0; featureIndex < features.get(0).length; featureIndex++) {
            Set<Double> thresholds = new HashSet<>();
            for (double[] row : features) {
                thresholds.add(row[featureIndex]);
            }
            
            for (double threshold : thresholds) {
                double gini = Gini(featureIndex, threshold, features, labels);
                if (gini < bestGini) {
                    bestGini = gini;
                    bestFeatureIndex = featureIndex;
                    bestThreshold = threshold;
                }
            }
        }
        if (bestFeatureIndex == -1) {
            return new Node(getMajorityLabel(labels));
        }
        List<double[]> leftSplit = new ArrayList<>();
        List<double[]> rightSplit = new ArrayList<>();
        List<String> leftLabels = new ArrayList<>();
        List<String> rightLabels = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            if (features.get(i)[bestFeatureIndex] <= bestThreshold) {
                leftSplit.add(features.get(i));
                leftLabels.add(labels.get(i));
            } else {
                rightSplit.add(features.get(i));
                rightLabels.add(labels.get(i));
            }
        }
        if (leftSplit.isEmpty() || rightSplit.isEmpty()) {
            return new Node(getMajorityLabel(labels));
        }
        Node node = new Node(bestFeatureIndex, bestThreshold);
        try{
            if(!leftSplit.isEmpty() && !leftLabels.isEmpty())
            node.left = build(leftSplit, leftLabels);
            if(!rightSplit.isEmpty() && !rightLabels.isEmpty())
            node.right = build(rightSplit, rightLabels);
        } catch (Exception e){
            System.out.println("Error: " + e.getMessage());
            return null;
        }
        return node;
    }
    private double Gini(int featureIndex, double threshold, List<double[]> features, List<String> labels) {
        List<String> leftLabels = new ArrayList<>();
        List<String> rightLabels = new ArrayList<>();
    
        for (int i = 0; i < labels.size(); i++) {
            if (features.get(i)[featureIndex] <= threshold) {
                leftLabels.add(labels.get(i));
            } else {
                rightLabels.add(labels.get(i));
            }
        }
    
        double giniLeft = GiniForLabels(leftLabels);
        double giniRight = GiniForLabels(rightLabels);
    
        double weightLeft = (double) leftLabels.size() / labels.size();
        double weightRight = (double) rightLabels.size() / labels.size();
    
        return weightLeft * giniLeft + weightRight * giniRight;
    }    
    private double GiniForLabels(List<String> labels) {
        if (labels.isEmpty()) return 0;
        Map<String, Long> labelCounts = labels.stream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        double gini = 1.0;
        double totalLabels = labels.size();

        for (long count : labelCounts.values()) {
            double probability = (double) count / totalLabels;
            gini -= probability * probability;
        }
        return gini;
    }
    @SuppressWarnings("unchecked")
    private Object convertType(List<?> data) {
        if (data.isEmpty()) return null;

        if (!(data.get(0) instanceof List)) {
            return data.stream().map(Object::toString).collect(Collectors.toList());
        } else {
            List<double[]> result = new ArrayList<>();
            List<List<Object>> newData = (List<List<Object>>) (List<?>) data;
            for (List<Object> row : newData) {
                double[] featureArray = new double[row.size()];
                for (int j = 0; j < row.size(); j++) {
                    try {
                        featureArray[j] = Double.parseDouble(row.get(j).toString());
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse value: " + row.get(j));
                        throw new IllegalArgumentException("Non-numeric value encountered: " + row.get(j), e);
                    }
                }
                result.add(featureArray);
            }
            return result;
        }
    }
    private int[] encodeFeature(List<List<Object>> data, int columnIndex) {
        String key = this.colName == null ? "Column" + columnIndex : this.colName.get(columnIndex);
        if(EncodeData==null){
            EncodeData = new EncodeData();
        }
        if ((EncodeData.allData().isEmpty()) || !EncodeData.allData().containsKey(key)) {
            EncodeData.newMap(key);
        }
        int[] values = new int[data.size()];
        int k = 0;
        for(int i = 0; i < data.size(); i++){
            if(!(EncodeData.allData().get(key).containsKey(data.get(i).get(columnIndex).toString()))){
                EncodeData.addSingleData(key, data.get(i).get(columnIndex).toString(), k++);
            }
            values[i] = EncodeData.getData(key, data.get(i).get(columnIndex).toString());
        }
        return values;
    }
    private List<Object> encodeInput(List<Object> input) {
        for (int i = 0; i < input.size(); i++) {
            Object value = input.get(i);
            try{
                Integer.parseInt(value.toString());
                continue;
            }
            catch (Exception e){
                //ignore
            }
            try{
                Double.parseDouble(value.toString());
                continue;
            }
            catch (Exception e){
                //ignore
            }
            input.set(i, EncodeData.getData(this.colName.get(i), input.get(i).toString()));
        }
        return input;
    }    
    
    public String predict(List<Object> input){
        List<Object> input2 = encodeInput(input);
        if(input.size()!=input2.size()){
            System.out.println("Encoded input size not equal to actual input size");
        }
        double[] inputArr = new double[input2.size()];
        for(int i = 0; i < input2.size(); i++){
            try{
                inputArr[i] = Double.parseDouble(input2.get(i).toString());
            }
            catch (Exception e){
                System.out.println("Error: "+e);
                return null;
            }
        }
        if(inputArr.length!=input2.size()){
            System.out.println("Size not equal 2");
        }
        return predict(inputArr);
    }
    public String predict(double[] input){
        if(input.length != this.numFeatures){
            return null;
        }
        Node node = this.root;
        while (!node.isLeaf()) {
            if (input[node.featureIndex] <= node.threshold) {
                node = node.left;
            } else {
                node = node.right;
            }
        }
        return node.label;
    }

    @SuppressWarnings("unchecked")
    public double accuracy(List<List<Object>> data, List<Object> target){
        List<double[]> features = (List<double[]>) convertType(data);
        List<String> labels = (List<String>) convertType(target);
        if(features.get(0).length != this.numFeatures)
            return 0;
        double correct = 0, wrong = 0;
        for(int i = 0; i < features.size(); i++){
            String pred = predict(features.get(i));
            if(pred.equals(labels.get(i)))
                correct+=1;
            else
                wrong+=1;
        }
        return correct/(correct+wrong);
    }
    public double train_score(){
        return this.trainAccuracy;
    }
}
