/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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

package nxt.shuffling;

import nxt.db.DbIterator;

import java.util.Arrays;

public enum ShufflingStage {
    REGISTRATION((byte) 0, new byte[]{1, 4}) {
        @Override
        byte[] getHash(ShufflingHome.Shuffling shuffling) {
            return shuffling.getFullHash();
        }
    },
    PROCESSING((byte) 1, new byte[]{2, 3, 4}) {
        @Override
        byte[] getHash(ShufflingHome.Shuffling shuffling) {
            if (shuffling.getAssigneeAccountId() == shuffling.getIssuerId()) {
                try (DbIterator<ShufflingParticipantHome.ShufflingParticipant> participants =
                             shuffling.getShufflingParticipantHome().getParticipants(shuffling.getFullHash())) {
                    return ShufflingHome.getParticipantsHash(participants);
                }
            } else {
                ShufflingParticipantHome.ShufflingParticipant participant = shuffling.getParticipant(shuffling.getAssigneeAccountId());
                return participant.getPreviousParticipant().getDataTransactionFullHash();
            }

        }
    },
    VERIFICATION((byte) 2, new byte[]{3, 4, 5}) {
        @Override
        byte[] getHash(ShufflingHome.Shuffling shuffling) {
            return shuffling.getLastParticipant().getDataTransactionFullHash();
        }
    },
    BLAME((byte) 3, new byte[]{4}) {
        @Override
        byte[] getHash(ShufflingHome.Shuffling shuffling) {
            return shuffling.getParticipant(shuffling.getAssigneeAccountId()).getDataTransactionFullHash();
        }
    },
    CANCELLED((byte) 4, new byte[]{}) {
        @Override
        byte[] getHash(ShufflingHome.Shuffling shuffling) {
            byte[] hash = shuffling.getLastParticipant().getDataTransactionFullHash();
            if (hash != null && hash.length > 0) {
                return hash;
            }
            try (DbIterator<ShufflingParticipantHome.ShufflingParticipant> participants =
                         shuffling.getShufflingParticipantHome().getParticipants(shuffling.getFullHash())) {
                return ShufflingHome.getParticipantsHash(participants);
            }
        }
    },
    DONE((byte) 5, new byte[]{}) {
        @Override
        byte[] getHash(ShufflingHome.Shuffling shuffling) {
            return shuffling.getLastParticipant().getDataTransactionFullHash();
        }
    };

    private final byte code;
    private final byte[] allowedNext;

    ShufflingStage(byte code, byte[] allowedNext) {
        this.code = code;
        this.allowedNext = allowedNext;
    }

    public static ShufflingStage get(byte code) {
        for (ShufflingStage stage : ShufflingStage.values()) {
            if (stage.code == code) {
                return stage;
            }
        }
        throw new IllegalArgumentException("No matching stage for " + code);
    }

    public byte getCode() {
        return code;
    }

    public boolean canBecome(ShufflingStage nextStage) {
        return Arrays.binarySearch(allowedNext, nextStage.code) >= 0;
    }

    abstract byte[] getHash(ShufflingHome.Shuffling shuffling);

}
