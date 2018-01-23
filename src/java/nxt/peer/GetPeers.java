/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.peer;

import java.util.List;

final class GetPeers {

    private GetPeers() {}

    /**
     * Process the GetPeers message and return the AddPeers message
     *
     * @param   peer                    Peer
     * @param   request                 Request message
     * @return                          Response message
     */
    static NetworkMessage processRequest(PeerImpl peer, NetworkMessage.GetPeersMessage request) {
        List<Peer> peerList = Peers.getPeers(p -> !p.isBlacklisted()
                        && p.getState() == Peer.State.CONNECTED
                        && p.getAnnouncedAddress() != null
                        && p.shareAddress()
                        && !p.getAnnouncedAddress().equals(peer.getAnnouncedAddress()),
                    NetworkMessage.MAX_LIST_SIZE);
        if (!peerList.isEmpty()) {
            peer.sendMessage(new NetworkMessage.AddPeersMessage(peerList));
        }
        return null;
    }
}
