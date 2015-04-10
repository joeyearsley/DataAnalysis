
package dataanalysis;

/**
 * Main class to bring together the project.
 * @author josephyearsley
 */
public class DataAnalysis {

    /**
     * Brings all the classes together to get a end result fo analysis
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        //To auto start mongodb
        final Boolean DEV_MODE = true;
        
        CommandHelpers helper = new CommandHelpers();
        //Check if DB is open and start if in dev mode
        helper.startMongo(DEV_MODE);
        try {
            DataConvert dataConvert = new DataConvert();

            //Convert all data firstly
            dataConvert.convertCS();
            dataConvert.convertTW();
            Similarity s = new Similarity();
            s.similarityCalc();
            CosineSimilarity c = new CosineSimilarity("","");
            c.consolidate();
            c.selfSim();
            c.diffSim();
            //Do self comparison of all tasks in subject
            c.selfSimAllSame();
            //New Cos to compare to self i.e task1,task1 and to say do diff between all tasks 
            c = new CosineSimilarity("withSelf","allTasks");
            c.selfSim();
            c.diffSim();
            //Standard DTW
            DtwSimilarity d = new DtwSimilarity("","","");
            d.selfSim();
            d.diffSim();
            //New DTW to compare to self i.e task1,task1 and to say do diff between all tasks 
            d = new DtwSimilarity("withSelf","allTasks","");
            d.selfSim();
            d.diffSim();
            //New DTW Compare all Tasks to just subject not task defined
            d = new DtwSimilarity("","","sAT");
            d.selfSim();
        } catch (Exception mongoDB) {
            System.err.println(mongoDB);
            System.err.println("Ensure MongoDB is running & try again!");
        }
        
        //Call Rscript to plot these graphs
        helper.graphSimilarites("Cosine Similarity", "eps", "Cosine Similarity", "cos", "4");
        helper.graphSimilarites("Wrong Cosine Similarity", "eps", "Wrong Cosine Similarity", "wcos", "4");
        helper.graphSimilarites("Dtw Similarity", "eps", "DTW Similarity", "dtw", "10");
        helper.graphSimilarites("Wrong Dtw Similarity", "eps", "Wrong DTW Similarity", "wdtw", "10");
        //Close everything up
        helper.closeMongo(DEV_MODE);
        
    }
}
