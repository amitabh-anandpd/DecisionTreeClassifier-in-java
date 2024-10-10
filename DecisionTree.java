import java.util.*;
import java.util.stream.*;
public class DecisionTree {
    private Node root;
    BaseTree(){
        this.root = new Node(null);
    }
    public void fit(List<List<Object>> data){
        List<Object> target = new ArrayList<>();
        for(int i = 0; i < data.size(); i++){
            target.add(data.get(i).get(data.get(i).size()-1));
            data.get(i).remove(data.get(i).size()-1);
        }
        this.root = build(data, target);
    }
    public void fit(List<List<Object>> data, List<Object> target){
        this.root = build(data, target);
    }
    @SuppressWarnings("unchecked")
    private Node build(List<List<Object>> data, List<Object> target){
        List<double[]> features = (List<double[]>) convertType(data);
        List<String> labels = (List<String>) convertType(target);
        if (labels.stream().distinct().count() == 1)
            return new Node(labels.get(0));
    }
    private Object convertType(List<?> data) {
        if(data.isEmpty())
        return null;
        if (!(data.get(0) instanceof List)) {
            return data.stream().map(Object::toString).collect(Collectors.toList());
        } else {
            List<double[]> result = new ArrayList<>();
            for(int i = 0; i < data.size(); i++){
                result.add(new double[data.get(i).size()])
                for(int j = 0; j < data.get(i).size(); j++){
                    if(data.get(i).get(j).getClass()==double.class){
                        result.get(i)[j] = (double)data.get(i).get(j);
                    }
                    else return null;
                }
            }
            return result;
        }
    }
}
