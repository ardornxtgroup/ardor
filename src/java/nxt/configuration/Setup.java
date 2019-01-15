package nxt.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static nxt.configuration.SubSystem.ADDONS;
import static nxt.configuration.SubSystem.API_SERVER;
import static nxt.configuration.SubSystem.BLOCKCHAIN;
import static nxt.configuration.SubSystem.DB;
import static nxt.configuration.SubSystem.DESKTOP_WALLET;
import static nxt.configuration.SubSystem.LOGGER;
import static nxt.configuration.SubSystem.PEER_NETWORKING;
import static nxt.configuration.SubSystem.RANDOMIZATION;
import static nxt.configuration.SubSystem.SYSTEM_TRAY;
import static nxt.configuration.SubSystem.THREAD_POOL;

public interface Setup {

    List<SubSystem> initSequence();

    List<SubSystem> shutdownSequence();

    Setup FULL_NODE = new Setup() {

        // Init and shutdown sequence are based on the old Nxt.init() and Nxt.shutdown() sequence
        private List<SubSystem> initSequence = Arrays.asList(LOGGER, SYSTEM_TRAY, DB, BLOCKCHAIN, PEER_NETWORKING, ADDONS, THREAD_POOL, API_SERVER, RANDOMIZATION, DESKTOP_WALLET);
        private List<SubSystem> shutdownSequence = Arrays.asList(DESKTOP_WALLET, ADDONS, RANDOMIZATION, API_SERVER, THREAD_POOL, BLOCKCHAIN, PEER_NETWORKING, DB, LOGGER, SYSTEM_TRAY);

        @Override
        public List<SubSystem> initSequence() {
            return initSequence;
        }

        @Override
        public List<SubSystem> shutdownSequence() {
            return shutdownSequence;
        }
    };

    Setup COMMAND_LINE_TOOL = new Setup() {
        List<SubSystem> subSystems = Arrays.asList(LOGGER, DB, BLOCKCHAIN, THREAD_POOL, API_SERVER);

        @Override
        public List<SubSystem> initSequence() {
            return subSystems;
        }

        @Override
        public List<SubSystem> shutdownSequence() {
            List<SubSystem> reverseList = new ArrayList<>(subSystems);
            Collections.reverse(reverseList);
            return reverseList;
        }
    };

    Setup UNIT_TEST = new Setup() {
        private List<SubSystem> initSequence = Arrays.asList(LOGGER, DB, BLOCKCHAIN, PEER_NETWORKING, ADDONS, THREAD_POOL, API_SERVER, RANDOMIZATION);
        private List<SubSystem> shutdownSequence = Arrays.asList(ADDONS, RANDOMIZATION, API_SERVER, THREAD_POOL, BLOCKCHAIN, PEER_NETWORKING, DB, LOGGER);

        @Override
        public List<SubSystem> initSequence() {
            return initSequence;
        }

        @Override
        public List<SubSystem> shutdownSequence() {
            return shutdownSequence;
        }
    };

    Setup CLIENT_APP = new Setup() {
        List<SubSystem> subSystems = Arrays.asList(LOGGER, ADDONS, API_SERVER);

        @Override
        public List<SubSystem> initSequence() {
            return subSystems;
        }

        @Override
        public List<SubSystem> shutdownSequence() {
            return subSystems;
        }
    };

    Setup NOT_INITIALIZED = new Setup() {
        @Override
        public List<SubSystem> initSequence() {
            return Collections.emptyList();
        }

        @Override
        public List<SubSystem> shutdownSequence() {
            return Collections.emptyList();
        }
    };
}
