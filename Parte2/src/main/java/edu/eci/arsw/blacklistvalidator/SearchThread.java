package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

/**
 * Hilo encargado de revisar un rango de servidores de listas negras.
 * Todos los hilos comparten un SearchState para detener la búsqueda
 * cuando se alcanza el número global de ocurrencias requerido.
 */
public class SearchThread extends Thread {

    private final int startIndex;
    private final int endingIndex;
    private final String ipAddress;
    private final SearchState searchState;
    private final HostBlacklistsDataSourceFacade skds;

    public SearchThread(
            int startIndex,
            int endingIndex,
            String ipAddress,
            SearchState searchState
    ) {
        this.startIndex = startIndex;
        this.endingIndex = endingIndex;
        this.ipAddress = ipAddress;
        this.searchState = searchState;
        this.skds = HostBlacklistsDataSourceFacade.getInstance();
    }

    @Override
    public void run() {
        for (int i = startIndex; i <= endingIndex; i++) {

            if (!searchState.shouldContinue()) {
                break;
            }

            boolean found = skds.isInBlackListServer(
                    i,
                    ipAddress
            );

            searchState.registerCheckedList();

            if (found) {
                boolean continueSearching =
                        searchState.registerOccurrence(i);

                if (!continueSearching) {
                    break;
                }
            }
        }
    }
}