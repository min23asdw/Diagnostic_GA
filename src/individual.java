import java.util.ArrayList;

public class individual  implements Cloneable {
    public final int[] neural_type;

    //sum of squared error at iteration n (sse)
    private final ArrayList<Double[]> error_n = new ArrayList<>();
    public double avg_error_n  ; // average sse of all epoch
    private double biases; // threshold connected : biases

    private Matrix[] layer_weight  ;
//    private Matrix[] change_weight;

    private Double[][]  node  ;
//    private Double[][]  local_gradient_node  ;
  // TODO    30 - xxx - 2
    public individual(   String _neural_type ,double _biases) {

        String[] splitArray = _neural_type.split(",");
        int[] array = new int[splitArray.length];
        for (int i = 0; i < splitArray.length; i++) array[i] = Integer.parseInt(splitArray[i]);

        this.neural_type = array;

        init_Structor();

        this.biases = _biases;

    }
    private void init_Structor(){
        node = new Double[neural_type.length][];
        for (int i = 0; i < neural_type.length; i++) {
            node[i] = new Double[neural_type[i]];
        }

        layer_weight = new Matrix[neural_type.length-1];
//        change_weight = new Matrix[neural_type.length-1];
        for (int layer = 0; layer < layer_weight.length; layer++) {
            Matrix weight = new Matrix(neural_type[layer+1],neural_type[layer] ,true);
//            Matrix change = new Matrix(neural_type[layer+1],neural_type[layer] ,false);
            layer_weight[layer] = weight;
//            change_weight[layer] = change;
        }
    }

    public void test(ArrayList<Double[]> _test_dataset,ArrayList<Double[]> _test_desired_data){
        //setup input data
        double t_p =0;
        double t_n =0;
        double f_p =0;
        double f_n =0;
        for(int test_i = 0; test_i < _test_dataset.size()-1 ; test_i++) {

            //set dataset value to input node
            for (int input_i = 0; input_i < neural_type[0]; input_i++) {
                node[0][input_i] = _test_dataset.get(test_i)[input_i];
            }
            forward_pass();

            // class set
            if(activation_fn(node[node.length-1][0])  > activation_fn(node[node.length-1][1])){
                node[node.length-1][0] = 1.0;
                node[node.length-1][1] = 0.0;
            }else {
                node[node.length-1][0] = 0.0;
                node[node.length-1][1] = 1.0;
            }
//            System.out.println("get");
//                for (Double val : node[node.length-1]) {
//                    System.out.println(val);
//                }
//                System.out.println("desired");
//                for (Double val : _test_desired_data.get(test_i)) {
//                    System.out.println(val);
//                }
            if(node[node.length-1][0].equals(_test_desired_data.get(test_i)[0]) && node[node.length-1][0].equals(1.0) ) t_p++;
            if(node[node.length-1][0].equals(_test_desired_data.get(test_i)[0]) && node[node.length-1][0].equals(0.0)  ) t_n++;

            if(!node[node.length-1][0].equals(_test_desired_data.get(test_i)[0])  && node[node.length-1][0].equals(1.0)  ) f_p++;
            if(!node[node.length-1][0].equals(_test_desired_data.get(test_i)[0])  && node[node.length-1][0].equals(0.0)  ) f_n++;
        }
        // t_p       t_n    f_p   f_n
        System.out.println(t_p+"\t"+t_n+"\t"+f_p+"\t"+f_n);
        System.out.println( t_p/(t_p+f_p) +"\t"+  t_p/(t_p+f_n) +"\t"+  (t_p+t_n)/(t_p+t_n+f_p+f_n)) ;
        error_n.clear();
    }
    public double eval(ArrayList<Double[]> _train_dataset, ArrayList<Double[]> _train_desired_data  ){

            unique_random  uq = new unique_random(_train_dataset.size());
            for(int data = 0; data < _train_dataset.size() ; data++) {
                //random  one dataset
                int ran_dataset_i = uq.get_num();
                //setup dataset value to input node
                for(int input_i = 0 ; input_i < neural_type[0] ; input_i ++){
                    node[0][input_i] = _train_dataset.get(ran_dataset_i)[input_i];
                }

                //cal ∑(input x weight) -> activation_Fn  for each neuron_node
                forward_pass();

                get_error(_train_desired_data.get(ran_dataset_i));
            }


            double sum = 0.0;
            for (Double[] doubles : error_n) {
                // ∑E(n) = 1/2 ∑ e^2   : sum of squared error at iteration n (sse)
                double error_output = 0.0;
                for (double error:doubles) {

                    error_output += Math.pow(error, 2);
                }
                sum += error_output;
            }
            // avg_E(n) = 1/N ∑ E(n)  : avg (sse)
            avg_error_n =  sum / (error_n.size());

//            error_n.clear();


        //TODO return avg_error_n
        return avg_error_n;

    }

    private void forward_pass(){
        for(int layer = 0; layer < neural_type.length-1 ; layer++) {

            // W r_c X N r_1 = N+1 r_1
            if(   layer_weight[layer].cols != node[layer].length){
                System.out.println("invalid matrix");
                return;
            }

            double  sum_input;
            Double[] sum_inputnode = new Double[neural_type[layer+1]];

            //mutiply matrix
            for (int j = 0; j < neural_type[layer+1] ; j++){
                double sum=0;
                for(int k=0;k<node[layer].length;k++)
                {
                    //w_ji : weight from input neuron j to neron i : in each layer
                    sum += layer_weight[layer].data[j][k]  *  activation_fn( node[layer][k])  ;
                }
                // V_j = sum all input*weight i->j + biases
                sum_input = sum + biases;
                sum_inputnode[j] = sum_input;
            }
            // O_k  =  output of neuron_node k in each layer
            node[layer+1] = sum_inputnode;

        }
    }

    private void get_error(Double[] desired_data) {

        int number_outputn_node  =   node[node.length-1].length;
        Double[] errors = new Double[number_outputn_node];
        for ( int outnode_j = 0 ; outnode_j < number_outputn_node ; outnode_j++) {
            //train_desired_data => d_j desired output for neuron_node j at iteration N // it have "one data"
            //e_j  = error at neuron j at iteration N
            double desired = desired_data[outnode_j];
            double getOutput =  activation_fn( node[node.length-1][outnode_j] )  ;
            errors[outnode_j] =  desired - getOutput  ;

        }
        error_n.add(errors);
    }

    public  Matrix[] get_weight(){
        return  layer_weight;
    }
    public  void set_weight(Matrix[] _newWeight){
        this.layer_weight = _newWeight;
    }

    public void add_weight(int layer , int node , int column , double value){
        layer_weight[layer].add(node,column,value);
    }


    public double activation_fn(Double x){
        //TODO
        return Math.max(0.01,x);  // leak relu
//        return (Math.exp(x)-Math.exp(-x))/(Math.exp(x)+Math.exp(-x)); // Tanh
//        return 1.0 / (1.0 + Math.exp(-x)); //sigmoid
//         return  x; // linear
    }

    @Override
    public individual clone() {
        try {
            individual clone = (individual) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
