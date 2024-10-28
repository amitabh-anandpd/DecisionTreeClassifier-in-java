import java.util.*;
public class EncodeData{
    Map<String, Map<String, Integer>> data;
    EncodeData(){
        this.data = new HashMap<>();
    }
    void newMap(String key){
        this.data.put(key, new HashMap<>());
    }
    void addData(String key, String[] subKey, int[] k){
        for(int i = 0; i < subKey.length; i++)
        this.data.get(key).put(subKey[i], k[i]);
    }
    void addSingleData(String key, String subkey, int k){
        this.data.get(key).put(subkey, k);
    }
    int getData(String key, String subkey){
        return data.get(key).get(subkey);
    }
    Map<String, Map<String, Integer>> allData(){
        return this.data;
    }
    boolean checkSubkey(String key, String subkey){
        return data.get(key).containsKey(subkey);
    }
}
