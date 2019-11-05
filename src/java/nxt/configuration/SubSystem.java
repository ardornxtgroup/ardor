/*
 * Copyright © 2016-2019 Jelurida IP B.V.
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

package nxt.configuration;

import nxt.Constants;
import nxt.Nxt;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.AccountRestrictions;
import nxt.account.FundingMonitor;
import nxt.addons.AddOns;
import nxt.addons.DebugTrace;
import nxt.ae.Asset;
import nxt.ae.AssetControl;
import nxt.ae.AssetHistory;
import nxt.ae.AssetTransfer;
import nxt.blockchain.BlockchainProcessorImpl;
import nxt.blockchain.Bundler;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.blockchain.Generator;
import nxt.blockchain.TransactionProcessorImpl;
import nxt.ce.CoinExchange;
import nxt.crypto.Crypto;
import nxt.dbschema.Db;
import nxt.env.RuntimeEnvironment;
import nxt.env.ServerStatus;
import nxt.http.API;
import nxt.http.APIProxy;
import nxt.lightcontracts.ContractReference;
import nxt.ms.Currency;
import nxt.ms.CurrencyMint;
import nxt.ms.CurrencyTransfer;
import nxt.peer.NetworkHandler;
import nxt.peer.Peers;
import nxt.util.Logger;
import nxt.util.ThreadPool;
import nxt.util.Time;

import static nxt.Nxt.getEpochTime;

public interface SubSystem {

    void init();
    void shutdown();

    SubSystem LOGGER = new SubSystem() {

        @Override
        public void init() {
            Logger.init();
            logSystemProperties();
        }

        @Override
        public void shutdown() {
            Logger.logShutdownMessage("Ardor server " + Nxt.VERSION + " stopped.");
            Logger.shutdown();
        }

        public void logSystemProperties() {
            String[] loggedProperties = new String[] {
                    "java.version",
                    "java.vm.version",
                    "java.vm.name",
                    "java.vendor",
                    "java.vm.vendor",
                    "java.home",
                    "java.library.path",
                    "java.class.path",
                    "os.arch",
                    "sun.arch.data.model",
                    "os.name",
                    "file.encoding",
                    "java.security.policy",
                    "java.security.manager",
                    "java.specification.vendor",
                    RuntimeEnvironment.RUNTIME_MODE_ARG,
                    RuntimeEnvironment.DIRPROVIDER_ARG
            };
            for (String property : loggedProperties) {
                Logger.logDebugMessage(String.format("%s = %s", property, System.getProperty(property)));
            }
            Logger.logDebugMessage(String.format("availableProcessors = %s", Runtime.getRuntime().availableProcessors()));
            Logger.logDebugMessage(String.format("maxMemory = %s", Runtime.getRuntime().maxMemory()));
            Logger.logDebugMessage(String.format("processId = %s", Nxt.getProcessId()));
        }
    };

    SubSystem SYSTEM_TRAY = new SubSystem() {
        @Override
        public void init() {
            Nxt.getRuntimeMode().init();
        }

        @Override
        public void shutdown() {
            Nxt.getRuntimeMode().shutdown();
        }
    };

    SubSystem DB = new SubSystem() {
        @Override
        public void init() {
            Nxt.setServerStatus(ServerStatus.BEFORE_DATABASE, null);
            Db.init();
            Nxt.setServerStatus(ServerStatus.AFTER_DATABASE, null);
        }

        @Override
        public void shutdown() {
            Db.shutdown();
        }
    };

    SubSystem BLOCKCHAIN = new SubSystem() {
        @Override
        public void init() {
            TransactionProcessorImpl.init();
            BlockchainProcessorImpl.init();
            ChildChain.init();
            FxtChain.init();
            Account.init();
            AccountRestrictions.init();
            AccountLedger.init();
            Asset.init();
            AssetTransfer.init();
            AssetControl.init();
            AssetHistory.init();
            Currency.init();
            CurrencyMint.init();
            CurrencyTransfer.init();
            CoinExchange.init();
            ContractReference.init();
            Generator.init();
            Bundler.init();
        }

        @Override
        public void shutdown() {
            FundingMonitor.shutdown();
            BlockchainProcessorImpl.getInstance().shutdown();
        }
    };

    SubSystem PEER_NETWORKING = new SubSystem() {
        @Override
        public void init() {
            NetworkHandler.init();
            Peers.init();
            APIProxy.init();
        }

        @Override
        public void shutdown() {
            Peers.shutdown();
            NetworkHandler.shutdown();
        }
    };

    SubSystem ADDONS = new SubSystem() {
        @Override
        public void init() {
            AddOns.init();
            DebugTrace.init();
        }

        @Override
        public void shutdown() {
            AddOns.shutdown();
        }
    };

    SubSystem API_SERVER = new SubSystem() {
        @Override
        public void init() {
            API.init();
        }

        @Override
        public void shutdown() {
            API.shutdown();
        }
    };

    SubSystem THREAD_POOL = new SubSystem() {
        @Override
        public void init() {
            int timeMultiplier = (Constants.isTestnet && Constants.isOffline) ? Math.max(Nxt.getIntProperty("nxt.timeMultiplier"), 1) : 1;
            ThreadPool.start(timeMultiplier);
            if (timeMultiplier > 1) {
                Nxt.setTime(new Time.FasterTime(Math.max(getEpochTime(), Nxt.getBlockchain().getLastBlock().getTimestamp()), timeMultiplier));
                Logger.logMessage("TIME WILL FLOW " + timeMultiplier + " TIMES FASTER!");
            }
        }

        @Override
        public void shutdown() {
            ThreadPool.shutdown();
        }
    };

    SubSystem RANDOMIZATION = new SubSystem() {

        @Override
        public void init() {
            Thread secureRandomInitThread = initSecureRandom();
            try {
                secureRandomInitThread.join(10000);
            } catch (InterruptedException ignore) {}
            testSecureRandom();
        }

        @Override
        public void shutdown() {}

        private Thread initSecureRandom() {
            Thread secureRandomInitThread = new Thread(() -> Crypto.getSecureRandom().nextBytes(new byte[1024]));
            secureRandomInitThread.setDaemon(true);
            secureRandomInitThread.start();
            return secureRandomInitThread;
        }

        private void testSecureRandom() {
            Thread thread = new Thread(() -> Crypto.getSecureRandom().nextBytes(new byte[1024]));
            thread.setDaemon(true);
            thread.start();
            try {
                thread.join(2000);
                if (thread.isAlive()) {
                    throw new RuntimeException("SecureRandom implementation too slow!!! " +
                            "Install haveged if on linux, or set nxt.useStrongSecureRandom=false.");
                }
            } catch (InterruptedException ignore) {}
        }
    };

    SubSystem DESKTOP_WALLET = new SubSystem() {

        @Override
        public void init() {
            if (RuntimeEnvironment.isDesktopApplicationEnabled()) {
                launchDesktopApplication();
            } else {
                Logger.logInfoMessage("Desktop application is disabled");
            }
        }

        @Override
        public void shutdown() {}

        private void launchDesktopApplication() {
            Nxt.getRuntimeMode().launchDesktopApplication();
        }
    };
}
