package dataanalysis;

/**
 * Main class to bring together the project.
 *
 * @author josephyearsley
 */
public class DataAnalysis {

    /**
     * Brings all the classes together to get a end result for analysis
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //To auto start mongodb
        final Boolean DEV_MODE = true;
        
        CommandHelpers helper;
        try {
            helper = new CommandHelpers();
            //Check if DB is open and start if in dev mode
            helper.startMongo(DEV_MODE);
            DataConvert dataConvert = new DataConvert();

            //Convert all data firstly
            dataConvert.convertCS();
            dataConvert.convertTW();
            dataConvert.closeMongoClient();
            Similarity s = new Similarity();
            s.similarityCalc();
            s.closeMongoClient();
            CosineSimilarity c = new CosineSimilarity("", "");
            c.consolidate();
            c.selfSim();
            c.diffSim();
            //Do self comparison of all tasks in subject
            c.selfSimAllSame();
            c.closeMongoClient();
            //New Cos to compare to self i.e task1,task1 and to say do diff between all tasks 
            c = new CosineSimilarity("WithSelf", "AllTasks");
            c.selfSim();
            c.diffSim();
            c.closeMongoClient();
            //Standard DTW
            DtwSimilarity d = new DtwSimilarity("", "", "");
            d.selfSim();
            d.diffSim();
            d.closeMongoClient();
            //New DTW to compare to self i.e task1,task1 and to say do diff between all tasks 
            d = new DtwSimilarity("WithSelf", "AllTasks", "");
            d.selfSim();
            d.diffSim();
            d.closeMongoClient();
            //New DTW Compare all Tasks to just subject not task defined
            d = new DtwSimilarity("", "", "SAT");
            d.selfSim();
            d.closeMongoClient();
            //Call Rscript to plot these graphs
            helper.graphSimilarites("Cosine Similarity", "eps", "Cosine Similarity", "cos", "4");
            helper.graphSimilarites("Wrong Cosine Similarity", "eps", "Wrong Cosine Similarity", "wcos", "4");
            helper.graphSimilarites("Dtw Similarity", "eps", "DTW Similarity", "dtw", "10");
            helper.graphSimilarites("Wrong Dtw Similarity", "eps", "Wrong DTW Similarity", "wdtw", "10");
            helper.graphSimilarites("Cosine Similarity with Extras", "eps", "Cosine Similarity with Extras", "ecos", "10");
            helper.graphSimilarites("Dtw Similarity with Extras", "eps", "DTW Similarity with Extras", "edtw", "10");
            //Close everything up
            helper.closeMongo(DEV_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
