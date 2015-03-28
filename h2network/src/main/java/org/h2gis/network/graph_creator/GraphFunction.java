package org.h2gis.network.graph_creator;

import org.h2gis.h2spatialapi.AbstractFunction;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Helper class for Graph Functions.
 *
 * @author Adam Gouge
 * @author Olivier Bonin
 */
public class GraphFunction extends AbstractFunction {

    public static final String ARG_ERROR  = "Unrecognized argument: ";

    /**
     * Return a JGraphT graph from the input edges table.
     *
     * @param connection  Connection
     * @param inputTable  Input table name
     * @param orientation Orientation string
     * @param weight      Weight column name, null for unweighted graphs
     * @param deadWeight
     * @return Graph
     */
    protected static KeyedGraph prepareGraph(Connection connection,
                                             String inputTable,
                                             String orientation,
                                             String weight,
                                             String deadWeight,
                                             Class vertexClass,
                                             Class edgeClass) throws SQLException {
        GraphFunctionParser parser = new GraphFunctionParser();
        //if(deadWeight != null){
            parser.parseWeightAndDeadWeightAndOrientation(orientation, weight, deadWeight);
        //}else {
        //    parser.parseWeightAndOrientation(orientation, weight);
        //}
        

        return new GraphCreator(connection,
                inputTable,
                parser.getGlobalOrientation(), parser.getEdgeOrientation(), parser.getWeightColumn(),
                parser.getDeadWeightColumn(),
                vertexClass,
                edgeClass).prepareGraph();
    }

    /**
     * Log the time elapsed from startTime until now.
     *
     * @param logger    Logger
     * @param startTime Start time in milliseconds
     */
    protected static void logTime(Logger logger, long startTime) {
        logger.info("    " + (System.currentTimeMillis() - startTime) / 1000. + " seconds");
    }
}
