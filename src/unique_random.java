import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class unique_random {
    int size;
    List<Integer> list_number;
    public unique_random(int size){
        list_number = new ArrayList<>();
        this.size = size;
        for (int i = 0; i < size; i++) {
            list_number.add(i);
        }
        Collections.shuffle(list_number);

    }
    public int get_num(){
        int temp = list_number.get(0);
        list_number.remove(0);
        return temp;
    }
}
