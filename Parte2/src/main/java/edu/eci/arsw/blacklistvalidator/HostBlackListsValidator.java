/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int n){
        
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        
        int ocurrencesCount=0;
        
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();

        int totalServers=skds.getRegisteredServersCount();
        //Reutilizamos lo que hizimos en el ejercicio pasado
        int A=0;
        int B=totalServers-1;
        int total=B-A+1;
        int chunk=total/n;
        int remainder=total%n;

        SearchThread[] threads=new SearchThread[n];
        int start=A;

        for (int i=0;i<n;i++){

            int extra=(i<remainder) ? 1 : 0;
            int end=start+chunk-1+extra;

            threads[i]=new SearchThread(start, end, ipaddress);
            threads[i].start();

            start=end+1;
        }

        //se espera que los hilos terminen usando el join
        for (int i=0;i<n;i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, e.getMessage());
            }
        }
        //aqui sumamos las listas de cada hilo
        int checkedListsCount=0;
        for (int i=0;i<n;i++){
            ocurrencesCount+=threads[i].getOcurrencesCount();
            blackListOcurrences.addAll(threads[i].getBlackListOcurrences());
            checkedListsCount+=threads[i].getCheckedListsCountThread();
        }

        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, totalServers});

        return blackListOcurrences;
    }


    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());



}
