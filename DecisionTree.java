import java.util.*;
import java.util.stream.*;
public class DecisionTree {
    private Node root;
    private int numFeatures;
    private List<String> colName;
    private EncodeData EncodeData;
    DecisionTree(){
        this.root = new Node(null);
        this.EncodeData = null;
        this.colName = null;
        this.numFeatures = 0;
    }
    public void fit(DataFrame data){
        this.colName = data.columns();
        fit(data.getData());
    }
    public void fit(List<List<Object>> data){
        List<Object> target = new ArrayList<>();
        int targetIndex = this.colName.contains("target") ? this.colName.indexOf("target") : data.get(0).size()-1;
        for(int i = 0; i < data.size(); i++){
            target.add(data.get(i).get(targetIndex));
            data.get(i).remove(targetIndex);
        }
        for(int i = 0; i < data.get(0).size(); i++){
            if(!(data.get(0).get(i).getClass() == double.class)){
                if(EncodeData==null) EncodeData = new EncodeData();
                int[] encoded = encodeFeature(data, i);
                for(int j = 0; j < data.size(); j++){
                    data.get(j).set(i, encoded[j]);
                }
            }
        }
        this.root = build(data, target);
    }
    @SuppressWarnings("unchecked")
    public void fit(List<List<Object>> data, List<Object> target){
        List<double[]> features = (List<double[]>) convertType(data);
        List<String> labels = (List<String>) convertType(target);
        this.numFeatures = data.get(0).size();
        this.root = build(features, labels);
    }
    private Node build(List<List<Object>> data, List<Object> target){
        Set<String> uniqueLabels = new HashSet<>(labels);
        if (uniqueLabels.size() == 1) {
            return new Node(labels.get(0));
        }
        if (features.isEmpty() || features.get(0) == null || features.get(0).length == 0) {
            String majorityLabel = getMajorityLabel(labels);
            return new Node(majorityLabel);
        }
        int bestFeatureIndex = 0;
        double bestThreshold = features.get(0)[bestFeatureIndex];
        double bestGini = Double.MAX_VALUE;

        for (int featureIndex = 0; featureIndex < features.get(0).length; featureIndex++) {
            double threshold = features.get(0)[featureIndex]; // choosing first value as threshold

            double gini = Gini(featureIndex, threshold, features, labels);

            if (gini < bestGini) {
                bestGini = gini;
                bestFeatureIndex = featureIndex;
                bestThreshold = threshold;
            }
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
            String majorityLabel = getMajorityLabel(labels);  // Use the current node's labels
            return new Node(majorityLabel);
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
    private String getMajorityLabel(List<String> labels) {
        Map<String, Long> labelCounts = labels.stream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    
        // Find the label with the highest count (majority)
        return labelCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);  // Handle cases where no label is available
    }
    private double Gini(int featureIndex, double threshold, List<double[]> features, List<String> labels) {
        List<String> leftLabels = new ArrayList<>();
        List<String> rightLabels = new ArrayList<>();
    
        for (int i = 0; i < features.size(); i++) {
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
            double probability = count / totalLabels;
            gini -= probability * probability;
        }
        return gini;
    }
    @SuppressWarnings("unchecked")
    private Object convertType(List<?> data) {
        if(data.isEmpty())
        return null;
        if (!(data.get(0) instanceof List)) {
            return data.stream().map(Object::toString).collect(Collectors.toList());
        } else {
            List<double[]> result = new ArrayList<>();
            List<List<Object>> newData = (List<List<Object>>) (List<?>) data;
            for(int i = 0; i < newData.size(); i++){
                result.add(new double[(newData.get(i)).size()]);
                for(int j = 0; j < newData.get(i).size(); j++){
                    //if(newData.get(i).get(j).getClass() == double.class)
                        result.get(i)[j] = Double.parseDouble(newData.get(i).get(j).toString());
                    //else return null;
                }
            }
            return result;
        }
    }
    private int[] encodeFeature(List<List<Object>> fulldata, int index){
        int[] encoded = new int[fulldata.size()];
        int k = 1;
        List<String> feature = new ArrayList<>();
        for(int i = 0; i < fulldata.size(); i++){
            feature.add(fulldata.get(i).get(index).toString());
        }
        Set<String> unqele = new HashSet<>(feature);
        for(String ele : unqele){
            for(int j = 0; j < fulldata.size(); j++){
                if(ele.equals(fulldata.get(j).get(index).toString())){
                    encoded[k] = k;
                }
            }
            k++;
        }
        String Key = this.colName == null ? "Column"+index : this.colName.get(index);
        EncodeData.newMap(Key);
        EncodeData.addData(Key, (String[]) unqele.toArray(), encoded);
        return encoded;
    }
    private List<Object> encodeInput(List<Object> input){
        for(int i = 0; i < input.size(); i++){
            if(input.get(i).getClass()!=double.class){
                String col = this.colName == null ? "Column"+i : this.colName.get(i);
                input.set(i, (Object)EncodeData.getData(col, (String)input.get(i)));
            }
        }
        return input;
    }
    public String predict(List<Object> input){
        input = encodeInput(input);
        double[] inputArr = new double[input.size()];
        for(int i = 0; i < input.size(); i++){
            if(!(input.get(i) instanceof Double))
                return null;
            inputArr[i] = (double) input.get(i);
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
}
