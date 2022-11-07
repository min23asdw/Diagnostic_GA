

import java.util.ArrayList;
import java.util.Random;
public class genetic {

    String[] mlp;
    int num_population;
    int maxGeGeneration;
    Random r = new Random();
    individual[] population_pool;


    ArrayList<Double[]> train_dataset;
    ArrayList<Double[]> train_desired_data;


    ArrayList<Double[]> Result = new ArrayList<>();

    ArrayList< Pair<Double , individual>> scoreBoard = new ArrayList<>();

    double prob_mul = 0.01;
    double prob_parent = 0.6;

    double best_mlp_score = 0.0;

    double fx_min = Double.MAX_VALUE;



    public genetic( String _mlp , int _population , int _maxGeneration){
        this.mlp = _mlp.split(",");;
        this.num_population = _population;
        this.maxGeGeneration = _maxGeneration;



    }

    public void settraindata(ArrayList<Double[]> _train_dataset, ArrayList<Double[]> _train_desired_data) {
        this.train_dataset = _train_dataset;
        this.train_desired_data = _train_desired_data;
    }

    public void  init_population(){
        individual[] init =  new individual[num_population];
        for(int indi = 0 ; indi < num_population ; indi++){
            individual indiler = new individual( "30,5,2" ,0 );
            init[indi] = indiler;
        }
        this.population_pool = init;


    }


    public void run_gen(){

        init_population();

        for (int gen = 0 ; gen < maxGeGeneration ; gen++){

            Double[] result =  population_eval();
            Result.add(result);
            ArrayList<individual> offspring_pool =  p1();
              p2(offspring_pool);
              //TODO
              add2N(offspring_pool);

              if(gen != maxGeGeneration-1)  move2next_population(offspring_pool);

        }

        System.out.println("test");


    }



    public Double[] population_eval(){
        double sum_fit = 0;
        double avg_fit;
        double max_fit = -9999999;

        //eval fitness
        for (individual mlp : population_pool) {
            double error_mlp =   mlp.eval(train_dataset , train_desired_data);


            double fitness = scaling(error_mlp);
            if(max_fit<fitness){
                max_fit = fitness;
            }


            sum_fit += fitness;

            if(best_mlp_score < fitness){
                best_mlp_score = fitness;
                Pair<Double , individual> score_individual = new Pair(fitness, mlp);
                scoreBoard.add(score_individual);
            }



        }
        avg_fit = sum_fit / num_population;
        return new Double[]{sum_fit, avg_fit, max_fit};
    }

    public double scaling(double error){
        return 1/(error + 0.01)  ;
    }


    public  ArrayList<individual>  p1(){
        ArrayList<individual> offspring_pool = new ArrayList<>();
        //pair selection
//        unique_random  uq = new unique_random(train_dataset.size());
        for(int ran = 0 ; ran < num_population/2 ; ran++){
            int select1 = r.nextInt(0,49);
            int select2 =r.nextInt(0,49);

            individual offspring =  crossover(population_pool[select1], population_pool[select2]);
            offspring_pool.add(offspring);
        }
        return offspring_pool;
    }


    public individual crossover(individual f , individual m){
        Matrix[] father_weight = f.get_weight();
        Matrix[] mother_weight = m.get_weight();

        individual offspring = new individual( "30,5,2" ,0 );

        Matrix[] offspring_weight = newWeight(f);
        for (int layer = 0 ; layer < father_weight.length ; layer++ ) {
            for (int node = 0; node < father_weight[layer].rows; node++) {
                double q = uniform_random(0.0,1.0);
                if (q < prob_parent) {  // เอาจากพ่อ
                    offspring_weight[layer].data[node] = father_weight[layer].data[node].clone();
                } else {  //เอาจากแม่
                    offspring_weight[layer].data[node] = mother_weight[layer].data[node].clone();
                }
            }
        }
        offspring.set_weight(offspring_weight);

        return offspring;
    }

    public Matrix[] newWeight(individual blueprint){
        Matrix[] offsprong_weight = new Matrix[blueprint.neural_type.length-1];
        for (int layer = 0; layer < offsprong_weight.length; layer++) {
            Matrix weight = new Matrix(blueprint.neural_type[layer+1],blueprint.neural_type[layer] ,false);
            offsprong_weight[layer] = weight;
        }
        return offsprong_weight;
    }

    public void p2(ArrayList<individual> offspring_pool){
        for (individual offspring:offspring_pool) {
            Matrix[] nodeofchild = offspring.get_weight();
              for (int layer = 0 ; layer < nodeofchild.length ; layer++ ){
                  for (int node = 0 ; node < nodeofchild[layer].rows ; node++) {
                        //            for each non-input node
                      double q = uniform_random(0.0,1.0);

                      if( q < prob_mul){
//                          System.out.println("Mualtation!!");
                          multation(offspring,layer,node);
                      }
                  }
              }
        }

    }


    public void multation(individual offspring , int layer , int node){
        Matrix[] a = offspring.get_weight();
        for (int weightline = 0 ; weightline < a[layer].cols ; weightline ++ ){
            double e = uniform_random(-1.0,1.0);
            offspring.add_weight(layer,node,weightline , e);
        }
    }


    public void add2N(ArrayList<individual> offspring_pool ){
        while(offspring_pool.size() < num_population){
            int pick = r.nextInt(0,num_population-1);
            individual copy = population_pool[pick].clone();

            offspring_pool.add(copy);
        }

    }

    public void move2next_population(ArrayList<individual> offspring_pool) {
        int i = 0 ;
        for (individual offspring : offspring_pool) {
            population_pool[i] =  offspring.clone();
            i++;
        }
    }

    public double uniform_random(double rangeMin , double rangeMax ){
        return rangeMin + (rangeMax - rangeMin) * r.nextDouble();
    }



}
