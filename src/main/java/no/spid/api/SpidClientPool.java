package no.spid.api;

import no.spid.api.client.SpidApiClient;

import java.util.LinkedList;
import java.util.List;

/**
 * This class creates & maintain clients pool with active connection to SPiD ID.
 *
 */
public class SpidClientPool {

    private final SpidApiClient.ClientBuilder clientBuilder;
    private int minimumConnectionNumber;
    private int maximumConnectionNumber;
    private List<SpidApiClient> clientList;
    private LinkedList<SpidApiClient> leasedList;

    /**
     * Creates new connection pool, with min connection number initialized connections.
     * @param minConnectionNumber
     * @param maxConnectionNumber
     */

    public SpidClientPool(int minConnectionNumber, int maxConnectionNumber, SpidApiClient.ClientBuilder clientBuilder)
    {
        if( minConnectionNumber>maxConnectionNumber ){
            throw new IllegalStateException("minConnectionNumber cannot be bigger than maxConnectionNumber");
        }
        if( minConnectionNumber <= 0 || maxConnectionNumber <=0 ){
            throw new IllegalStateException("minConnectionNumber nor maxConnectionNumber have to be grater than 0");
        }
        this.clientBuilder = clientBuilder;
        this.minimumConnectionNumber = minConnectionNumber;
        this.maximumConnectionNumber = maxConnectionNumber;

        initalizeConnections();
    }

    private void initalizeConnections() {
        clientList = new LinkedList<SpidApiClient>();
        leasedList = new LinkedList<SpidApiClient>();
        for(int i=0; i<minimumConnectionNumber; i++){
            clientList.add(clientBuilder.build());
        }
    }

    public SpidApiClient leaseClient(){
        SpidApiClient clientToLease = clientList.get(0);
        leasedList.add(clientToLease);
        return clientToLease;
    }

    public void returnClient(SpidApiClient client){
        leasedList.remove(client);
        clientList.add(client);

    }


}
